package com.expensemanager.storage;

import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.Wallet;

import java.io.IOException;
import java.util.List;

public interface Storage {

    void save(List<Transaction> transactions,
              String path) throws IOException;

    /**
     * Đọc danh sách giao dịch từ file.
     * existingWallets / existingCategories dùng để tra cứu lại đúng đối tượng
     * (theo tên) thay vì tạo mới đè mất dữ liệu (VD: số dư ví bị reset về 0).
     * Nếu không tìm thấy ví/danh mục tương ứng, một đối tượng mới sẽ được tạo
     * và thêm vào danh sách được truyền vào.
     */
    List<Transaction> load(String path,
                            List<Wallet> existingWallets,
                            List<Category> existingCategories) throws IOException;
}
