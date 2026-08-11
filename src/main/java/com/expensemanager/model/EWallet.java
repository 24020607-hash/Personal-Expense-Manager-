package com.expensemanager.model;

import com.expensemanager.enums.WalletType;

public class EWallet extends Wallet {

    private String provider;

    public EWallet(String name,
                   double balance,
                   String provider) {

        super(name, balance);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public void withdraw(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        if (amount > getBalance()) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        setBalance(getBalance() - amount);
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.EWALLET;
    }
}