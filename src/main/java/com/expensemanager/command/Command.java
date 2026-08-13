package com.expensemanager.command;

/**
 * Command Pattern: trừu tượng hóa một thao tác có thể hoàn tác (undo).
 * Mỗi khi ExpenseManager thực hiện một thao tác thay đổi dữ liệu thành công
 * (thêm/xóa/sửa giao dịch, ví, danh mục), một Command tương ứng được đẩy vào
 * ngăn xếp lịch sử. undo() sẽ đảo ngược đúng thao tác đó.
 */
public interface Command {

    /**
     * Hoàn tác thao tác mà Command này đại diện.
     */
    void undo();

    /**
     * @return mô tả ngắn gọn thao tác, hiển thị cho người dùng (VD: "Xóa giao dịch #5")
     */
    String getDescription();
}
