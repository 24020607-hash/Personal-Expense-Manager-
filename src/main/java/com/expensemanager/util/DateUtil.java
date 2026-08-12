package com.expensemanager.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích xử lý/định dạng ngày tháng dùng chung trong ứng dụng.
 */
public class DateUtil {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateUtil() {
    }

    /**
     * @param date ngày cần định dạng
     * @return chuỗi dạng dd/MM/yyyy, hoặc chuỗi rỗng nếu date là null
     */
    public static String format(LocalDate date) {
        return date == null ? "" : date.format(DISPLAY_FORMAT);
    }

    /**
     * @return ngày hiện tại của hệ thống
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
}
