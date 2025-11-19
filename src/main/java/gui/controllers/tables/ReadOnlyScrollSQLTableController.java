package gui.controllers.tables;

import gui.ScreenManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadOnlyScrollSQLTableController {

    @FXML
    private TableView<Map<String, Object>> table;
    @FXML
    private Label tableTitle;
    @FXML
    private ComboBox<String> filterColumnComboBox;
    @FXML
    private TextField filterField;

    private ObservableList<Map<String, Object>> masterData = FXCollections.observableArrayList();
    private FilteredList<Map<String, Object>> filteredData;
    private SortedList<Map<String, Object>> sortedData;
    private Map<String, String> displayToDbColumnMap = new HashMap<>();

    private List<ColumnStructure> columnStructures = new ArrayList<>();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        filteredData = new FilteredList<>(masterData, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        filterColumnComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
    }

    private void updateFilter() {
        String filterText = filterField.getText();
        String selectedDisplayCol = filterColumnComboBox.getValue();

        filteredData.setPredicate(row -> {
            if (filterText == null || filterText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = filterText.toLowerCase();

            if (selectedDisplayCol == null) {
                return row.values().stream()
                        .filter(val -> val != null)
                        .anyMatch(val -> val.toString().toLowerCase().contains(lowerCaseFilter));
            }

            String dbColumnKey = displayToDbColumnMap.get(selectedDisplayCol);
            if (dbColumnKey != null) {
                Object val = row.get(dbColumnKey);
                return val != null && val.toString().toLowerCase().contains(lowerCaseFilter);
            }

            return false;
        });
    }

    public void loadTable(ResultSet rs, String tableTitle, String... columnNames) {
        table.getColumns().clear();
        masterData.clear();
        columnStructures.clear();
        displayToDbColumnMap.clear();
        filterColumnComboBox.getItems().clear();
        setTableTitle(tableTitle);

        try {
            ResultSetMetaData metaData = rs.getMetaData();
            List<TableColumn<Map<String, Object>, Object>> columnHolder = new ArrayList<>();

            int cols = metaData.getColumnCount();
            int visibleColIndex = 0;
            for (int i = 1; i <= cols; i++) {
                String dbColumnName = metaData.getColumnName(i);
                ColumnStructure currentStructure = new ColumnStructure(dbColumnName, metaData.getColumnTypeName(i),
                        metaData.getColumnName(i).equals("visible") ||
                                metaData.isAutoIncrement(i));
                columnStructures.add(currentStructure);

                if (currentStructure.isHidden()) continue;

                if (visibleColIndex < columnNames.length) {
                    String displayName = columnNames[visibleColIndex];
                    displayToDbColumnMap.put(displayName, dbColumnName);
                    filterColumnComboBox.getItems().add(displayName);

                    TableColumn<Map<String, Object>, Object> tableCol = new TableColumn<>(displayName);
                    tableCol.setMinWidth(170);
                    visibleColIndex++;
                    tableCol.setCellValueFactory(cellData ->
                            new SimpleObjectProperty<>(cellData.getValue().get(dbColumnName))
                    );
                    columnHolder.add(tableCol);
                }
            }

            if (!filterColumnComboBox.getItems().isEmpty()) {
                filterColumnComboBox.getSelectionModel().selectFirst();
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
                masterData.add(row);
            }
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