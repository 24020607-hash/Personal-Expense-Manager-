package com.expensemanager.util;

/**
 * Các hàm kiểm tra hợp lệ dữ liệu nhập từ giao diện,
 * dùng trước khi tạo đối tượng model (tránh exception "khó hiểu" bung ra UI).
 */
public class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean isPositiveNumber(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        try {
            double value = Double.parseDouble(text.trim());
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.isBlank();
    }
}
