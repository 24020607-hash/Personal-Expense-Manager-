# Personal Expense Manager

Ứng dụng quản lý chi tiêu cá nhân — Bài tập lớn môn Lập trình Hướng đối tượng.
Xây dựng bằng Java (JavaFX + Maven), hỗ trợ cả giao diện đồ họa và dòng lệnh.

## Công nghệ sử dụng

- Java 21
- JavaFX 21 (giao diện đồ họa)
- Maven
- JUnit 5 (unit test)
- Lưu trữ: CSV hoặc JSON (`Storage` interface, không phụ thuộc thư viện ngoài)

## Kiến trúc & Design Pattern

- **Kế thừa + Đa hình**: `Transaction` (abstract) → `Income`, `Expense` → `RecurringExpense`;
  `Wallet` (abstract) → `CashWallet`, `BankAccount`, `EWallet` (mỗi loại `withdraw()` khác nhau —
  VD: `BankAccount` trừ thêm phí giao dịch).
- **Trừu tượng hóa**: interface `Storage` với 2 cách cài đặt `CsvStorage` / `JsonStorage`.
- **Singleton**: `ExpenseManager` — đảm bảo toàn ứng dụng (cả GUI lẫn Console) dùng chung
  một bộ dữ liệu duy nhất.
- **Factory Method**: `TransactionFactory`, `WalletFactory` — tập trung logic khởi tạo đối tượng.
- **Đóng gói**: mọi thuộc tính private, validate qua setter (không cho số tiền/số dư âm).

## Cấu trúc thư mục

```
src/main/java/com/expensemanager/
├── enums/         TransactionType, WalletType, Period
├── model/         Transaction, Income, Expense, RecurringExpense,
│                  Wallet, CashWallet, BankAccount, EWallet, Category, Budget
├── factory/       TransactionFactory, WalletFactory
├── storage/       Storage (interface), CsvStorage, JsonStorage
├── service/       ExpenseManager (Singleton - toàn bộ logic nghiệp vụ)
├── controller/    DashboardController (xử lý sự kiện FXML)
├── view/          ConsoleView (giao diện dòng lệnh)
└── Main.java      Điểm khởi động (GUI mặc định, "console" để chạy dòng lệnh)

src/main/resources/com/expensemanager/view/
└── Dashboard.fxml Giao diện chính (GUI)

src/test/java/com/expensemanager/
├── model/         WalletTest, BudgetTest, CategoryTest
└── service/       ExpenseManagerTest

docs/
└── class-diagram.puml   Sơ đồ UML (mở bằng plugin PlantUML hoặc plantuml.com)
```

## Cách chạy

### Giao diện đồ họa (mặc định)
```
mvn clean javafx:run
```

### Giao diện dòng lệnh
Trong IntelliJ: Run Configuration của `Main` → thêm Program argument `console`.
Hoặc:
```
mvn clean compile exec:java -Dexec.mainClass=com.expensemanager.Main -Dexec.args=console
```
(hoặc build jar rồi chạy `java -jar ... console`)

Dữ liệu tự động lưu vào `data/transactions.csv` khi thoát và tự nạp lại khi mở app lần sau —
dùng chung cho cả 2 chế độ GUI/Console.

### Chạy Unit Test
```
mvn test
```

## Chức năng đã hoàn thiện

**Quản lý giao dịch**
- [x] Thêm / Sửa / Xóa / Tìm kiếm (theo danh mục, khoảng ngày, số tiền)
- [x] Giao dịch **tự động cập nhật số dư ví** (cộng khi Income, trừ khi Expense)
- [x] Rollback tự động nếu sửa giao dịch làm ví âm số dư
- [x] Giao dịch định kỳ (`RecurringExpense`)

**Quản lý danh mục & ví**
- [x] Thêm/xóa danh mục, thêm ví (Cash/Bank/EWallet), xem số dư từng ví
- [x] Chặn xóa danh mục/ví đang có giao dịch liên quan

**Thống kê**
- [x] Tổng thu / tổng chi / số dư theo tháng
- [x] Chi tiêu theo từng danh mục
- [x] Khoản chi lớn nhất / nhỏ nhất, danh mục tốn kém nhất trong tháng

**Ngân sách (Budget)**
- [x] Đặt hạn mức theo danh mục/tháng, cảnh báo khi vượt hạn mức

**Lưu trữ**
- [x] Đọc/ghi CSV và JSON (2 cài đặt của cùng interface `Storage`)

**Xử lý lỗi**
- [x] Không cho chi vượt quá số dư ví, số tiền/số dư âm, ngày sai định dạng,
      lựa chọn không hợp lệ → thông báo lỗi rõ ràng, không crash chương trình

## Ghi chú
File dữ liệu (`data/*.csv`, `data/*.json`) không đưa lên Git (đã có trong `.gitignore`) —
sẽ tự sinh khi chạy ứng dụng lần đầu.
