package com.expensemanager.model;

import com.expensemanager.enums.Period;

public class Budget {

    private Category category;
    private double limit;
    private Period period;

    public Budget(Category category, double limit, Period period) {

        this.category = category;
        setLimit(limit);
        this.period = period;
    }

    public Category getCategory() {
        return category;
    }

    public double getLimit() {
        return limit;
    }

    public Period getPeriod() {
        return period;
    }

    public void setLimit(double limit) {

        if (limit <= 0) {
            throw new IllegalArgumentException("Budget limit must be greater than 0.");
        }

        this.limit = limit;
    }

    public boolean isExceeded(double spent) {
        return spent > limit;
    }
}