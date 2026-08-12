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

    /**
     * Tạo một giao dịch Thu nhập.
     *
     * @param amount   số tiền
     * @param date     ngày phát sinh
     * @param note     ghi chú
     * @param category danh mục
     * @param wallet   ví nhận tiền
     * @param source   nguồn thu
     * @return đối tượng Income vừa tạo, với id được sinh tự động
     */
    public static Transaction createIncome(double amount,
                                            LocalDate date,
                                            String note,
                                            Category category,
                                            Wallet wallet,
                                            String source) {

        int id = IdGenerator.nextId();
        return new Income(id, amount, date, note, category, wallet, source);
    }

    /**
     * Tạo một giao dịch Chi tiêu.
     *
     * @param amount        số tiền
     * @param date          ngày phát sinh
     * @param note          ghi chú
     * @param category      danh mục
     * @param wallet        ví bị trừ tiền
     * @param paymentMethod hình thức thanh toán
     * @return đối tượng Expense vừa tạo, với id được sinh tự động
     */
    public static Transaction createExpense(double amount,
                                             LocalDate date,
                                             String note,
                                             Category category,
                                             Wallet wallet,
                                             String paymentMethod) {

        int id = IdGenerator.nextId();
        return new Expense(id, amount, date, note, category, wallet, paymentMethod);
    }

    /**
     * Tạo một giao dịch Chi tiêu định kỳ.
     *
     * @param amount        số tiền
     * @param date          ngày phát sinh (kỳ đầu tiên)
     * @param note          ghi chú
     * @param category      danh mục
     * @param wallet        ví bị trừ tiền
     * @param paymentMethod hình thức thanh toán
     * @param period        chu kỳ lặp lại
     * @return đối tượng RecurringExpense vừa tạo, với id được sinh tự động
     */
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
     *
     * @param type     loại giao dịch (INCOME/EXPENSE)
     * @param amount   số tiền
     * @param date     ngày phát sinh
     * @param note     ghi chú
     * @param category danh mục
     * @param wallet   ví liên quan
     * @return đối tượng Income hoặc Expense tương ứng
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
