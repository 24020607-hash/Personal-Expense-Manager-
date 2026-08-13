package com.expensemanager.command;

import com.expensemanager.model.Transaction;
import com.expensemanager.service.ExpenseManager;

/**
 * Hoàn tác thao tác "xóa giao dịch": thêm lại giao dịch vào danh sách và
 * áp dụng lại ảnh hưởng của nó lên số dư ví.
 */
public class RemoveTransactionCommand implements Command {

    private final Transaction transaction;
    private final ExpenseManager manager;

    public RemoveTransactionCommand(Transaction transaction, ExpenseManager manager) {
        this.transaction = transaction;
        this.manager = manager;
    }

    @Override
    public void undo() {
        manager.getTransactions().add(transaction);
        manager.applyWalletEffect(transaction, true);
    }

    @Override
    public String getDescription() {
        return "Xóa giao dịch #" + transaction.getId();
    }
}
