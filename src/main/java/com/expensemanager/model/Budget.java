package com.expensemanager.model;

import com.expensemanager.enums.Period;

/**
 * Hạn mức ngân sách đặt cho một danh mục trong một chu kỳ (thường là theo tháng).
 */
public class Budget {

    private Category category;
    private double limit;
    private Period period;

    /**
     * Khởi tạo một hạn mức ngân sách.
     *
     * @param category danh mục áp dụng hạn mức
     * @param limit    hạn mức, phải lớn hơn 0
     * @param period   chu kỳ áp dụng (ngày/tuần/tháng/năm)
     */
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

    /**
     * Gán hạn mức, có kiểm tra hợp lệ (đóng gói).
     *
     * @param limit hạn mức mới
     * @throws IllegalArgumentException nếu limit không lớn hơn 0
     */
    public void setLimit(double limit) {

        if (limit <= 0) {
            throw new IllegalArgumentException("Budget limit must be greater than 0.");
        }

        this.limit = limit;
    }

    /**
     * Kiểm tra một khoản đã chi có vượt hạn mức hay không.
     *
     * @param spent số tiền đã chi trong chu kỳ
     * @return true nếu spent vượt quá hạn mức
     */
    public boolean isExceeded(double spent) {
        return spent > limit;
    }
}
