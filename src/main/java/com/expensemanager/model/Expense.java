package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;

import java.time.LocalDate;

public class Expense extends Transaction {

    private String paymentMethod;

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