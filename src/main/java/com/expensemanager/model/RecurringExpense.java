package com.expensemanager.model;

import com.expensemanager.enums.Period;

import java.time.LocalDate;

public class RecurringExpense extends Expense {

    private Period period;

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