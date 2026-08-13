package com.expensemanager.command;

import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.Wallet;
import com.expensemanager.service.ExpenseManager;

import java.time.LocalDate;

/**
 * Hoàn tác thao tác "sửa giao dịch": gỡ ảnh hưởng hiện tại lên ví, khôi phục
 * lại toàn bộ giá trị cũ của giao dịch, rồi áp dụng lại ảnh hưởng của giá
 * trị cũ đó lên ví.
 */
public class UpdateTransactionCommand implements Command {

    private final Transaction transaction;
    private final ExpenseManager manager;

    private final double oldAmount;
    private final LocalDate oldDate;
    private final String oldNote;
    private final Category oldCategory;
    private final Wallet oldWallet;

    public UpdateTransactionCommand(Transaction transaction,
                                     double oldAmount,
                                     LocalDate oldDate,
                                     String oldNote,
                                     Category oldCategory,
                                     Wallet oldWallet,
                                     ExpenseManager manager) {

        this.transaction = transaction;
        this.oldAmount = oldAmount;
        this.oldDate = oldDate;
        this.oldNote = oldNote;
        this.oldCategory = oldCategory;
        this.oldWallet = oldWallet;
        this.manager = manager;
    }

    @Override
    public void undo() {

        manager.applyWalletEffect(transaction, false); // gỡ ảnh hưởng của giá trị hiện tại (mới)

        transaction.setAmount(oldAmount);
        transaction.setDate(oldDate);
        transaction.setNote(oldNote);
        transaction.setCategory(oldCategory);
        transaction.setWallet(oldWallet);

        manager.applyWalletEffect(transaction, true); // áp dụng lại ảnh hưởng của giá trị cũ
    }

    @Override
    public String getDescription() {
        return "Sửa giao dịch #" + transaction.getId();
    }
}
