package gui.controllers.tables;

import db.Query;
import gui.ScreenManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreTableSQLController {
    @FXML
    private TableView<Map<String, Object>> table;

    private String tableName;

    private ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
    private List<ColumnStructure> columnStructures = new ArrayList<>();
    private String[] columnNames;

    @FXML
    public void initialize() {
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

        actionCol.setCellFactory(param -> new EditDeleteRelatedCell(
                this::editRowPopup,
                this::deleteRowFromDb,
                this::viewRelatedRows
        ));

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
                // We don't have a default foreign key value so I'm gonna include them in the display / add prompt
                // TODO ask sir about this
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
        AddRowDialog dialog = new AddRowDialog(tableName, columnStructures, columnNames);
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

        try (Connection conn = ScreenManager.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
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
                try (Connection conn = ScreenManager.getConnection()) {
                    PreparedStatement statement = conn.prepareStatement(sql);
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
        EditRowDialog dialog = new EditRowDialog(tableName, columnStructures, columnNames, originalRowData);
        dialog.showAndWait().ifPresent(editedData -> {
            Object targetId = originalRowData.get(columnStructures.get(0).name());
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

        try (Connection conn = ScreenManager.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int i = 0; i < values.size(); i++) {
                statement.setObject(i + 1, values.get(i));
            }
            statement.executeUpdate();
            refreshData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewRelatedRows(Map<String, Object> rowData) {
        if (rowData == null || tableName == null) return;
        String sql = null;
        Object idValue = null;
        String[] relatedColumnNames = null;
        String tableTitle = null;
        switch (tableName) {
            case "stock_items":
                sql = Query.stockItemAndSuppliers();
                idValue = rowData.get("item_id");
                relatedColumnNames = new String[]{"Item Name", "Unit of Measure", "Category", "Supplier Name",
                        "Contact Person", "Contact Info"};
                tableTitle = "Stock Item and Suppliers Providing It";
                break;
            case "inventory":
                sql = Query.storedItemAndLocations();
                idValue = rowData.get("inventory_id");
                relatedColumnNames = new String[]{"Stored Item", "Running Balance", "Last Restocked", "Expiry Date",
                        "Storage Name", "Address", "Storage Type"};
                tableTitle = "Inventory Item and Locations It is Present In";
                break;
            case "stock_locations":
                sql = Query.locationAndStoredItems();
                idValue = rowData.get("location_id");
                relatedColumnNames = new String[]{"Storage Name", "Storage Type", "Address", "Stored Item",
                        "Unit of Measure", "Running Balance", "Item Category"};
                tableTitle = "Storage Location and Inventory Items Stored There";
                break;
            case "suppliers":
                sql = Query.supplierAndProducts();
                idValue = rowData.get("supplier_id");
                relatedColumnNames = new String[]{"Supplier Name", "Contact Person", "Contact Info", "Item Name",
                        "Amount", "Unit Cost"};
                tableTitle = "Supplier and Products They Provide";
                break;
            default:
                System.out.println("Not a core table: " + tableName);
                return;
        }

        if (idValue == null) return;

        try (
                Connection conn = ScreenManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, idValue);
            ResultSet rs = stmt.executeQuery();

            ScreenManager.SINGLETON.loadReadOnlyTableScreen(rs, tableTitle, relatedColumnNames);

        } catch (
                SQLException | IOException e) {
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