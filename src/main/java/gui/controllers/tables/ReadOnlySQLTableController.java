package gui.controllers.tables;

import gui.ScreenManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadOnlySQLTableController {

    @FXML
    private TableView<Map<String, Object>> table;
    private ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
    private List<ColumnStructure> columnStructures = new ArrayList<>();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    @FXML
    private Label tableTitle;

    public void loadTable(String tableName, String... columnNames) {
        table.getColumns().clear();
        data.clear();
        columnStructures.clear();

        try (Connection conn = ScreenManager.getConnection()) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();

            java.util.List<TableColumn<Map<String, Object>, Object>> columnHolder = new java.util.ArrayList<>();

            int cols = metaData.getColumnCount();
            int visibleColIndex = 0;
            for (int i = 1; i <= cols; i++) {
                String columnName = metaData.getColumnName(i);
                ColumnStructure currentStructure = new ColumnStructure(columnName, metaData.getColumnTypeName(i),
                        metaData.getColumnName(i).equals("visible") ||
                                metaData.isAutoIncrement(i));
                columnStructures.add(currentStructure);

                if (currentStructure.isHidden()) continue;

                TableColumn<Map<String, Object>, Object> tableCol = new TableColumn<>(columnName);
                visibleColIndex++;
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
                if (row.get("visible").equals(true)) {
                    data.add(row);
                }
            }
            table.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // TODO see which method needs to be removed (or just keep the overload lol I guess)

    public void loadTable(ResultSet rs, String tableTitle, String... columnNames) {
        table.getColumns().clear();
        data.clear();
        columnStructures.clear();
        setTableTitle(tableTitle);

        try {
            ResultSetMetaData metaData = rs.getMetaData();

            java.util.List<TableColumn<Map<String, Object>, Object>> columnHolder = new java.util.ArrayList<>();

            int cols = metaData.getColumnCount();
            int visibleColIndex = 0;
            for (int i = 1; i <= cols; i++) {
                String columnName = metaData.getColumnName(i);
                ColumnStructure currentStructure = new ColumnStructure(columnName, metaData.getColumnTypeName(i),
                        metaData.getColumnName(i).equals("visible") ||
                                metaData.isAutoIncrement(i));
                columnStructures.add(currentStructure);

                if (currentStructure.isHidden()) continue;

                TableColumn<Map<String, Object>, Object> tableCol = new TableColumn<>(columnNames[visibleColIndex]);
                visibleColIndex++;
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

    public void setTableTitle(String title) {
        if (tableTitle != null) {
            tableTitle.setText(title);
        }
    }

    public void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}