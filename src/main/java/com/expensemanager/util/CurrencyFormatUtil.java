package com.expensemanager.util;

import java.text.DecimalFormat;

/**
 * Định dạng số tiền hiển thị theo kiểu VND (VD: 1,000,000 đ).
 */
public class CurrencyFormatUtil {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,###");

    private CurrencyFormatUtil() {
    }

    /**
     * @param amount số tiền cần định dạng
     * @return chuỗi đã định dạng, VD: "1,000,000 đ"
     */
    public static String format(double amount) {
        return FORMAT.format(amount) + " đ";
    }
}
