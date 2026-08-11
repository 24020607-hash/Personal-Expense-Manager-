package com.expensemanager.factory;

import com.expensemanager.enums.WalletType;
import com.expensemanager.model.BankAccount;
import com.expensemanager.model.CashWallet;
import com.expensemanager.model.EWallet;
import com.expensemanager.model.Wallet;

/**
 * Factory Pattern: tạo đối tượng Wallet theo WalletType, gom logic
 * "loại nào cần thêm thông tin gì" về một chỗ thay vì rải if/else ở UI.
 */
public class WalletFactory {

    private WalletFactory() {
    }

    public static Wallet create(WalletType type,
                                 String name,
                                 double balance,
                                 String extraInfo1,
                                 String extraInfo2) {

        switch (type) {
            case CASH:
                return new CashWallet(name, balance);

            case EWALLET:
                // extraInfo1 = provider (VD: Momo, ZaloPay)
                return new EWallet(name, balance, extraInfo1);

            case BANK:
                // extraInfo1 = bankName, extraInfo2 = accountNumber
                return new BankAccount(name, balance, extraInfo1, extraInfo2);

            default:
                throw new IllegalArgumentException("Loại ví không hợp lệ: " + type);
        }
    }
}
