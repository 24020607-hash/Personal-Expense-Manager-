package com.expensemanager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử cho Wallet và các lớp con: deposit/withdraw, tính đóng gói
 * (không cho số dư âm), và tính đa hình của withdraw() giữa các loại ví.
 */
class WalletTest {

    private Wallet cashWallet;
    private Wallet bankAccount;

    @BeforeEach
    void setUp() {
        cashWallet = new CashWallet("Vi tien mat", 100_000);
        bankAccount = new BankAccount("Tai khoan chinh", 100_000, "Vietcombank", "0011001");
    }

    @Test
    void deposit_shouldIncreaseBalance() {
        cashWallet.deposit(50_000);
        assertEquals(150_000, cashWallet.getBalance());
    }

    @Test
    void deposit_negativeAmount_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> cashWallet.deposit(-1000));
    }

    @Test
    void withdraw_enoughBalance_shouldSucceed() {
        cashWallet.withdraw(50_000);
        assertEquals(50_000, cashWallet.getBalance());
    }

    @Test
    void withdraw_exceedBalance_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> cashWallet.withdraw(200_000));
    }

    @Test
    void bankAccount_withdraw_shouldChargeExtraFee() {
        // BankAccount tru them phi 1000 so voi CashWallet (dac trung tinh da hinh)
        bankAccount.withdraw(50_000);
        assertEquals(100_000 - 50_000 - 1000, bankAccount.getBalance());
    }

    @Test
    void bankAccount_withdraw_notEnoughForFee_shouldThrow() {
        Wallet almostEmpty = new BankAccount("Test", 1000, "ABC", "999");
        assertThrows(IllegalArgumentException.class, () -> almostEmpty.withdraw(500));
    }

    @Test
    void constructor_negativeBalance_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new CashWallet("X", -100));
    }
}
