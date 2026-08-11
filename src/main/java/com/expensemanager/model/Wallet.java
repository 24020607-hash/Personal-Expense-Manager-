package com.expensemanager.model;

import com.expensemanager.enums.WalletType;

public abstract class Wallet {

    private String name;
    private double balance;

    public Wallet(String name, double balance) {
        this.name = name;
        setBalance(balance);
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }
        this.balance = balance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        balance += amount;
    }

    public abstract void withdraw(double amount);

    /**
     * Trừ tiền trực tiếp, KHÔNG áp dụng phí giao dịch (dùng khi hoàn tác/rollback
     * một giao dịch Thu nhập đã cộng trước đó - khác với withdraw() dùng cho
     * chi tiêu thật, có thể bị tính phí tùy loại ví).
     */
    public void deductWithoutFee(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        if (amount > getBalance()) {
            throw new IllegalArgumentException("Insufficient balance to reverse this transaction.");
        }
        setBalance(getBalance() - amount);
    }

    public abstract WalletType getWalletType();

    @Override
    public String toString() {
        return name;
    }
}