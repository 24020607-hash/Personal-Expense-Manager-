package com.expensemanager.model;

import com.expensemanager.enums.TransactionType;

import java.util.Objects;

/**
 * Danh mục thu/chi (VD: Ăn uống, Lương, Di chuyển...).
 */
public class Category {

    private String name;
    private TransactionType type;

    /**
     * Khởi tạo một danh mục.
     *
     * @param name tên danh mục
     * @param type loại danh mục (Thu nhập/Chi tiêu)
     */
    public Category(String name, TransactionType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public TransactionType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String toString() {
        return name;
    }

    /**
     * Hai Category được coi là bằng nhau nếu trùng tên (không phân biệt hoa thường)
     * và cùng loại. Bắt buộc phải có để dùng Category làm key trong
     * Map<Category, Budget> một cách chính xác (nếu không, 2 object Category cùng
     * tên vẫn bị coi là khác nhau -> tra Budget sai/không ra).
     *
     * @param o đối tượng cần so sánh
     * @return true nếu cùng tên (không phân biệt hoa thường) và cùng loại
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Category)) return false;

        Category category = (Category) o;
        return name.equalsIgnoreCase(category.name) && type == category.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), type);
    }
}
