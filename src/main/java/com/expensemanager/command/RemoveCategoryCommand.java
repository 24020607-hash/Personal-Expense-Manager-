package com.expensemanager.command;

import com.expensemanager.model.Category;
import com.expensemanager.service.ExpenseManager;

/**
 * Hoàn tác thao tác "xóa danh mục": thêm lại danh mục vào danh sách.
 * An toàn vì removeCategory() chỉ cho xóa khi không còn giao dịch nào tham
 * chiếu đến danh mục đó.
 */
public class RemoveCategoryCommand implements Command {

    private final Category category;
    private final ExpenseManager manager;

    public RemoveCategoryCommand(Category category, ExpenseManager manager) {
        this.category = category;
        this.manager = manager;
    }

    @Override
    public void undo() {
        manager.getCategories().add(category);
    }

    @Override
    public String getDescription() {
        return "Xóa danh mục \"" + category.getName() + "\"";
    }
}
