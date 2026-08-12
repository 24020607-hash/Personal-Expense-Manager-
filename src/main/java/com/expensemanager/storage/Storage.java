package com.expensemanager.storage;

import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.Wallet;

import java.io.IOException;
import java.util.List;

/**
 * Trừu tượng hóa tầng lưu trữ dữ liệu. Cho phép thay đổi định dạng file
 * (CSV, JSON...) mà không ảnh hưởng đến phần còn lại của chương trình
 * (đa hình thông qua CsvStorage/JsonStorage).
 */
public interface Storage {

    /**
     * Ghi danh sách giao dịch ra file.
     *
     * @param transactions danh sách giao dịch cần lưu
     * @param path         đường dẫn file đích
     * @throws IOException nếu ghi file thất bại
     */
    void save(List<Transaction> transactions,
              String path) throws IOException;

    /**
     * Đọc danh sách giao dịch từ file.
     * existingWallets / existingCategories dùng để tra cứu lại đúng đối tượng
     * (theo tên) thay vì tạo mới đè mất dữ liệu (VD: số dư ví bị reset về 0).
     * Nếu không tìm thấy ví/danh mục tương ứng, một đối tượng mới sẽ được tạo
     * và thêm vào danh sách được truyền vào.
     *
     * @param path               đường dẫn file nguồn
     * @param existingWallets    danh sách ví hiện có, dùng để đối chiếu
     * @param existingCategories danh sách danh mục hiện có, dùng để đối chiếu
     * @return danh sách giao dịch đọc được từ file
     * @throws IOException nếu đọc file thất bại
     */
    List<Transaction> load(String path,
                            List<Wallet> existingWallets,
                            List<Category> existingCategories) throws IOException;
}
