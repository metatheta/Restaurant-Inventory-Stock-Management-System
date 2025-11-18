package gui.controllers.tables;

import gui.ScreenManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    // TODO pascalcase column and table names

    private ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
    private Connection conn;
    private List<ColumnStructure> columnStructures = new ArrayList<>();
    private String[] columnNames;

    @FXML
    public void initialize() {
        conn = ScreenManager.SINGLETON.getConnection();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public void loadTable(String tableName, String... columnNames) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        table.getColumns().clear();
        data.clear();
        columnStructures.clear();

        TableColumn<Map<String, Object>, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMinWidth(300);
        actionCol.setMaxWidth(300);

        actionCol.setCellFactory(param -> new EditDeleteCell(
                this::editRowPopup,
                this::deleteRowFromDb
        ));

        try {
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
                                metaData.getColumnName(i).contains("_id"));
                // Currently hiding all _id columns so foreign and primary keys
                // TODO ask sir if we can hide these id columns given the edit and delete buttons being per-row

                columnStructures.add(currentStructure);

                if (currentStructure.isHidden()) continue;

                TableColumn<Map<String, Object>, Object> tableCol = new TableColumn<>(columnNames[visibleColIndex]);
                visibleColIndex++;
                tableCol.setCellValueFactory(cellData ->
                        new SimpleObjectProperty<>(cellData.getValue().get(columnName))
                );
                columnHolder.add(tableCol);
            }

            table.getColumns().add(actionCol);
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

    public void addRowPopup() {
        AddRowDialog dialog = new AddRowDialog(tableName, columnStructures);
        dialog.showAndWait().ifPresent(this::addRowToDb);
    }

    private void addRowToDb(Map<String, String> rowData) {
        StringBuilder columnsPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();
        List<Object> values = new ArrayList<>();

        rowData.forEach((column, value) -> {
            columnsPart.append(column).append(", ");
            valuesPart.append("?, ");
            values.add(value);
        });

        columnsPart.append("visible");
        valuesPart.append("?");
        values.add(1);

        String sql = "INSERT INTO " + tableName + " (" + columnsPart + ") VALUES (" + valuesPart + ")";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                statement.setObject(i + 1, values.get(i));
            }
            statement.executeUpdate();
            refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteRowFromDb(Map<String, Object> rowData) {
        String idColumn = columnStructures.getFirst().name();
        Object targetId = rowData.get(idColumn);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");

        alert.setHeaderText("Delete row with ID: " + targetId);
        alert.setContentText("This will delete the row. Are you sure?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String sql = "UPDATE " + tableName + " SET visible = 0 WHERE " + idColumn + " = ?";
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    statement.setObject(1, targetId);
                    int affectedRows = statement.executeUpdate();
                    if (affectedRows > 0) {
                        refreshData();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void editRowPopup(Map<String, Object> originalRowData) {
        Object targetId = originalRowData.get(columnStructures.getFirst().name());
        EditRowDialog dialog = new EditRowDialog(tableName, columnStructures, originalRowData);
        dialog.showAndWait().ifPresent(editedData -> {
            editRowInDb(editedData, targetId);
        });
    }

    private void editRowInDb(Map<String, String> updatedData, Object targetId) {
        StringBuilder setPart = new StringBuilder();
        List<Object> values = new ArrayList<>();
        updatedData.forEach((column, value) -> {
            setPart.append(column).append(" = ?, ");
            values.add(value);
        });
        setPart.setLength(setPart.length() - 2);

        String idColumn = columnStructures.getFirst().name();
        values.add(targetId);
        String sql = "UPDATE " + tableName + " SET " + setPart + " WHERE " + idColumn + " = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                statement.setObject(i + 1, values.get(i));
            }
            statement.executeUpdate();
            refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshData() {
        loadTable(tableName, columnNames);
    }

    public void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}