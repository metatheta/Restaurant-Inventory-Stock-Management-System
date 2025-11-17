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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private List<ColumnStructure> columnStructures = new ArrayList<>();

    @FXML
    public void initialize() {
        conn = ScreenManager.SINGLETON.getConnection();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public void loadTable(String tableName) {
        this.tableName = tableName;
        table.getColumns().clear();
        data.clear();
        columnStructures.clear();
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();

            java.util.List<TableColumn<Map<String, Object>, Object>> columnHolder = new java.util.ArrayList<>();

            int cols = metaData.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                String columnName = metaData.getColumnName(i);
                ColumnStructure currentStructure = new ColumnStructure(columnName, metaData.getColumnTypeName(i),
                        metaData.isAutoIncrement(i));
                columnStructures.add(currentStructure);
                // TODO make it hide the visibility flag column too

                if (currentStructure.isHidden()) continue;

                TableColumn<Map<String, Object>, Object> tableCol = new TableColumn<>(columnName);
                tableCol.setCellValueFactory(cellData ->
                        new SimpleObjectProperty<>(cellData.getValue().get(columnName))
                );
                columnHolder.add(tableCol);
            }
            table.getColumns().addAll(columnHolder);
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) {
                    Object object = rs.getObject(i);
                    if (object == null || object.toString().isEmpty()) {
                        object = "NULL";
                    }
                    row.put(metaData.getColumnName(i), object);
                }
                data.add(row);
            }
            table.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addRow() {
        AddRowDialog dialog = new AddRowDialog(tableName, columnStructures);
        dialog.showAndWait().ifPresent(this::insertRow);
    }

    private void insertRow(Map<String, String> rowData) {
        StringBuilder columnsPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();

        rowData.forEach((column, value) -> {
            columnsPart.append(column).append(", ");
            valuesPart.append("'").append(value).append("', ");
        });

        if (!columnsPart.isEmpty()) {
            columnsPart.setLength(columnsPart.length() - 2);
            valuesPart.setLength(valuesPart.length() - 2);
        }

        String sql = "INSERT INTO " + tableName + " (" + columnsPart + ") VALUES (" + valuesPart + ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshData() {
        loadTable(tableName);
    }

    public void returnToMainMenu() {
        try {
            gui.view.ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}