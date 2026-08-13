package com.expensemanager.controller;

import com.expensemanager.enums.Period;
import com.expensemanager.enums.TransactionType;
import com.expensemanager.enums.WalletType;
import com.expensemanager.factory.TransactionFactory;
import com.expensemanager.factory.WalletFactory;
import com.expensemanager.model.*;
import com.expensemanager.service.ExpenseManager;
import com.expensemanager.util.CurrencyFormatUtil;
import com.expensemanager.util.DateUtil;
import com.expensemanager.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho Dashboard.fxml - tầng hiển thị (View), toàn bộ logic nghiệp vụ
 * đều gọi qua ExpenseManager (Singleton), không xử lý trực tiếp ở đây.
 */
public class DashboardController {

    @FXML private TableView<Transaction> tblTransactions;
    @FXML private TableColumn<Transaction, String> colId;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colAmount;
    @FXML private TableColumn<Transaction, String> colWallet;
    @FXML private TableColumn<Transaction, String> colNote;

    @FXML private Label lblIncome;
    @FXML private Label lblExpense;
    @FXML private Label lblBalance;

    private final ExpenseManager manager = ExpenseManager.getInstance();

    @FXML
    public void initialize() {
        setupTableColumns();
        refreshTable(manager.getTransactions());
        refreshSummary();
    }

    /**
     * Gán cách lấy dữ liệu hiển thị cho từng cột của bảng giao dịch.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(DateUtil.format(data.getValue().getDate())));
        colType.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getType().getLabelSafe()));
        colCategory.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory().getName()));
        colAmount.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(CurrencyFormatUtil.format(data.getValue().getAmount())));
        colWallet.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getWallet().getName()));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
    }

    /**
     * Cập nhật danh sách hiển thị trên bảng.
     *
     * @param list danh sách giao dịch cần hiển thị
     */
    private void refreshTable(List<Transaction> list) {
        ObservableList<Transaction> data = FXCollections.observableArrayList(list);
        tblTransactions.setItems(data);
    }

    /**
     * Cập nhật 3 thẻ tổng quan (thu nhập/chi tiêu/số dư) của tháng hiện tại.
     */
    private void refreshSummary() {
        YearMonth now = YearMonth.now();
        double income = manager.getMonthlyIncome(now);
        double expense = manager.getMonthlyExpense(now);
        double balance = income - expense;

        lblIncome.setText("Thu nhập tháng này: " + CurrencyFormatUtil.format(income));
        lblExpense.setText("Chi tiêu tháng này: " + CurrencyFormatUtil.format(expense));
        lblBalance.setText("Số dư: " + CurrencyFormatUtil.format(balance));
    }

    /**
     * Hiển thị lại toàn bộ giao dịch (bỏ bộ lọc tìm kiếm nếu có).
     */
    @FXML
    private void handleShowAll() {
        refreshTable(manager.getTransactions());
    }

    // ================== THÊM / SỬA GIAO DỊCH (dùng chung 1 form) ==================

    /**
     * Mở form thêm giao dịch mới.
     */
    @FXML
    private void handleAddTransaction() {
        openTransactionForm(null);
    }

