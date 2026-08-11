package com.expensemanager.storage;

import com.expensemanager.enums.TransactionType;
import com.expensemanager.model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CsvStorage implements Storage {

    @Override
    public void save(List<Transaction> transactions,
                      String path) throws IOException {

        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        BufferedWriter writer =
                new BufferedWriter(new FileWriter(path));

        writer.write("id,type,amount,date,note,category,wallet");
        writer.newLine();

        for (Transaction transaction : transactions) {

            writer.write(
                    transaction.getId() + ","
                            + transaction.getType() + ","
                            + transaction.getAmount() + ","
                            + transaction.getDate() + ","
                            + safe(transaction.getNote()) + ","
                            + transaction.getCategory().getName() + ","
                            + transaction.getWallet().getName()
            );

            writer.newLine();
        }

        writer.close();
    }

    @Override
    public List<Transaction> load(String path,
                                   List<Wallet> existingWallets,
                                   List<Category> existingCategories)
            throws IOException {

        List<Transaction> transactions = new ArrayList<>();

        File file = new File(path);

        if (!file.exists()) {
            return transactions;
        }

        BufferedReader reader =
                new BufferedReader(new FileReader(file));

        reader.readLine(); // bỏ qua dòng tiêu đề

        String line;

        while ((line = reader.readLine()) != null) {

            if (line.isBlank()) {
                continue;
            }

            String[] data = line.split(",", -1);

            int id = Integer.parseInt(data[0]);
            TransactionType type = TransactionType.valueOf(data[1]);
            double amount = Double.parseDouble(data[2]);
            LocalDate date = LocalDate.parse(data[3]);
            String note = data[4];
            String categoryName = data[5];
            String walletName = data[6];

            // Tìm lại đúng Category đã tồn tại theo tên, nếu chưa có thì tạo mới
            Category category = findOrCreateCategory(existingCategories, categoryName, type);

            // Tìm lại đúng Wallet đã tồn tại theo tên, để KHÔNG làm mất số dư hiện có
            Wallet wallet = findOrCreateWallet(existingWallets, walletName);

            Transaction transaction;

            if (type == TransactionType.INCOME) {
                transaction = new Income(id, amount, date, note, category, wallet, "Imported");
            } else {
                transaction = new Expense(id, amount, date, note, category, wallet, "Cash");
            }

            transactions.add(transaction);
        }

        reader.close();

        return transactions;
    }

    private Category findOrCreateCategory(List<Category> categories, String name, TransactionType type) {
        for (Category c : categories) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        Category newCategory = new Category(name, type);
        categories.add(newCategory);
        return newCategory;
    }

    private Wallet findOrCreateWallet(List<Wallet> wallets, String name) {
        for (Wallet w : wallets) {
            if (w.getName().equals(name)) {
                return w;
            }
        }
        // Chỉ tạo mới (balance = 0) nếu ví thực sự chưa tồn tại trong hệ thống
        Wallet newWallet = new CashWallet(name, 0);
        wallets.add(newWallet);
        return newWallet;
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }
}
