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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tính năng Hoàn tác (Command Pattern): undoLast() cho từng loại
 * thao tác, và undoAll() đảm bảo khôi phục đúng thứ tự (LIFO) không phá vỡ
 * tính toàn vẹn dữ liệu.
 */
class UndoTest {

    private ExpenseManager manager;
    private Wallet wallet;
    private Category salary;
    private Category food;

    @BeforeEach
    void setUp() throws Exception {

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
    void undoLast_afterAddTransaction_shouldRemoveItAndRefundWallet() {

        Transaction income = TransactionFactory.createIncome(
                200_000, LocalDate.now(), "Thuong", salary, wallet, "Cong ty");
        manager.addTransaction(income);
        assertEquals(1_200_000, wallet.getBalance());

        String description = manager.undoLast();

        assertTrue(description.contains("Thêm giao dịch"));
        assertEquals(1_000_000, wallet.getBalance());
        assertTrue(manager.getTransactions().isEmpty());
    }

    @Test
    void undoLast_afterRemoveTransaction_shouldRestoreItAndReapplyEffect() {

        Transaction expense = TransactionFactory.createExpense(
                100_000, LocalDate.now(), "An trua", food, wallet, "Tien mat");
        manager.addTransaction(expense);
        manager.removeTransaction(expense.getId());
        assertEquals(1_000_000, wallet.getBalance()); // đã hoàn lại khi xóa

        manager.undoLast();

        assertEquals(1, manager.getTransactions().size());
        assertEquals(900_000, wallet.getBalance()); // áp dụng lại đúng như trước khi xóa
    }

    @Test
    void undoLast_afterUpdateTransaction_shouldRestoreOldValues() {

        Transaction expense = TransactionFactory.createExpense(
                100_000, LocalDate.now(), "An trua", food, wallet, "Tien mat");
        manager.addTransaction(expense);

        manager.updateTransaction(expense.getId(), 300_000, LocalDate.now(),
                "Da sua", food, wallet);
        assertEquals(700_000, wallet.getBalance());

        manager.undoLast();

        assertEquals(100_000, expense.getAmount());
        assertEquals(900_000, wallet.getBalance());
    }

    @Test
    void undoLast_withNoHistory_shouldThrow() {
        manager.undoAll(); // dọn sạch lịch sử do setUp() tạo ra (thêm ví, thêm danh mục)
        assertThrows(IllegalStateException.class, () -> manager.undoLast());
    }

    @Test
    void canUndo_reflectsHistoryState() {
        assertTrue(manager.canUndo()); // đã có addWallet + 2 addCategory từ setUp()
        manager.undoAll();
        assertFalse(manager.canUndo());
    }

    @Test
    void undoAll_shouldReverseEntireSessionHistory() {

        // Kịch bản đúng như lo ngại ban đầu của người dùng: "lỡ xóa hết dữ liệu".
        // Lưu ý: undoAll() hoàn tác TOÀN BỘ lịch sử của phiên làm việc (kể cả
        // việc tạo ví/danh mục lúc đầu), KHÔNG chỉ hoàn tác riêng các lệnh xóa
        // gần nhất - đưa dữ liệu về đúng lúc mới mở ứng dụng.
        Transaction t1 = TransactionFactory.createIncome(
                500_000, LocalDate.now(), "Luong", salary, wallet, "Cong ty");
        manager.addTransaction(t1);

        Transaction t2 = TransactionFactory.createExpense(
                200_000, LocalDate.now(), "Cho", food, wallet, "Tien mat");
        manager.addTransaction(t2);

        manager.removeTransaction(t1.getId());
        manager.removeTransaction(t2.getId());

        assertTrue(manager.getTransactions().isEmpty());
        assertEquals(1_000_000, wallet.getBalance());

        int undone = manager.undoAll();

        assertTrue(undone > 0);
        assertTrue(manager.getTransactions().isEmpty()); // đã lùi về trước cả lúc thêm giao dịch
        assertTrue(manager.getWallets().isEmpty());       // và trước cả lúc thêm ví
        assertTrue(manager.getCategories().isEmpty());    // và trước cả lúc thêm danh mục
        assertEquals(1_000_000, wallet.getBalance());      // số dư ví trở về đúng giá trị gốc
        assertFalse(manager.canUndo());
    }

    @Test
    void undoLast_calledTwice_shouldRestoreOnlyTheDeletedTransactions() {

        // Đây là cách dùng ĐÚNG khi chỉ muốn khôi phục lại các giao dịch vừa
        // lỡ xóa, mà KHÔNG muốn mất luôn cả ví/danh mục đã thiết lập từ trước
        // (khác với undoAll() sẽ lùi về tận lúc mới mở app).
        Transaction t1 = TransactionFactory.createIncome(
                500_000, LocalDate.now(), "Luong", salary, wallet, "Cong ty");
        manager.addTransaction(t1);

        Transaction t2 = TransactionFactory.createExpense(
                200_000, LocalDate.now(), "Cho", food, wallet, "Tien mat");
        manager.addTransaction(t2);

        manager.removeTransaction(t1.getId());
        manager.removeTransaction(t2.getId());
        assertTrue(manager.getTransactions().isEmpty());

        manager.undoLast(); // hoàn tác lệnh xóa t2
        manager.undoLast(); // hoàn tác lệnh xóa t1

        assertEquals(2, manager.getTransactions().size());
        assertEquals(1, manager.getWallets().size());   // ví vẫn còn nguyên
        assertEquals(2, manager.getCategories().size()); // danh mục vẫn còn nguyên
        assertEquals(1_300_000, wallet.getBalance());     // 1.000.000 + 500.000 - 200.000
    }

    @Test
    void undoLast_afterAddWallet_shouldRemoveWallet() {

        Wallet newWallet = new CashWallet("Vi phu", 50_000);
        manager.addWallet(newWallet);
        assertEquals(2, manager.getWallets().size());

        manager.undoLast();

        assertEquals(1, manager.getWallets().size());
        assertNull(manager.findWalletByName("Vi phu"));
    }

    @Test
    void undoLast_afterRemoveCategory_shouldRestoreCategory() {

        manager.removeCategory("An uong");
        assertNull(manager.findCategoryByName("An uong"));

        manager.undoLast();

        assertNotNull(manager.findCategoryByName("An uong"));
    }
}
