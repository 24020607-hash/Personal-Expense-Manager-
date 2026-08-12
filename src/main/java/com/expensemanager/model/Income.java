package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;

import java.time.LocalDate;

/**
 * Giao dịch Thu nhập, kế thừa Transaction.
 * Đa hình: getSignedAmount() trả về giá trị dương, khác với Expense.
 */
public class Income extends Transaction {

    private String source;

    /**
     * Khởi tạo một giao dịch thu nhập.
     *
     * @param id       mã định danh giao dịch
     * @param amount   số tiền
     * @param date     ngày phát sinh
     * @param note     ghi chú
     * @param category danh mục
     * @param wallet   ví nhận tiền
     * @param source   nguồn thu (VD: lương, thưởng...)
     */
    public Income(int id,
                  double amount,
                  LocalDate date,
                  String note,
                  Category category,
                  Wallet wallet,
                  String source) {

        super(id, amount, date, note, category, wallet);
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.INCOME;
    }

    @Override
    public double getSignedAmount() {
        return getAmount();
    }

    @Override
    public void printInfo() {
        System.out.println("Income: " + getAmount() + " VND");
    }
}
