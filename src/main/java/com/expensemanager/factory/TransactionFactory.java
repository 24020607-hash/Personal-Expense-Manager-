package com.expensemanager.factory;

import com.expensemanager.enums.Period;
import com.expensemanager.enums.TransactionType;
import com.expensemanager.model.*;
import com.expensemanager.util.IdGenerator;

import java.time.LocalDate;

/**
 * Factory Pattern: tập trung logic khởi tạo các loại Transaction (Income/Expense/
 * RecurringExpense) tại một nơi duy nhất. Controller chỉ cần gọi factory,
 * không cần biết chi tiết constructor của từng lớp con.
 */
public class TransactionFactory {

    private TransactionFactory() {
    }

    public static Transaction createIncome(double amount,
                                            LocalDate date,
                                            String note,
                                            Category category,
                                            Wallet wallet,
                                            String source) {

        int id = IdGenerator.nextId();
        return new Income(id, amount, date, note, category, wallet, source);
    }

    public static Transaction createExpense(double amount,
                                             LocalDate date,
                                             String note,
                                             Category category,
                                             Wallet wallet,
                                             String paymentMethod) {

        int id = IdGenerator.nextId();
        return new Expense(id, amount, date, note, category, wallet, paymentMethod);
    }

    public static Transaction createRecurringExpense(double amount,
                                                       LocalDate date,
                                                       String note,
                                                       Category category,
                                                       Wallet wallet,
                                                       String paymentMethod,
                                                       Period period) {

        int id = IdGenerator.nextId();
        return new RecurringExpense(id, amount, date, note, category, wallet, paymentMethod, period);
    }

    /**
     * Tạo giao dịch theo loại (dùng khi UI chỉ cho người dùng chọn combo Thu/Chi
     * mà không phân biệt Expense thường hay Recurring).
     */
    public static Transaction create(TransactionType type,
                                      double amount,
                                      LocalDate date,
                                      String note,
                                      Category category,
                                      Wallet wallet) {

        if (type == TransactionType.INCOME) {
            return createIncome(amount, date, note, category, wallet, "Khác");
        }
        return createExpense(amount, date, note, category, wallet, "Tiền mặt");
    }
}
