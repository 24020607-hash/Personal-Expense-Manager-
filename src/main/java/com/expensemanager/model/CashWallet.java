package com.expensemanager.model;

import com.expensemanager.enums.WalletType;

/**
 * Ví tiền mặt. Rút tiền không tính phí giao dịch.
 */
public class CashWallet extends Wallet {

    /**
     * Khởi tạo ví tiền mặt.
     *
     * @param name    tên ví
     * @param balance số dư ban đầu
     */
    public CashWallet(String name, double balance) {
        super(name, balance);
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
        return WalletType.CASH;
    }
}
