package com.expensemanager;

import com.expensemanager.service.ExpenseManager;
import com.expensemanager.storage.CsvStorage;
import com.expensemanager.storage.Storage;
import com.expensemanager.view.ConsoleView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Điểm khởi động ứng dụng.
 * - Chạy mặc định (không tham số): mở giao diện đồ họa JavaFX.
 * - Chạy với tham số "console": chạy ở chế độ dòng lệnh (menu 0-9 theo đề bài).
 *   VD: mvn javafx:run -Djavafx.args=console (hoặc chạy trực tiếp class Main
 *   với argument "console" từ IntelliJ Run Configuration).
 */
public class Main extends Application {

    private static final String DATA_PATH = "data/transactions.csv";

    private static boolean consoleMode = false;

    @Override
    public void start(Stage stage) throws Exception {

        Storage storage = new CsvStorage();
        ExpenseManager expenseManager = ExpenseManager.getInstance();
        expenseManager.setStorage(storage);
        expenseManager.loadData(DATA_PATH);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/expensemanager/view/Dashboard.fxml")
        );

        // Controller tự lấy ExpenseManager.getInstance() (Singleton) trong initialize(),
        // dữ liệu đã được loadData() ở trên trước khi FXML dựng giao diện nên vẫn đồng bộ.
        Parent root = loader.load();

        Scene scene = new Scene(root, 1050, 680);

        stage.setTitle("Personal Expense Manager");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            try {
                expenseManager.saveData(DATA_PATH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        stage.show();
    }

    public static void main(String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            runConsole();
            return;
        }

        launch(args);
    }

    private static void runConsole() {

        Storage storage = new CsvStorage();
        ExpenseManager expenseManager = ExpenseManager.getInstance();
        expenseManager.setStorage(storage);

        try {
            expenseManager.loadData(DATA_PATH);
        } catch (Exception e) {
            System.out.println("Khong the doc du lieu cu: " + e.getMessage());
        }

        new ConsoleView().run();

        try {
            expenseManager.saveData(DATA_PATH);
        } catch (Exception e) {
            System.out.println("Khong the luu du lieu: " + e.getMessage());
        }
    }
}
