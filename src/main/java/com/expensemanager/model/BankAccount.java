package com.expensemanager.model;

import com.expensemanager.enums.WalletType;

/**
 * Tài khoản ngân hàng. Khác với CashWallet: rút tiền bị tính thêm phí giao
 * dịch cố định (minh họa tính đa hình của withdraw()).
 */
public class BankAccount extends Wallet {

    private String bankName;
    private String accountNumber;

    /**
     * Khởi tạo tài khoản ngân hàng.
     *
     * @param name          tên ví hiển thị
     * @param balance       số dư ban đầu
     * @param bankName      tên ngân hàng
     * @param accountNumber số tài khoản
     */
    public BankAccount(String name,
                       double balance,
                       String bankName,
                       String accountNumber) {

        super(name, balance);
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public void withdraw(double amount) {

        double fee = 1000;

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        if (amount + fee > getBalance()) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        setBalance(getBalance() - amount - fee);
    }

    @Override
    public WalletType getWalletType() {
        return WalletType.BANK;
    }
}
