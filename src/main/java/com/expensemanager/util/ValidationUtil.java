package com.expensemanager.util;

/**
 * Các hàm kiểm tra hợp lệ dữ liệu nhập từ giao diện,
 * dùng trước khi tạo đối tượng model (tránh exception "khó hiểu" bung ra UI).
 */
public class ValidationUtil {

    private ValidationUtil() {
    }

    /**
     * Kiểm tra chuỗi có phải là một số lớn hơn 0 hay không.
     *
     * @param text chuỗi nhập vào từ giao diện
     * @return true nếu text hợp lệ và là số dương
     */
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

    /**
     * @param text chuỗi cần kiểm tra
     * @return true nếu text khác null và không rỗng
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.isBlank();
    }
}
