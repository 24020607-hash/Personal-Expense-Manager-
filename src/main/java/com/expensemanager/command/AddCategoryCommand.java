package com.expensemanager.command;

import com.expensemanager.model.Category;
import com.expensemanager.service.ExpenseManager;

/**
 * Hoàn tác thao tác "thêm danh mục": gỡ danh mục khỏi danh sách.
 */
public class AddCategoryCommand implements Command {

    private final Category category;
    private final ExpenseManager manager;

    public AddCategoryCommand(Category category, ExpenseManager manager) {
        this.category = category;
        this.manager = manager;
    }

    @Override
    public void undo() {
        manager.getCategories().remove(category);
    }

    @Override
    public String getDescription() {
        return "Thêm danh mục \"" + category.getName() + "\"";
    }
}
