package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;

import java.time.LocalDate;

/**
 * Giao dịch Chi tiêu, kế thừa Transaction.
 * Đa hình: getSignedAmount() trả về giá trị âm, khác với Income.
 */
public class Expense extends Transaction {

    private String paymentMethod;

    /**
     * Khởi tạo một giao dịch chi tiêu.
     *
     * @param id            mã định danh giao dịch
     * @param amount        số tiền
     * @param date          ngày phát sinh
     * @param note          ghi chú
     * @param category      danh mục
     * @param wallet        ví bị trừ tiền
     * @param paymentMethod hình thức thanh toán (VD: tiền mặt, chuyển khoản...)
     */
    public Expense(int id,
                   double amount,
                   LocalDate date,
                   String note,
                   Category category,
                   Wallet wallet,
                   String paymentMethod) {

        super(id, amount, date, note, category, wallet);
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public double getSignedAmount() {
        return -getAmount();
    }

    @Override
    public void printInfo() {
        System.out.println("Expense: " + getAmount() + " VND");
    }
}
