package gui;

import gui.controllers.tables.CoreTableSQLController;
import gui.controllers.tables.ReadOnlySQLTableController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ScreenManager {
    public static final ScreenManager SINGLETON = new ScreenManager();

    private Stage stage;
    private static Connection connection = null;

    public void start(Stage stage) {
        this.stage = stage;
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:mysql://localhost/crisms_db?" +
                        "user=root&password=p@ssword");
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void displayScreen(String fxml) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }
    }

    public void loadCoreTableScreen(String tableName, String... columnNames) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/view/tables/core-sql-table.fxml"));
        Parent root = loader.load();
        CoreTableSQLController controller = loader.getController();
        controller.loadTable(tableName, columnNames);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }
    }

    public void loadReadOnlyTableScreen(ResultSet resultSet, String tableTitle, String... columnNames) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/view/tables/read-only-sql-table.fxml"));
        Parent root = loader.load();
        ReadOnlySQLTableController controller = loader.getController();
        controller.loadTable(resultSet, tableTitle, columnNames);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }
    }
}
