package com.expensemanager.service;

import com.expensemanager.enums.TransactionType;
import com.expensemanager.factory.TransactionFactory;
import com.expensemanager.model.CashWallet;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseManagerTest {

    private ExpenseManager manager;
    private Wallet wallet;
    private Category salary;
    private Category food;

    @BeforeEach
    void setUp() throws Exception {
        // Reset Singleton giua cac test bang reflection, de moi test doc lap
        // (Singleton thuong khong lam the nay trong production code, chi dung
        // cho muc dich kiem thu don vi).
        Field instanceField = ExpenseManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        manager = ExpenseManager.getInstance();

        wallet = new CashWallet("Vi test", 1_000_000);
        manager.addWallet(wallet);

        salary = new Category("Luong", TransactionType.INCOME);
        food = new Category("An uong", TransactionType.EXPENSE);
        manager.addCategory(salary);
        manager.addCategory(food);
    }

    @Test
    void addTransaction_income_shouldIncreaseWalletBalance() {
        Transaction income = TransactionFactory.createIncome(
                2_000_000, LocalDate.now(), "Luong thang 8", salary, wallet, "Cong ty");
        manager.addTransaction(income);

        assertEquals(3_000_000, wallet.getBalance());
    }

    @Test
    void addTransaction_expense_shouldDecreaseWalletBalance() {
        Transaction expense = TransactionFactory.createExpense(
                200_000, LocalDate.now(), "An trua", food, wallet, "Tien mat");
        manager.addTransaction(expense);

        assertEquals(800_000, wallet.getBalance());
    }

    @Test
    void addTransaction_expenseExceedBalance_shouldThrowAndNotAdd() {
        Transaction expense = TransactionFactory.createExpense(
                5_000_000, LocalDate.now(), "Qua tay", food, wallet, "Tien mat");

        assertThrows(IllegalArgumentException.class, () -> manager.addTransaction(expense));
        assertEquals(1_000_000, wallet.getBalance()); // khong bi tru vi giao dich khong hop le
        assertTrue(manager.getTransactions().isEmpty());
    }

    @Test
    void removeTransaction_expense_shouldRefundWallet() {
        Transaction expense = TransactionFactory.createExpense(
                200_000, LocalDate.now(), "An trua", food, wallet, "Tien mat");
        manager.addTransaction(expense);
        assertEquals(800_000, wallet.getBalance());

        manager.removeTransaction(expense.getId());
        assertEquals(1_000_000, wallet.getBalance());
        assertTrue(manager.getTransactions().isEmpty());
    }

    @Test
    void updateTransaction_invalidNewAmount_shouldRollback() {
        Transaction expense = TransactionFactory.createExpense(
                200_000, LocalDate.now(), "An trua", food, wallet, "Tien mat");
        manager.addTransaction(expense);

        // Thu sua thanh so tien vuot qua so du hien tai -> phai rollback ve cu
        assertThrows(IllegalArgumentException.class, () ->
                manager.updateTransaction(expense.getId(), 5_000_000, LocalDate.now(),
                        "note", food, wallet));

        assertEquals(200_000, expense.getAmount()); // gia tri cu duoc giu nguyen
        assertEquals(800_000, wallet.getBalance());  // so du khong bi anh huong
    }

    @Test
    void removeTransaction_income_onBankAccount_shouldRefundWithoutFee() {
        // Bug tai hien: BankAccount.withdraw() tinh them phi 1000, truoc day khi
        // hoan tac (xoa) mot giao dich Income, code dung nham withdraw() nen bi
        // tru oan phi nay. Gio phai dung deductWithoutFee() -> hoan du, khong phi.
        Wallet bankWallet = new com.expensemanager.model.BankAccount(
                "The ABC", 100_000, "Vietcombank", "0011001");
        manager.addWallet(bankWallet);

        Transaction income = TransactionFactory.createIncome(
                20_000, LocalDate.now(), "Thuong", salary, bankWallet, "Khac");
        manager.addTransaction(income);
        assertEquals(120_000, bankWallet.getBalance());

        manager.removeTransaction(income.getId());
        assertEquals(100_000, bankWallet.getBalance()); // phai hoan du 100.000, khong bi tru phi
    }

    @Test
    void monthlySummary_shouldCalculateCorrectly() {
        LocalDate now = LocalDate.now();
        manager.addTransaction(TransactionFactory.createIncome(
                2_000_000, now, "Luong", salary, wallet, "Cong ty"));
        manager.addTransaction(TransactionFactory.createExpense(
                300_000, now, "An uong", food, wallet, "Tien mat"));

        YearMonth month = YearMonth.from(now);
        assertEquals(2_000_000, manager.getMonthlyIncome(month));
        assertEquals(300_000, manager.getMonthlyExpense(month));
        assertEquals(1_700_000, manager.getMonthlyBalance(month));
    }

    @Test
    void findTransactions_byCategory_shouldFilterCorrectly() {
        LocalDate now = LocalDate.now();
        manager.addTransaction(TransactionFactory.createExpense(
                100_000, now, "Note 1", food, wallet, "Tien mat"));
        manager.addTransaction(TransactionFactory.createIncome(
                500_000, now, "Note 2", salary, wallet, "Cong ty"));

        List<Transaction> results = manager.findTransactions(food, null, null, null);

        assertEquals(1, results.size());
        assertEquals(food, results.get(0).getCategory());
    }
}
