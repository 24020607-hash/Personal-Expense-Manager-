package com.expensemanager.view;

import com.expensemanager.enums.Period;
import com.expensemanager.enums.TransactionType;
import com.expensemanager.enums.WalletType;
import com.expensemanager.factory.TransactionFactory;
import com.expensemanager.factory.WalletFactory;
import com.expensemanager.model.*;
import com.expensemanager.service.ExpenseManager;
import com.expensemanager.util.CurrencyFormatUtil;
import com.expensemanager.util.DateUtil;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleView {

    private final ExpenseManager manager = ExpenseManager.getInstance();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {

        System.out.println("Welcome to My Expense Manager!");

        boolean running = true;

        while (running) {

            printMenu();
            String input = scanner.nextLine().trim();

            switch (input) {
                case "0": running = false; break;
                case "1": addTransaction(); break;
                case "2": removeTransaction(); break;
                case "3": updateTransaction(); break;
                case "4": findTransaction(); break;
                case "5": displayAllTransactions(); break;
                case "6": manageCategory(); break;
                case "7": manageWallet(); break;
                case "8": monthlySummary(); break;
                case "9": setOrCheckBudget(); break;
                default:
                    System.out.println("Action is not supported");
            }
        }

        System.out.println("Bye!");
    }

    private void printMenu() {
        System.out.println("\n[0] Exit");
        System.out.println("[1] Add Transaction");
        System.out.println("[2] Remove Transaction");
        System.out.println("[3] Update Transaction");
        System.out.println("[4] Find Transaction");
        System.out.println("[5] Display All Transactions");
        System.out.println("[6] Manage Category");
        System.out.println("[7] Manage Wallet");
        System.out.println("[8] Monthly Summary");
        System.out.println("[9] Set / Check Budget");
        System.out.print("Choose an action: ");
    }

    private void addTransaction() {

        if (manager.getWallets().isEmpty()) {
            System.out.println("Loi: chua co vi nao. Vui long them vi truoc (menu 7).");
            return;
        }
        if (manager.getCategories().isEmpty()) {
            System.out.println("Loi: chua co danh muc nao. Vui long them danh muc truoc (menu 6).");
            return;
        }

        try {
            System.out.print("Loai (1-Income / 2-Expense): ");
            String typeChoice = scanner.nextLine().trim();
            TransactionType type = typeChoice.equals("1") ? TransactionType.INCOME : TransactionType.EXPENSE;

            double amount = readPositiveDouble("So tien: ");
            LocalDate date = readDate("Ngay (yyyy-MM-dd): ");

            Category category = chooseCategory();
            if (category == null) return;

            Wallet wallet = chooseWallet();
            if (wallet == null) return;

            System.out.print("Ghi chu: ");
            String note = scanner.nextLine();

            Transaction transaction = TransactionFactory.create(type, amount, date, note, category, wallet);
            manager.addTransaction(transaction);

            System.out.println("Da them giao dich thanh cong. (ID = " + transaction.getId() + ")");

        } catch (IllegalArgumentException e) {
            System.out.println("Loi: " + e.getMessage());
        }
    }

    private void removeTransaction() {
        int id = readInt("Nhap ID giao dich can xoa: ");
        boolean removed = manager.removeTransaction(id);
        System.out.println(removed ? "Da xoa giao dich." : "Khong tim thay giao dich co ID = " + id);
    }

    private void updateTransaction() {

        int id = readInt("Nhap ID giao dich can sua: ");
        Transaction transaction = manager.findTransactionById(id);

        if (transaction == null) {
            System.out.println("Khong tim thay giao dich co ID = " + id);
            return;
        }

        try {
            double amount = readPositiveDouble("So tien moi (hien tai " + transaction.getAmount() + "): ");
            LocalDate date = readDate("Ngay moi (yyyy-MM-dd): ");

            Category category = chooseCategory();
            if (category == null) return;

            Wallet wallet = chooseWallet();
            if (wallet == null) return;

            System.out.print("Ghi chu moi: ");
            String note = scanner.nextLine();

            manager.updateTransaction(id, amount, date, note, category, wallet);
            System.out.println("Da cap nhat giao dich.");

        } catch (IllegalArgumentException e) {
            System.out.println("Loi: " + e.getMessage() + " (giao dich giu nguyen trang thai cu)");
        }
    }

    private void findTransaction() {

        System.out.println("Tim theo: [1] Danh muc  [2] Khoang ngay  [3] So tien chinh xac  [4] Bo qua, hien het");
        String choice = scanner.nextLine().trim();

        Category category = null;
        LocalDate from = null, to = null;
        Double amount = null;

        switch (choice) {
            case "1":
                category = chooseCategory();
                break;
            case "2":
                from = readDate("Tu ngay (yyyy-MM-dd): ");
                to = readDate("Den ngay (yyyy-MM-dd): ");
                break;
            case "3":
                amount = readPositiveDouble("So tien: ");
                break;
            default:
                break;
        }

        List<Transaction> results = manager.findTransactions(category, from, to, amount);
        printTransactionTable(results);
    }

    private void displayAllTransactions() {
        printTransactionTable(manager.getTransactions());
    }

    private void printTransactionTable(List<Transaction> list) {

        if (list.isEmpty()) {
            System.out.println("(Khong co giao dich nao)");
            return;
        }

        System.out.printf("%-5s %-8s %-12s %-15s %-15s %-15s %s%n",
                "ID", "Loai", "Ngay", "So tien", "Danh muc", "Vi", "Ghi chu");

        for (Transaction t : list) {
            System.out.printf("%-5d %-8s %-12s %-15s %-15s %-15s %s%n",
                    t.getId(),
                    t.getType(),
                    DateUtil.format(t.getDate()),
                    CurrencyFormatUtil.format(t.getAmount()),
                    t.getCategory().getName(),
                    t.getWallet().getName(),
                    t.getNote() == null ? "" : t.getNote());
        }
    }

    private void manageCategory() {

        System.out.println("[1] Them danh muc  [2] Xoa danh muc  [3] Danh sach danh muc");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.out.print("Ten danh muc: ");
                String name = scanner.nextLine().trim();
                System.out.print("Loai (1-Income / 2-Expense): ");
                TransactionType type = scanner.nextLine().trim().equals("1")
                        ? TransactionType.INCOME : TransactionType.EXPENSE;
                try {
                    manager.addCategory(new Category(name, type));
                    System.out.println("Da them danh muc.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Loi: " + e.getMessage());
                }
                break;

            case "2":
                System.out.print("Ten danh muc can xoa: ");
                String delName = scanner.nextLine().trim();
                try {
                    boolean removed = manager.removeCategory(delName);
                    System.out.println(removed ? "Da xoa." : "Khong tim thay danh muc.");
                } catch (IllegalStateException e) {
                    System.out.println("Loi: " + e.getMessage());
                }
                break;

            case "3":
                for (Category c : manager.getCategories()) {
                    System.out.println("- " + c.getName() + " (" + c.getType() + ")");
                }
                break;

            default:
                System.out.println("Action is not supported");
        }
    }

    private void manageWallet() {

        System.out.println("[1] Them vi  [2] Xem so du tung vi");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {

            System.out.print("Loai vi (1-Cash / 2-Bank / 3-EWallet): ");
            String typeChoice = scanner.nextLine().trim();
            WalletType type = typeChoice.equals("2") ? WalletType.BANK
                    : typeChoice.equals("3") ? WalletType.EWALLET : WalletType.CASH;

            System.out.print("Ten vi: ");
            String name = scanner.nextLine().trim();

            double balance = readPositiveOrZeroDouble("So du ban dau: ");

            String extra1 = "", extra2 = "";
            if (type == WalletType.BANK) {
                System.out.print("Ten ngan hang: ");
                extra1 = scanner.nextLine();
                System.out.print("So tai khoan: ");
                extra2 = scanner.nextLine();
            } else if (type == WalletType.EWALLET) {
                System.out.print("Nha cung cap (Momo/ZaloPay...): ");
                extra1 = scanner.nextLine();
            }

            try {
                Wallet wallet = WalletFactory.create(type, name, balance, extra1, extra2);
                manager.addWallet(wallet);
                System.out.println("Da them vi.");
            } catch (IllegalArgumentException e) {
                System.out.println("Loi: " + e.getMessage());
            }

        } else if (choice.equals("2")) {
            for (Wallet w : manager.getWallets()) {
                System.out.println("- " + w.getName() + " (" + w.getWalletType() + "): "
                        + CurrencyFormatUtil.format(w.getBalance()));
            }
        } else {
            System.out.println("Action is not supported");
        }
    }

    private void monthlySummary() {

        YearMonth month = readYearMonth();

        double income = manager.getMonthlyIncome(month);
        double expense = manager.getMonthlyExpense(month);
        double balance = income - expense;

        System.out.println("Tong thu: " + CurrencyFormatUtil.format(income));
        System.out.println("Tong chi: " + CurrencyFormatUtil.format(expense));
        System.out.println("So du: " + CurrencyFormatUtil.format(balance));

        System.out.println("\nChi tieu theo danh muc:");
        Map<Category, Double> byCategory = manager.statisticsByCategory(month);
        for (Map.Entry<Category, Double> entry : byCategory.entrySet()) {
            System.out.println("  - " + entry.getKey().getName() + ": "
                    + CurrencyFormatUtil.format(entry.getValue()));
        }

        Optional<Transaction> largest = manager.getLargestExpense(month);
        Optional<Transaction> smallest = manager.getSmallestExpense(month);
        largest.ifPresent(t -> System.out.println("\nKhoan chi lon nhat: "
                + CurrencyFormatUtil.format(t.getAmount()) + " (" + t.getCategory().getName() + ")"));
        smallest.ifPresent(t -> System.out.println("Khoan chi nho nhat: "
                + CurrencyFormatUtil.format(t.getAmount()) + " (" + t.getCategory().getName() + ")"));

        manager.getTopExpensiveCategory(month).ifPresent(entry ->
                System.out.println("Danh muc ton kem nhat: " + entry.getKey().getName()
                        + " (" + CurrencyFormatUtil.format(entry.getValue()) + ")"));
    }

    private void setOrCheckBudget() {

        System.out.println("[1] Dat han muc  [2] Kiem tra vuot han muc");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {

            Category category = chooseCategory();
            if (category == null) return;

            double limit = readPositiveDouble("Han muc (VND/thang): ");

            try {
                manager.setBudget(new Budget(category, limit, Period.MONTHLY));
                System.out.println("Da dat han muc.");
            } catch (IllegalArgumentException e) {
                System.out.println("Loi: " + e.getMessage());
            }

        } else if (choice.equals("2")) {

            YearMonth month = readYearMonth();

            for (Map.Entry<Category, Budget> entry : manager.getBudgets().entrySet()) {
                Category category = entry.getKey();
                double spent = manager.getSpentByCategory(category, month);
                boolean exceeded = manager.isBudgetExceeded(category, month);

                System.out.println("- " + category.getName() + ": da chi "
                        + CurrencyFormatUtil.format(spent) + " / han muc "
                        + CurrencyFormatUtil.format(entry.getValue().getLimit())
                        + (exceeded ? "  >>> VUOT HAN MUC!" : ""));
            }

        } else {
            System.out.println("Action is not supported");
        }
    }

    private Category chooseCategory() {
        if (manager.getCategories().isEmpty()) {
            System.out.println("Chua co danh muc nao.");
            return null;
        }
        List<Category> categories = manager.getCategories();
        for (int i = 0; i < categories.size(); i++) {
            System.out.println("[" + i + "] " + categories.get(i).getName()
                    + " (" + categories.get(i).getType() + ")");
        }
        int index = readInt("Chon danh muc (so thu tu): ");
        if (index < 0 || index >= categories.size()) {
            System.out.println("Lua chon khong hop le.");
            return null;
        }
        return categories.get(index);
    }

    private Wallet chooseWallet() {
        if (manager.getWallets().isEmpty()) {
            System.out.println("Chua co vi nao.");
            return null;
        }
        List<Wallet> wallets = manager.getWallets();
        for (int i = 0; i < wallets.size(); i++) {
            System.out.println("[" + i + "] " + wallets.get(i).getName()
                    + " (" + CurrencyFormatUtil.format(wallets.get(i).getBalance()) + ")");
        }
        int index = readInt("Chon vi (so thu tu): ");
        if (index < 0 || index >= wallets.size()) {
            System.out.println("Lua chon khong hop le.");
            return null;
        }
        return wallets.get(index);
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value <= 0) {
                    System.out.println("So tien phai lon hon 0, nhap lai.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Gia tri khong hop le, nhap lai.");
            }
        }
    }

    private double readPositiveOrZeroDouble(String prompt) {
        System.out.print(prompt);
        try {
            double value = Double.parseDouble(scanner.nextLine().trim());
            return Math.max(value, 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                System.out.println("Ngay sai dinh dang (dung: yyyy-MM-dd), nhap lai.");
            }
        }
    }

    private YearMonth readYearMonth() {
        while (true) {
            System.out.print("Thang can xem (yyyy-MM), Enter de lay thang hien tai: ");
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                return YearMonth.now();
            }
            try {
                return YearMonth.parse(text);
            } catch (DateTimeParseException e) {
                System.out.println("Sai dinh dang (dung: yyyy-MM), nhap lai.");
            }
        }
    }
}
