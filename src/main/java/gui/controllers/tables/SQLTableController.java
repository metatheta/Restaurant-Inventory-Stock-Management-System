package gui.controllers.tables;

import gui.view.ScreenManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SQLTableController {
    @FXML
    private TableView<Map<String, Object>> table;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button refreshButton;

    private String tableName;
    private ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
    private Connection conn;

    @FXML
    public void initialize() {
        conn = ScreenManager.SINGLETON.getConnection();
        refreshButton.setOnAction(e -> loadTable(tableName));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public void loadTable(String tableName) {
        this.tableName = tableName;
        table.getColumns().clear();
        data.clear();

        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();

            int cols = metaData.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                int colIndex = i;
                TableColumn<Map<String, Object>, Object> tableCol = new TableColumn<>(metaData.getColumnName(i));
                tableCol.setCellValueFactory(cellData ->
                        {
                            try {
                                return new SimpleObjectProperty<>(cellData.getValue().get(metaData.getColumnName(colIndex)));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
                table.getColumns().add(tableCol);
            }

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                data.add(row);
            }
            table.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void returnToMainMenu() {
        try {
            gui.view.ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}