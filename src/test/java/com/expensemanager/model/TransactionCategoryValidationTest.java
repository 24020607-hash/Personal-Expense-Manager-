package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử ràng buộc: một giao dịch Income chỉ được gán danh mục loại INCOME,
 * một giao dịch Expense chỉ được gán danh mục loại EXPENSE. Đây là bug đã sửa:
 * trước đây có thể tạo Income với danh mục EXPENSE (và ngược lại) mà không bị
 * chặn ở đâu cả.
 */
class TransactionCategoryValidationTest {

    private final Wallet wallet = new CashWallet("Vi test", 1_000_000);

    @Test
    void income_withIncomeCategory_shouldSucceed() {
        Category salary = new Category("Luong", TransactionType.INCOME);
        assertDoesNotThrow(() ->
                new Income(1, 100_000, LocalDate.now(), "note", salary, wallet, "Cong ty"));
    }

    @Test
    void income_withExpenseCategory_shouldThrow() {
        Category food = new Category("An uong", TransactionType.EXPENSE);
        assertThrows(IllegalArgumentException.class, () ->
                new Income(1, 100_000, LocalDate.now(), "note", food, wallet, "Cong ty"));
    }

    @Test
    void expense_withExpenseCategory_shouldSucceed() {
        Category food = new Category("An uong", TransactionType.EXPENSE);
        assertDoesNotThrow(() ->
                new Expense(1, 100_000, LocalDate.now(), "note", food, wallet, "Tien mat"));
    }

    @Test
    void expense_withIncomeCategory_shouldThrow() {
        Category salary = new Category("Luong", TransactionType.INCOME);
        assertThrows(IllegalArgumentException.class, () ->
                new Expense(1, 100_000, LocalDate.now(), "note", salary, wallet, "Tien mat"));
    }

    @Test
    void recurringExpense_withIncomeCategory_shouldThrow() {
        // RecurringExpense ke thua Expense nen tu dong duoc validate qua super()
        Category salary = new Category("Luong", TransactionType.INCOME);
        assertThrows(IllegalArgumentException.class, () ->
                new RecurringExpense(1, 100_000, LocalDate.now(), "note", salary, wallet,
                        "Tien mat", com.expensemanager.enums.Period.MONTHLY));
    }
}
