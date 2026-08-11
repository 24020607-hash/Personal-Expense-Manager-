package com.expensemanager.util;

import java.text.DecimalFormat;

/**
 * Định dạng số tiền hiển thị theo kiểu VND (VD: 1,000,000 đ).
 */
public class CurrencyFormatUtil {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,###");

    private CurrencyFormatUtil() {
    }

    public static String format(double amount) {
        return FORMAT.format(amount) + " đ";
    }
}
