package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    void sameNameAndType_shouldBeEqual() {
        Category c1 = new Category("An uong", TransactionType.EXPENSE);
        Category c2 = new Category("An uong", TransactionType.EXPENSE);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void differentType_shouldNotBeEqual() {
        Category c1 = new Category("Luong", TransactionType.INCOME);
        Category c2 = new Category("Luong", TransactionType.EXPENSE);
        assertNotEquals(c1, c2);
    }

    @Test
    void usedAsMapKey_shouldRetrieveCorrectly() {
        // Day chinh la bug da sua: truoc day 2 Category object cung ten van bi coi
        // la khac nhau trong Map, gay loi khong tra duoc Budget da dat.
        Map<Category, String> map = new HashMap<>();
        map.put(new Category("Di chuyen", TransactionType.EXPENSE), "Budget A");

        String result = map.get(new Category("Di chuyen", TransactionType.EXPENSE));
        assertEquals("Budget A", result);
    }
}
