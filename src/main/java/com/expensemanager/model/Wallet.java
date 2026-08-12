package com.expensemanager.model;

import com.expensemanager.enums.WalletType;

/**
 * Lớp trừu tượng đại diện cho một ví/tài khoản.
 * Là lớp cha của CashWallet, BankAccount, EWallet (kế thừa + đa hình
 * thông qua phương thức withdraw() - mỗi loại ví trừ tiền theo cách khác nhau).
 */
public abstract class Wallet {

    private String name;
    private double balance;

    /**
     * Khởi tạo một ví.
     *
     * @param name    tên ví
     * @param balance số dư ban đầu, không được âm
     */
    public Wallet(String name, double balance) {
        this.name = name;
        setBalance(balance);
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gán số dư cho ví, có kiểm tra hợp lệ (đóng gói - không cho số dư âm).
     *
     * @param balance số dư mới
     * @throws IllegalArgumentException nếu balance nhỏ hơn 0
     */
    public void setBalance(double balance) {

        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative.");
        }

        this.balance = balance;
    }

    /**
     * Nạp tiền vào ví.
     *
     * @param amount số tiền nạp, phải lớn hơn 0
     * @throws IllegalArgumentException nếu amount không lớn hơn 0
     */
    public void deposit(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        balance += amount;
    }

    /**
     * Rút/chi tiêu từ ví. Mỗi loại ví con cài đặt khác nhau (đa hình) -
     * VD: BankAccount tính thêm phí giao dịch, CashWallet thì không.
     *
     * @param amount số tiền cần rút
     */
    public abstract void withdraw(double amount);

    /**
     * Trừ tiền trực tiếp, KHÔNG áp dụng phí giao dịch (dùng khi hoàn tác/rollback
     * một giao dịch Thu nhập đã cộng trước đó - khác với withdraw() dùng cho
     * chi tiêu thật, có thể bị tính phí tùy loại ví).
     *
     * @param amount số tiền cần trừ lại
     */
    public void deductWithoutFee(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        if (amount > getBalance()) {
            throw new IllegalArgumentException("Insufficient balance to reverse this transaction.");
        }

        setBalance(getBalance() - amount);
    }

    /**
     * @return loại ví (CASH/BANK/EWALLET), mỗi lớp con trả về giá trị khác nhau
     */
    public abstract WalletType getWalletType();

    @Override
    public String toString() {
        return name;
    }
}
