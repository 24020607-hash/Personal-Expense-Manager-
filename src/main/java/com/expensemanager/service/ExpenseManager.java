package com.expensemanager.service;

import com.expensemanager.enums.TransactionType;
import com.expensemanager.model.*;
import com.expensemanager.storage.Storage;
import com.expensemanager.util.IdGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Lớp điều phối trung tâm (Design Pattern: Singleton) - quản lý toàn bộ
 * transaction/wallet/category/budget và một tham chiếu Storage để đọc/ghi file.
 * Dùng Singleton vì toàn bộ ứng dụng (GUI lẫn Console) chỉ nên thao tác trên
 * MỘT bộ dữ liệu duy nhất tại một thời điểm.
 */
public class ExpenseManager {

    private static ExpenseManager instance;

    private List<Transaction> transactions;
    private List<Wallet> wallets;
    private List<Category> categories;
    private Map<Category, Budget> budgets;

    private Storage storage;

    private ExpenseManager() {
        transactions = new ArrayList<>();
        wallets = new ArrayList<>();
        categories = new ArrayList<>();
        budgets = new HashMap<>();
    }

    public static ExpenseManager getInstance() {
        if (instance == null) {
            instance = new ExpenseManager();
        }
        return instance;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    // ================== WALLET ==================

    public void addWallet(Wallet wallet) {
        if (findWalletByName(wallet.getName()) != null) {
            throw new IllegalArgumentException("Ví \"" + wallet.getName() + "\" đã tồn tại.");
        }
        wallets.add(wallet);
    }

    public boolean removeWallet(String name) {
        Wallet wallet = findWalletByName(name);
        if (wallet == null) {
            return false;
        }
        boolean inUse = transactions.stream().anyMatch(t -> t.getWallet().equals(wallet));
        if (inUse) {
            throw new IllegalStateException("Không thể xóa ví đang có giao dịch liên quan.");
        }
        return wallets.remove(wallet);
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public Wallet findWalletByName(String name) {
        for (Wallet w : wallets) {
            if (w.getName().equalsIgnoreCase(name)) {
                return w;
            }
        }
        return null;
    }

    // ================== CATEGORY ==================

    public void addCategory(Category category) {
        if (findCategoryByName(category.getName()) != null) {
            throw new IllegalArgumentException("Danh mục \"" + category.getName() + "\" đã tồn tại.");
        }
        categories.add(category);
    }

    public boolean removeCategory(String name) {
        Category category = findCategoryByName(name);
        if (category == null) {
            return false;
        }
        boolean inUse = transactions.stream().anyMatch(t -> t.getCategory().equals(category));
        if (inUse) {
            throw new IllegalStateException("Không thể xóa danh mục đang có giao dịch liên quan.");
        }
        budgets.remove(category);
        return categories.remove(category);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Category findCategoryByName(String name) {
        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    // ================== TRANSACTION (CRUD) ==================

    /**
     * Thêm giao dịch mới VÀ áp dụng ảnh hưởng lên số dư ví (đa hình: Income ->
     * deposit(), Expense -> withdraw() - mỗi loại ví withdraw() khác nhau).
     * Nếu strictBalanceCheck = true và ví không đủ tiền, withdraw() sẽ ném
     * IllegalArgumentException và giao dịch KHÔNG được thêm vào danh sách.
     */
    public void addTransaction(Transaction transaction) {
        applyToWallet(transaction, +1);
        transactions.add(transaction);
    }

    public boolean removeTransaction(int id) {
        Transaction transaction = findTransactionById(id);
        if (transaction == null) {
            return false;
        }
        // Hoàn tác ảnh hưởng lên ví trước khi xóa (ngược lại lúc thêm)
        applyToWallet(transaction, -1);
        transactions.remove(transaction);
        return true;
    }

    /**
     * Cập nhật 1 giao dịch: hoàn tác ảnh hưởng cũ lên ví, áp dụng ảnh hưởng mới.
     * Nếu ảnh hưởng mới không hợp lệ (VD: không đủ số dư), giao dịch cũ sẽ được
     * khôi phục nguyên trạng (rollback) và exception được ném lên cho tầng UI xử lý.
     */
    public void updateTransaction(int id,
                                   double newAmount,
                                   LocalDate newDate,
                                   String newNote,
                                   Category newCategory,
                                   Wallet newWallet) {

        Transaction transaction = findTransactionById(id);
        if (transaction == null) {
            throw new NoSuchElementException("Không tìm thấy giao dịch có ID = " + id);
        }

        // Lưu lại trạng thái cũ để rollback nếu cần
        double oldAmount = transaction.getAmount();
        LocalDate oldDate = transaction.getDate();
        String oldNote = transaction.getNote();
        Category oldCategory = transaction.getCategory();
        Wallet oldWallet = transaction.getWallet();

        applyToWallet(transaction, -1); // hoàn tác ảnh hưởng cũ

        try {
            transaction.setAmount(newAmount);
            transaction.setDate(newDate);
            transaction.setNote(newNote);
            transaction.setCategory(newCategory);
            transaction.setWallet(newWallet);
            applyToWallet(transaction, +1); // áp dụng ảnh hưởng mới
        } catch (RuntimeException e) {
            // Rollback về trạng thái cũ nếu cập nhật thất bại
            transaction.setAmount(oldAmount);
            transaction.setDate(oldDate);
            transaction.setNote(oldNote);
            transaction.setCategory(oldCategory);
            transaction.setWallet(oldWallet);
            applyToWallet(transaction, +1);
            throw e;
        }
    }

    /** direction = +1 khi thêm giao dịch, -1 khi hoàn tác (xóa/sửa). */
    private void applyToWallet(Transaction transaction, int direction) {

        Wallet wallet = transaction.getWallet();
        double amount = transaction.getAmount();
        boolean isIncome = transaction.getType() == TransactionType.INCOME;

        if ((isIncome && direction == 1) || (!isIncome && direction == -1)) {
            wallet.deposit(amount);
        } else if (!isIncome && direction == 1) {
            // Chi tiêu THẬT SỰ (thêm/cập nhật Expense) -> dùng withdraw(), có thể
            // bị tính phí tùy loại ví (VD: BankAccount trừ thêm phí giao dịch).
            wallet.withdraw(amount);
        } else {
            // Hoàn tác một giao dịch Thu nhập (xóa/sửa Income) -> trừ lại đúng số
            // tiền đã cộng, KHÔNG được tính phí (đây không phải hành vi rút tiền).
            wallet.deductWithoutFee(amount);
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Transaction findTransactionById(int id) {
        for (Transaction transaction : transactions) {
            if (transaction.getId() == id) {
                return transaction;
            }
        }
        return null;
    }

    /** Tìm kiếm theo danh mục (có thể null), khoảng ngày (có thể null), số tiền (có thể null). */
    public List<Transaction> findTransactions(Category category,
                                               LocalDate fromDate,
                                               LocalDate toDate,
                                               Double exactAmount) {

        List<Transaction> result = new ArrayList<>();

        for (Transaction t : transactions) {
            if (category != null && !t.getCategory().equals(category)) continue;
            if (fromDate != null && t.getDate().isBefore(fromDate)) continue;
            if (toDate != null && t.getDate().isAfter(toDate)) continue;
            if (exactAmount != null && Double.compare(t.getAmount(), exactAmount) != 0) continue;
            result.add(t);
        }

        return result;
    }

    // ================== THỐNG KÊ ==================

    public double getMonthlyIncome(YearMonth month) {
        double total = 0;
        for (Transaction transaction : transactions) {
            if (YearMonth.from(transaction.getDate()).equals(month)
                    && transaction.getType() == TransactionType.INCOME) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public double getMonthlyExpense(YearMonth month) {
        double total = 0;
        for (Transaction transaction : transactions) {
            if (YearMonth.from(transaction.getDate()).equals(month)
                    && transaction.getType() == TransactionType.EXPENSE) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public double getMonthlyBalance(YearMonth month) {
        return getMonthlyIncome(month) - getMonthlyExpense(month);
    }

    /** Tổng chi tiêu theo từng danh mục trong 1 tháng - dùng vẽ biểu đồ tròn. */
    public Map<Category, Double> statisticsByCategory(YearMonth month) {

        Map<Category, Double> result = new LinkedHashMap<>();

        for (Transaction t : transactions) {
            if (t.getType() != TransactionType.EXPENSE) continue;
            if (!YearMonth.from(t.getDate()).equals(month)) continue;

            result.merge(t.getCategory(), t.getAmount(), Double::sum);
        }

        return result;
    }

    public Optional<Transaction> getLargestExpense(YearMonth month) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> YearMonth.from(t.getDate()).equals(month))
                .max(Comparator.comparingDouble(Transaction::getAmount));
    }

    public Optional<Transaction> getSmallestExpense(YearMonth month) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> YearMonth.from(t.getDate()).equals(month))
                .min(Comparator.comparingDouble(Transaction::getAmount));
    }

    public Optional<Map.Entry<Category, Double>> getTopExpensiveCategory(YearMonth month) {
        return statisticsByCategory(month).entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    // ================== BUDGET ==================

    public void setBudget(Budget budget) {
        budgets.put(budget.getCategory(), budget);
    }

    public Map<Category, Budget> getBudgets() {
        return budgets;
    }

    public double getSpentByCategory(Category category, YearMonth month) {
        double total = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getType() == TransactionType.EXPENSE
                    && transaction.getCategory().equals(category)
                    && YearMonth.from(transaction.getDate()).equals(month)) {
                total += transaction.getAmount();
            }
        }
        return total;
    }

    public boolean isBudgetExceeded(Category category, YearMonth month) {
        Budget budget = budgets.get(category);
        if (budget == null) {
            return false;
        }
        return budget.isExceeded(getSpentByCategory(category, month));
    }

    // ================== STORAGE ==================

    public void saveData(String path) throws IOException {
        storage.save(transactions, path);
    }

    public void loadData(String path) throws IOException {
        transactions = storage.load(path, wallets, categories);
        IdGenerator.initFrom(transactions);
    }
}
