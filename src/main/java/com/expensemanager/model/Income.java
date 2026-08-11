package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;

import java.time.LocalDate;

public class Income extends Transaction {

    private String source;

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