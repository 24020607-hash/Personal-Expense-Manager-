package com.expensemanager.command;

import com.expensemanager.model.Wallet;
import com.expensemanager.service.ExpenseManager;

/**
 * Hoàn tác thao tác "thêm ví": gỡ ví khỏi danh sách.
 */
public class AddWalletCommand implements Command {

    private final Wallet wallet;
    private final ExpenseManager manager;

    public AddWalletCommand(Wallet wallet, ExpenseManager manager) {
        this.wallet = wallet;
        this.manager = manager;
    }

    @Override
    public void undo() {
        manager.getWallets().remove(wallet);
    }

    @Override
    public String getDescription() {
        return "Thêm ví \"" + wallet.getName() + "\"";
    }
}
