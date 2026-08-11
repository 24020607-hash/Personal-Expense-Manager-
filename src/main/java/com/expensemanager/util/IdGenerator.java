package com.expensemanager.util;

import com.expensemanager.model.Transaction;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sinh ID tự tăng cho giao dịch mới.
 * Gọi initFrom(...) một lần sau khi load dữ liệu để tránh trùng ID cũ.
 */
public class IdGenerator {

    private static final AtomicInteger counter = new AtomicInteger(0);

    private IdGenerator() {
    }

    /** Khởi tạo bộ đếm dựa trên ID lớn nhất đang có trong danh sách giao dịch đã load. */
    public static void initFrom(List<Transaction> transactions) {
        int maxId = 0;
        for (Transaction t : transactions) {
            if (t.getId() > maxId) {
                maxId = t.getId();
            }
        }
        counter.set(maxId);
    }

    public static int nextId() {
        return counter.incrementAndGet();
    }
}
