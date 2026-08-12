package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;

import java.time.LocalDate;

/**
 * Lớp trừu tượng đại diện cho một giao dịch thu/chi.
 * Là lớp cha của Income, Expense, RecurringExpense (kế thừa + đa hình
 * thông qua getType() và getSignedAmount()).
 */
public abstract class Transaction {

    private int id;
    private double amount;
    private LocalDate date;
    private String note;
    private Category category;
    private Wallet wallet;

    /**
     * Khởi tạo một giao dịch.
     *
     * @param id       mã định danh giao dịch
     * @param amount   số tiền, phải lớn hơn 0
     * @param date     ngày phát sinh giao dịch
     * @param note     ghi chú
     * @param category danh mục của giao dịch
     * @param wallet   ví/tài khoản liên quan
     */
    public Transaction(int id,
                        double amount,
                        LocalDate date,
                        String note,
                        Category category,
                        Wallet wallet) {

        this.id = id;
        setAmount(amount);
        this.date = date;
        this.note = note;
        this.category = category;
        this.wallet = wallet;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    /**
     * Gán số tiền cho giao dịch, có kiểm tra hợp lệ (đóng gói).
     *
     * @param amount số tiền mới, phải lớn hơn 0
     * @throws IllegalArgumentException nếu amount không lớn hơn 0
     */
    public void setAmount(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    /**
     * @return loại giao dịch (Thu nhập/Chi tiêu), mỗi lớp con trả về giá trị khác nhau (đa hình)
     */
    public abstract TransactionType getType();

    /**
     * @return số tiền có dấu: dương với Income, âm với Expense (đa hình)
     */
    public abstract double getSignedAmount();

    /**
     * In thông tin tóm tắt của giao dịch ra console.
     */
    public void printInfo() {
        System.out.println(
                "ID: " + id
                        + " | Amount: " + amount
                        + " | Date: " + date
                        + " | Category: " + category.getName()
                        + " | Wallet: " + wallet.getName()
        );
    }
}
