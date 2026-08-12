package com.expensemanager.model;

import com.expensemanager.enums.WalletType;

/**
 * Ví điện tử (VD: Momo, ZaloPay...). Rút tiền không tính phí giao dịch,
 * tương tự CashWallet nhưng khác loại (getWalletType()).
 */
public class EWallet extends Wallet {

    private String provider;

    /**
     * Khởi tạo ví điện tử.
     *
     * @param name     tên ví hiển thị
     * @param balance  số dư ban đầu
     * @param provider nhà cung cấp (VD: Momo, ZaloPay)
     */
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
