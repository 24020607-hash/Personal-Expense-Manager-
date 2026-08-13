package com.expensemanager.command;

import com.expensemanager.model.Transaction;
import com.expensemanager.service.ExpenseManager;

/**
 * Hoàn tác thao tác "thêm giao dịch": gỡ giao dịch khỏi danh sách và gỡ ảnh
 * hưởng của nó khỏi số dư ví.
 */
public class AddTransactionCommand implements Command {

    private final Transaction transaction;
    private final ExpenseManager manager;

    public AddTransactionCommand(Transaction transaction, ExpenseManager manager) {
        this.transaction = transaction;
        this.manager = manager;
    }

    @Override
    public void undo() {
        manager.getTransactions().remove(transaction);
        manager.applyWalletEffect(transaction, false);
    }

    @Override
    public String getDescription() {
        return "Thêm giao dịch #" + transaction.getId();
    }
}
