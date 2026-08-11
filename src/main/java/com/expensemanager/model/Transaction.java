package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;

import java.time.LocalDate;

public abstract class Transaction {

    private int id;
    private double amount;
    private LocalDate date;
    private String note;
    private Category category;
    private Wallet wallet;

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

    public abstract TransactionType getType();

    public abstract double getSignedAmount();

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