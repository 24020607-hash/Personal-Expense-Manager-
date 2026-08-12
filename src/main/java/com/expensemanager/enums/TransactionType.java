package com.expensemanager.enums;

/**
 * Loại giao dịch: Thu nhập hoặc Chi tiêu.
 */
public enum TransactionType {
    INCOME,
    EXPENSE;

    /**
     * Nhãn hiển thị tiếng Việt cho giao diện. Không override toString()
     * để KHÔNG làm hỏng TransactionType.valueOf(...) khi đọc lại file CSV
     * (CsvStorage lưu theo tên gốc INCOME/EXPENSE).
     *
     * @return "Thu nhập" hoặc "Chi tiêu"
     */
    public String getLabelSafe() {
        return this == INCOME ? "Thu nhập" : "Chi tiêu";
    }
}
