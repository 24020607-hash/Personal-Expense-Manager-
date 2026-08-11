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

    public static String format(LocalDate date) {
        return date == null ? "" : date.format(DISPLAY_FORMAT);
    }

    public static LocalDate today() {
        return LocalDate.now();
    }
}