    /**
     * Mở form sửa giao dịch đang được chọn trên bảng.
     */
    @FXML
    private void handleEditTransaction() {
        Transaction selected = tblTransactions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn một giao dịch để sửa.");
            return;
        }
        openTransactionForm(selected);
    }

    /**
     * Mở dialog dùng chung cho cả Thêm và Sửa giao dịch.
     *
     * @param editing giao dịch cần sửa, hoặc null nếu đang thêm mới
     */
    private void openTransactionForm(Transaction editing) {

        if (manager.getWallets().isEmpty()) {
            showWarning("Bạn cần thêm ít nhất 1 Ví trước (nút 'Quản lý ví').");
            return;
        }
        if (manager.getCategories().isEmpty()) {
            showWarning("Bạn cần thêm ít nhất 1 Danh mục trước (nút 'Quản lý danh mục').");
            return;
        }

        boolean isEdit = editing != null;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Sửa giao dịch" : "Thêm giao dịch");
        applyStylesheet(dialog);

        ButtonType okButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<TransactionType> cbType = new ComboBox<>(
                FXCollections.observableArrayList(TransactionType.values()));
        cbType.setDisable(isEdit); // không đổi loại Thu/Chi khi sửa, tránh phức tạp hoá logic ví

        TextField txtAmount = new TextField();
        DatePicker dpDate = new DatePicker(LocalDate.now());

        // Danh mục hiển thị được lọc theo đúng loại (Thu nhập/Chi tiêu) đang chọn ở cbType,
        // để không thể chọn nhầm danh mục Chi tiêu cho giao dịch Thu nhập (hoặc ngược lại).
        ComboBox<Category> cbCategory = new ComboBox<>();

        Runnable refreshCategoryOptions = () -> {
            TransactionType selectedType = cbType.getValue();
            Category previouslySelected = cbCategory.getValue();

            List<Category> filtered = manager.getCategories().stream()
                    .filter(c -> c.getType() == selectedType)
                    .collect(java.util.stream.Collectors.toList());

            cbCategory.setItems(FXCollections.observableArrayList(filtered));

            if (filtered.contains(previouslySelected)) {
                cbCategory.getSelectionModel().select(previouslySelected);
            } else if (!filtered.isEmpty()) {
                cbCategory.getSelectionModel().selectFirst();
            }
        };

        cbType.valueProperty().addListener((obs, oldType, newType) -> refreshCategoryOptions.run());

        ComboBox<Wallet> cbWallet = new ComboBox<>(
                FXCollections.observableArrayList(manager.getWallets()));

        TextField txtNote = new TextField();
        txtNote.setPromptText("Ghi chú (không bắt buộc)");

        if (isEdit) {
            cbType.getSelectionModel().select(editing.getType());
            txtAmount.setText(String.valueOf((long) editing.getAmount()));
            dpDate.setValue(editing.getDate());
            refreshCategoryOptions.run();
            cbCategory.getSelectionModel().select(editing.getCategory());
            cbWallet.getSelectionModel().select(editing.getWallet());
            txtNote.setText(editing.getNote());
        } else {
            cbType.getSelectionModel().selectFirst();
            refreshCategoryOptions.run();
            cbWallet.getSelectionModel().selectFirst();
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Loại:"), cbType);
        grid.addRow(1, new Label("Số tiền:"), txtAmount);
        grid.addRow(2, new Label("Ngày:"), dpDate);
        grid.addRow(3, new Label("Danh mục:"), cbCategory);
        grid.addRow(4, new Label("Ví:"), cbWallet);
        grid.addRow(5, new Label("Ghi chú:"), txtNote);

        dialog.getDialogPane().setContent(grid);

        // Chặn dialog tự đóng nếu lưu thất bại (để người dùng sửa lại)
        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            // Chặn trường hợp bấm Lưu / nhấn Enter nhiều lần liên tiếp gây tạo
            // trùng giao dịch (double-submit) trước khi dialog kịp đóng lại.
            if (okButton.isDisabled()) {
                event.consume();
                return;
            }

            if (!ValidationUtil.isPositiveNumber(txtAmount.getText())) {
                showWarning("Số tiền không hợp lệ, phải là số lớn hơn 0.");
                event.consume();
                return;
            }
            if (dpDate.getValue() == null || cbCategory.getValue() == null || cbWallet.getValue() == null) {
                showWarning("Vui lòng điền đầy đủ thông tin.");
                event.consume();
                return;
            }

            double amount = Double.parseDouble(txtAmount.getText().trim());

            try {
                if (isEdit) {
                    manager.updateTransaction(editing.getId(), amount, dpDate.getValue(),
                            txtNote.getText(), cbCategory.getValue(), cbWallet.getValue());
                } else {
                    Transaction transaction = TransactionFactory.create(
                            cbType.getValue(), amount, dpDate.getValue(),
                            txtNote.getText(), cbCategory.getValue(), cbWallet.getValue());
                    manager.addTransaction(transaction);
                }
                okButton.setDisable(true); // lưu thành công -> khóa nút, chặn bấm thêm lần nữa
            } catch (IllegalArgumentException e) {
                showWarning(e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
        refreshTable(manager.getTransactions());
        refreshSummary();
    }

    /**
     * Xóa giao dịch đang được chọn trên bảng, sau khi xác nhận.
     */
    @FXML
    private void handleDeleteTransaction() {
        Transaction selected = tblTransactions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Vui lòng chọn một giao dịch để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa giao dịch này? (số tiền sẽ được hoàn lại vào ví)", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            manager.removeTransaction(selected.getId());
            refreshTable(manager.getTransactions());
            refreshSummary();
        }
    }

    // ================== TÌM KIẾM ==================

    /**
     * Mở dialog tìm kiếm giao dịch theo danh mục/khoảng ngày/số tiền.
     */
    @FXML
    private void handleFindTransaction() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Tìm kiếm giao dịch");
        applyStylesheet(dialog);

        ButtonType searchButtonType = new ButtonType("Tìm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);

        ComboBox<Category> cbCategory = new ComboBox<>(
                FXCollections.observableArrayList(manager.getCategories()));
        cbCategory.setPromptText("(Bỏ qua nếu không lọc theo danh mục)");

        DatePicker dpFrom = new DatePicker();
        DatePicker dpTo = new DatePicker();

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("(Bỏ qua nếu không lọc theo số tiền)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Danh mục:"), cbCategory);
        grid.addRow(1, new Label("Từ ngày:"), dpFrom);
        grid.addRow(2, new Label("Đến ngày:"), dpTo);
        grid.addRow(3, new Label("Số tiền:"), txtAmount);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == searchButtonType) {

                Double amount = null;
                if (ValidationUtil.isNotEmpty(txtAmount.getText())) {
                    try {
                        amount = Double.parseDouble(txtAmount.getText().trim());
                    } catch (NumberFormatException e) {
                        showWarning("Số tiền lọc không hợp lệ, bỏ qua điều kiện này.");
                    }
                }

                List<Transaction> results = manager.findTransactions(
                        cbCategory.getValue(), dpFrom.getValue(), dpTo.getValue(), amount);

                refreshTable(results);

                if (results.isEmpty()) {
                    showInfo("Không tìm thấy giao dịch nào phù hợp.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // ================== QUẢN LÝ VÍ ==================

    /**
     * Mở dialog quản lý ví: xem danh sách, xóa, hoặc thêm ví mới.
     */
    @FXML
    private void handleManageWallets() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Quản lý ví");
        applyStylesheet(dialog);

        ButtonType closeButtonType = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        ListView<Wallet> listView = new ListView<>(
                FXCollections.observableArrayList(manager.getWallets()));
        listView.setPrefHeight(160);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Wallet w, boolean empty) {
                super.updateItem(w, empty);
                setText(empty || w == null ? null :
                        w.getName() + " (" + w.getWalletType() + "): " + CurrencyFormatUtil.format(w.getBalance()));
            }
        });

        Button btnDelete = new Button("Xóa ví đã chọn");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> {
            Wallet selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Vui lòng chọn ví cần xóa.");
                return;
            }
            try {
                manager.removeWallet(selected.getName());
                listView.getItems().setAll(manager.getWallets());
            } catch (IllegalStateException ex) {
                showWarning(ex.getMessage());
            }
        });

        Separator separator = new Separator();

        ComboBox<WalletType> cbType = new ComboBox<>(FXCollections.observableArrayList(WalletType.values()));
        cbType.getSelectionModel().selectFirst();

        TextField txtName = new TextField();
        txtName.setPromptText("Tên ví, VD: Ví chính");

        TextField txtBalance = new TextField();
        txtBalance.setPromptText("Số dư ban đầu");

        TextField txtExtra1 = new TextField();
        txtExtra1.setPromptText("Nhà cung cấp / Tên ngân hàng (nếu có)");

        TextField txtExtra2 = new TextField();
        txtExtra2.setPromptText("Số tài khoản (chỉ dùng cho Bank)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Loại ví:"), cbType);
        grid.addRow(1, new Label("Tên ví:"), txtName);
        grid.addRow(2, new Label("Số dư:"), txtBalance);
        grid.addRow(3, new Label("Thông tin thêm 1:"), txtExtra1);
        grid.addRow(4, new Label("Thông tin thêm 2:"), txtExtra2);

        Button btnAdd = new Button("Thêm ví mới");
        btnAdd.getStyleClass().add("sidebar-btn-primary");
        btnAdd.setStyle("-fx-text-fill: #143028;");
        btnAdd.setOnAction(e -> {

            if (!ValidationUtil.isNotEmpty(txtName.getText())) {
                showWarning("Tên ví không được để trống.");
                return;
            }
            double balance = 0;
            if (ValidationUtil.isNotEmpty(txtBalance.getText())) {
                try {
                    balance = Double.parseDouble(txtBalance.getText().trim());
                } catch (NumberFormatException ex) {
                    showWarning("Số dư không hợp lệ.");
                    return;
                }
            }
            try {
                Wallet wallet = WalletFactory.create(
                        cbType.getValue(), txtName.getText().trim(), balance,
                        txtExtra1.getText(), txtExtra2.getText());
                manager.addWallet(wallet);
                listView.getItems().setAll(manager.getWallets());
                txtName.clear();
                txtBalance.clear();
                txtExtra1.clear();
                txtExtra2.clear();
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            }
        });

        VBox content = new VBox(12,
                new Label("Danh sách ví hiện có:"), listView, btnDelete,
                separator, new Label("Thêm ví mới:"), grid, btnAdd);
        content.setPadding(new Insets(15));
        content.setPrefWidth(420);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();

        refreshTable(manager.getTransactions());
        refreshSummary();
    }

    // ================== QUẢN LÝ DANH MỤC ==================

    /**
     * Mở dialog quản lý danh mục: xem danh sách, xóa, hoặc thêm danh mục mới.
     */
    @FXML
    private void handleManageCategories() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Quản lý danh mục");
        applyStylesheet(dialog);

        ButtonType closeButtonType = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        ListView<Category> listView = new ListView<>(
                FXCollections.observableArrayList(manager.getCategories()));
        listView.setPrefHeight(160);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getName() + " (" + c.getType() + ")");
            }
        });

        Button btnDelete = new Button("Xóa danh mục đã chọn");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> {
            Category selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Vui lòng chọn danh mục cần xóa.");
                return;
            }
            try {
                manager.removeCategory(selected.getName());
                listView.getItems().setAll(manager.getCategories());
            } catch (IllegalStateException ex) {
                showWarning(ex.getMessage());
            }
        });

        TextField txtName = new TextField();
        txtName.setPromptText("VD: Ăn uống, Lương...");

        ComboBox<TransactionType> cbType = new ComboBox<>(
                FXCollections.observableArrayList(TransactionType.values()));
        cbType.getSelectionModel().selectFirst();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Tên danh mục:"), txtName);
        grid.addRow(1, new Label("Loại:"), cbType);

        Button btnAdd = new Button("Thêm danh mục mới");
        btnAdd.getStyleClass().add("sidebar-btn-primary");
        btnAdd.setStyle("-fx-text-fill: #143028;");
        btnAdd.setOnAction(e -> {
            if (!ValidationUtil.isNotEmpty(txtName.getText())) {
                showWarning("Tên danh mục không được để trống.");
                return;
            }
            try {
                manager.addCategory(new Category(txtName.getText().trim(), cbType.getValue()));
                listView.getItems().setAll(manager.getCategories());
                txtName.clear();
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            }
        });

        VBox content = new VBox(12,
                new Label("Danh sách danh mục hiện có:"), listView, btnDelete,
                new Separator(), new Label("Thêm danh mục mới:"), grid, btnAdd);
        content.setPadding(new Insets(15));
        content.setPrefWidth(380);

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    // ================== NGÂN SÁCH ==================

    /**
     * Mở dialog đặt hạn mức ngân sách theo danh mục và xem tình trạng vượt hạn mức.
     */
    @FXML
    private void handleManageBudget() {

        if (manager.getCategories().stream().noneMatch(c -> c.getType() == TransactionType.EXPENSE)) {
            showWarning("Cần có ít nhất 1 danh mục Chi tiêu trước khi đặt ngân sách " +
                    "(ngân sách chỉ áp dụng cho danh mục Chi tiêu).");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ngân sách theo danh mục (tháng hiện tại)");
        applyStylesheet(dialog);

        ButtonType setButtonType = new ButtonType("Đặt hạn mức", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(setButtonType, ButtonType.CLOSE);

        // Chỉ hiện danh mục Chi tiêu - đặt ngân sách cho danh mục Thu nhập là vô nghĩa
        // vì getSpentByCategory() chỉ tính tổng các giao dịch Chi tiêu.
        List<Category> expenseCategories = manager.getCategories().stream()
                .filter(c -> c.getType() == TransactionType.EXPENSE)
                .collect(java.util.stream.Collectors.toList());

        ComboBox<Category> cbCategory = new ComboBox<>(
                FXCollections.observableArrayList(expenseCategories));
        cbCategory.getSelectionModel().selectFirst();

        TextField txtLimit = new TextField();
        txtLimit.setPromptText("Hạn mức VND/tháng");

        VBox statusBox = new VBox(4);
        rebuildBudgetStatus(statusBox);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Danh mục:"), cbCategory);
        grid.addRow(1, new Label("Hạn mức:"), txtLimit);

        VBox content = new VBox(15, grid, new Separator(), new Label("Tình trạng ngân sách:"), statusBox);
        dialog.getDialogPane().setContent(content);

        Button setButton = (Button) dialog.getDialogPane().lookupButton(setButtonType);
        setButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            if (!ValidationUtil.isPositiveNumber(txtLimit.getText())) {
                showWarning("Hạn mức không hợp lệ.");
                event.consume();
                return;
            }
            double limit = Double.parseDouble(txtLimit.getText().trim());
            manager.setBudget(new Budget(cbCategory.getValue(), limit, Period.MONTHLY));
            rebuildBudgetStatus(statusBox);
            event.consume(); // giữ dialog mở để xem trạng thái cập nhật
        });

        dialog.showAndWait();
    }

    /**
     * Vẽ lại danh sách trạng thái ngân sách (đã chi / hạn mức / có vượt hay không).
     *
     * @param statusBox khung chứa các dòng trạng thái, sẽ bị xóa và vẽ lại từ đầu
     */
    private void rebuildBudgetStatus(VBox statusBox) {
        statusBox.getChildren().clear();
        YearMonth now = YearMonth.now();

        if (manager.getBudgets().isEmpty()) {
            statusBox.getChildren().add(new Label("(chưa đặt ngân sách nào)"));
            return;
        }

        for (Map.Entry<Category, Budget> entry : manager.getBudgets().entrySet()) {
            Category category = entry.getKey();
            double spent = manager.getSpentByCategory(category, now);
            boolean exceeded = manager.isBudgetExceeded(category, now);

            String text = category.getName() + ": đã chi " + CurrencyFormatUtil.format(spent)
                    + " / hạn mức " + CurrencyFormatUtil.format(entry.getValue().getLimit());

            Label label = new Label(text + (exceeded ? "  ⚠ VƯỢT HẠN MỨC!" : ""));
            if (exceeded) {
                label.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            }
            statusBox.getChildren().add(label);
        }
    }

    // ================== THỐNG KÊ ==================

    /**
     * Hiển thị thống kê chi tiêu tháng hiện tại: theo danh mục, khoản chi lớn
     * nhất/nhỏ nhất, danh mục tốn kém nhất.
     */
    @FXML
    private void handleShowStatistics() {

        YearMonth now = YearMonth.now();
        Map<Category, Double> byCategory = manager.statisticsByCategory(now);

        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        content.getChildren().add(new Label("Chi tiêu theo danh mục (tháng " + now + "):"));

        if (byCategory.isEmpty()) {
            content.getChildren().add(new Label("(chưa có dữ liệu chi tiêu tháng này)"));
        } else {
            for (Map.Entry<Category, Double> entry : byCategory.entrySet()) {
                content.getChildren().add(new Label(
                        "- " + entry.getKey().getName() + ": " + CurrencyFormatUtil.format(entry.getValue())));
            }
        }

        manager.getLargestExpense(now).ifPresent(t -> content.getChildren().add(
                new Label("\nKhoản chi lớn nhất: " + CurrencyFormatUtil.format(t.getAmount())
                        + " (" + t.getCategory().getName() + ")")));

        manager.getSmallestExpense(now).ifPresent(t -> content.getChildren().add(
                new Label("Khoản chi nhỏ nhất: " + CurrencyFormatUtil.format(t.getAmount())
                        + " (" + t.getCategory().getName() + ")")));

        manager.getTopExpensiveCategory(now).ifPresent(entry -> content.getChildren().add(
                new Label("Danh mục tốn kém nhất: " + entry.getKey().getName()
                        + " (" + CurrencyFormatUtil.format(entry.getValue()) + ")")));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thống kê chi tiêu");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(content);
        applyStylesheet(alert);
        alert.showAndWait();
    }

    /**
     * Hoàn tác thao tác gần nhất (thêm/sửa/xóa giao dịch, ví, hoặc danh mục).
     */
    @FXML
    private void handleUndo() {

        if (!manager.canUndo()) {
            showInfo("Không có thao tác nào để hoàn tác.");
            return;
        }

        String description = manager.undoLast();
        refreshTable(manager.getTransactions());
        refreshSummary();
        showInfo("Đã hoàn tác: " + description);
    }

    /**
     * Hoàn tác toàn bộ lịch sử thao tác trong phiên làm việc hiện tại, sau khi
     * người dùng xác nhận (đề phòng bấm nhầm mất công sức các thao tác gần đây).
     */
    @FXML
    private void handleUndoAll() {

        if (!manager.canUndo()) {
            showInfo("Không có thao tác nào để hoàn tác.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hoàn tác TẤT CẢ thao tác trong phiên làm việc này? Không thể hoàn tác lại sau khi đã thực hiện.",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            int count = manager.undoAll();
            refreshTable(manager.getTransactions());
            refreshSummary();
            showInfo("Đã hoàn tác " + count + " thao tác.");
        }
    }

    // ================== TIỆN ÍCH ==================

    /**
     * Áp style CSS chung của ứng dụng cho một dialog để giao diện đồng bộ.
     *
     * @param dialog dialog cần áp style
     */
    private void applyStylesheet(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/expensemanager/view/styles.css").toExternalForm());
    }

    /**
     * Hiển thị hộp thoại cảnh báo.
     *
     * @param message nội dung cảnh báo
     */
    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    /**
     * Hiển thị hộp thoại thông báo.
     *
     * @param message nội dung thông báo
     */
    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}
