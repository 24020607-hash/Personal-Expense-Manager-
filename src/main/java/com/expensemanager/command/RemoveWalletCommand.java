package com.expensemanager.command;

import com.expensemanager.model.Wallet;
import com.expensemanager.service.ExpenseManager;

/**
 * Hoàn tác thao tác "xóa ví": thêm lại ví vào danh sách.
 * An toàn vì removeWallet() chỉ cho xóa khi không còn giao dịch nào tham
 * chiếu đến ví đó, nên tại thời điểm undo, việc thêm lại không gây xung đột.
 */
public class RemoveWalletCommand implements Command {

    private final Wallet wallet;
    private final ExpenseManager manager;

    public RemoveWalletCommand(Wallet wallet, ExpenseManager manager) {
        this.wallet = wallet;
        this.manager = manager;
    }

    @Override
    public void undo() {
        manager.getWallets().add(wallet);
    }

    @Override
    public String getDescription() {
        return "Xóa ví \"" + wallet.getName() + "\"";
    }
}
