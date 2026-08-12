package com.expensemanager.model;

import com.expensemanager.enums.Period;

import java.time.LocalDate;

/**
 * Giao dịch chi tiêu định kỳ (VD: tiền nhà, tiền internet lặp lại hằng tháng).
 * Kế thừa Expense - minh họa cây kế thừa 3 tầng: Transaction -> Expense -> RecurringExpense.
 */
public class RecurringExpense extends Expense {

    private Period period;

    /**
     * Khởi tạo một giao dịch chi tiêu định kỳ.
     *
     * @param id            mã định danh giao dịch
     * @param amount        số tiền
     * @param date          ngày phát sinh (kỳ đầu tiên)
     * @param note          ghi chú
     * @param category      danh mục
     * @param wallet        ví bị trừ tiền
     * @param paymentMethod hình thức thanh toán
     * @param period        chu kỳ lặp lại (ngày/tuần/tháng/năm)
     */
    public RecurringExpense(int id,
                            double amount,
                            LocalDate date,
                            String note,
                            Category category,
                            Wallet wallet,
                            String paymentMethod,
                            Period period) {

        super(id, amount, date, note, category, wallet, paymentMethod);
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    /**
     * Tính ngày đến hạn kế tiếp dựa trên chu kỳ lặp.
     *
     * @return ngày đến hạn tiếp theo
     */
    public LocalDate nextDueDate() {

        switch (period) {

            case DAILY:
                return getDate().plusDays(1);

            case WEEKLY:
                return getDate().plusWeeks(1);

            case MONTHLY:
                return getDate().plusMonths(1);

            case YEARLY:
                return getDate().plusYears(1);

            default:
                return getDate();
        }
    }
}
