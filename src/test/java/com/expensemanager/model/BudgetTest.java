package com.expensemanager.model;

import com.expensemanager.enums.Period;
import com.expensemanager.enums.TransactionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử cho Budget: kiểm tra vượt hạn mức và ràng buộc đóng gói (hạn mức phải dương).
 */
class BudgetTest {

    @Test
    void isExceeded_spentLessThanLimit_shouldReturnFalse() {
        Category food = new Category("An uong", TransactionType.EXPENSE);
        Budget budget = new Budget(food, 1_000_000, Period.MONTHLY);
        assertFalse(budget.isExceeded(500_000));
    }

    @Test
    void isExceeded_spentMoreThanLimit_shouldReturnTrue() {
        Category food = new Category("An uong", TransactionType.EXPENSE);
        Budget budget = new Budget(food, 1_000_000, Period.MONTHLY);
        assertTrue(budget.isExceeded(1_500_000));
    }

    @Test
    void constructor_negativeLimit_shouldThrow() {
        Category food = new Category("An uong", TransactionType.EXPENSE);
        assertThrows(IllegalArgumentException.class, () -> new Budget(food, -100, Period.MONTHLY));
    }
}
