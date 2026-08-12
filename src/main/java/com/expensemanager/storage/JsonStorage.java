package com.expensemanager.storage;

import com.expensemanager.enums.TransactionType;
import com.expensemanager.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt Storage bằng JSON, KHÔNG dùng thư viện ngoài (Gson/Jackson) để tránh
 * phụ thuộc thêm - minh họa đa hình: cùng interface Storage nhưng định dạng file
 * hoàn toàn khác so với CsvStorage.
 * Định dạng: một mảng JSON các object transaction.
 */
public class JsonStorage implements Storage {

    @Override
    public void save(List<Transaction> transactions, String path) throws IOException {

        Path filePath = Paths.get(path);

        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < transactions.size(); i++) {

            Transaction t = transactions.get(i);

            sb.append("  {")
              .append("\"id\":").append(t.getId()).append(",")
              .append("\"type\":\"").append(t.getType()).append("\",")
              .append("\"amount\":").append(t.getAmount()).append(",")
              .append("\"date\":\"").append(t.getDate()).append("\",")
              .append("\"note\":\"").append(escape(t.getNote())).append("\",")
              .append("\"category\":\"").append(escape(t.getCategory().getName())).append("\",")
              .append("\"wallet\":\"").append(escape(t.getWallet().getName())).append("\"")
              .append("}");

            if (i < transactions.size() - 1) {
                sb.append(",");
            }

            sb.append("\n");
        }

        sb.append("]\n");

        Files.write(filePath, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<Transaction> load(String path,
                                   List<Wallet> existingWallets,
                                   List<Category> existingCategories) throws IOException {

        List<Transaction> transactions = new ArrayList<>();
        Path filePath = Paths.get(path);

        if (!Files.exists(filePath)) {
            return transactions;
        }

        String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8).trim();

        if (content.isEmpty() || content.equals("[]")) {
            return transactions;
        }

        // Bóc từng object { ... } trong mảng JSON (parser tối giản, đủ dùng cho
        // định dạng phẳng mà save() ở trên tạo ra - không xử lý JSON lồng nhau)
        content = content.substring(content.indexOf('[') + 1, content.lastIndexOf(']'));

        String[] rawObjects = content.split("\\},\\s*\\{");

        for (String raw : rawObjects) {

            String obj = raw.replace("{", "").replace("}", "").trim();

            if (obj.isEmpty()) continue;

            String[] fields = obj.split(",(?=\"\\w+\":)");

            int id = 0;
            TransactionType type = null;
            double amount = 0;
            LocalDate date = null;
            String note = "";
            String categoryName = "";
            String walletName = "";

            for (String field : fields) {

                String[] kv = field.split(":", 2);
                String key = kv[0].replace("\"", "").trim();
                String value = kv[1].trim().replaceAll("^\"|\"$", "");

                switch (key) {
                    case "id": id = Integer.parseInt(value); break;
                    case "type": type = TransactionType.valueOf(value); break;
                    case "amount": amount = Double.parseDouble(value); break;
                    case "date": date = LocalDate.parse(value); break;
                    case "note": note = unescape(value); break;
                    case "category": categoryName = unescape(value); break;
                    case "wallet": walletName = unescape(value); break;
                }
            }

            Category category = findOrCreateCategory(existingCategories, categoryName, type);
            Wallet wallet = findOrCreateWallet(existingWallets, walletName);

            Transaction transaction = (type == TransactionType.INCOME)
                    ? new Income(id, amount, date, note, category, wallet, "Imported")
                    : new Expense(id, amount, date, note, category, wallet, "Imported");

            transactions.add(transaction);
        }

        return transactions;
    }

    /**
     * Tìm Category đã tồn tại theo tên; nếu chưa có, tạo mới và thêm vào danh sách.
     *
     * @param categories danh sách danh mục hiện có
     * @param name       tên danh mục cần tìm
     * @param type       loại danh mục (dùng khi phải tạo mới)
     * @return danh mục đã tìm thấy hoặc vừa được tạo mới
     */
    private Category findOrCreateCategory(List<Category> categories, String name, TransactionType type) {

        for (Category c : categories) {
            if (c.getName().equals(name)) return c;
        }

        Category newCategory = new Category(name, type);
        categories.add(newCategory);
        return newCategory;
    }

    /**
     * Tìm Wallet đã tồn tại theo tên; nếu chưa có, tạo mới (số dư 0) và thêm vào danh sách.
     *
     * @param wallets danh sách ví hiện có
     * @param name    tên ví cần tìm
     * @return ví đã tìm thấy hoặc vừa được tạo mới
     */
    private Wallet findOrCreateWallet(List<Wallet> wallets, String name) {

        for (Wallet w : wallets) {
            if (w.getName().equals(name)) return w;
        }

        Wallet newWallet = new CashWallet(name, 0);
        wallets.add(newWallet);
        return newWallet;
    }

    /**
     * Thay dấu ngoặc kép trong chuỗi để không làm vỡ cấu trúc JSON.
     *
     * @param value chuỗi cần xử lý, có thể null
     * @return chuỗi an toàn để ghi vào JSON
     */
    private String escape(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }

    private String unescape(String value) {
        return value == null ? "" : value;
    }
}
