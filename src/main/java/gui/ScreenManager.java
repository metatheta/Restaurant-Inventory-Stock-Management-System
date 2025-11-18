package gui;

import gui.controllers.tables.SQLTableController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;

public class ScreenManager {
    public static final ScreenManager SINGLETON = new ScreenManager();

    private Stage stage;
    private Connection connection;

    public void start(Stage stage, Connection connection) {
        this.stage = stage;
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void displayScreen(String fxml) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }
    }

    public void loadTableScreen(String tableName, String... columnNames) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/view/tables/sql-table.fxml"));
        Parent root = loader.load();
        SQLTableController controller = loader.getController();
        controller.loadTable(tableName, columnNames);

        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }
    }
}
