package gui.controllers.tables;

import gui.ScreenManager;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRowDialog extends Dialog<Map<String, String>> {
    private ButtonType confirmAddButtonType;
    private GridPane grid;
    private Map<String, Node> inputControls;
    private final String STYLE = "-fx-font-size: 16px";
    private final String tableName;

    private final List<DateTimePicker> datePickers = new ArrayList<>();

    public AddRowDialog(String tableName, List<ColumnStructure> columns, String[] columnNames) {
        this.tableName = tableName;
        buildUI(tableName);
        initializeInputFields(columns, columnNames);
        this.getDialogPane().setMinHeight(400);
        this.getDialogPane().setMinWidth(600);
    }

    private void buildUI(String tableName) {
        this.setTitle("Add New Row to " + tableName);
        this.setHeaderText("Enter details: ");
        this.setOnShown(e -> {
            Node header = getDialogPane().getHeader();
            header.setStyle(STYLE);
        });

        confirmAddButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(confirmAddButtonType, ButtonType.CANCEL);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
    }

    private void initializeInputFields(List<ColumnStructure> columns, String[] columnNames) {
        inputControls = new HashMap<>();
        datePickers.clear();
        int row = 0;

        // 1. Get Foreign Key Metadata once before looping
        Map<String, ForeignKeyRef> foreignKeys = getForeignKeyMap(tableName);

        for (ColumnStructure column : columns) {
            if (column.isHidden()) continue;
            if (!column.isEditable()) {
                row++; continue;
            }

            Label columnNameLabel = new Label(columnNames[row] + ":");
            columnNameLabel.setStyle(STYLE);
            grid.add(columnNameLabel, 0, row);

            Node inputNode;
            String colType = column.type().toLowerCase();

            if (colType.contains("date") || colType.contains("time") || colType.contains("stamp")) {
                DateTimePicker picker = new DateTimePicker();
                picker.setStyle(STYLE);
                if (datePickers.isEmpty()) {
                    picker.setCurrentTime();
                }
                datePickers.add(picker);
                inputNode = picker;

            } else {
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.setStyle(STYLE);
                comboBox.setPrefWidth(300);

                if (foreignKeys.containsKey(column.name())) {
                    ForeignKeyRef ref = foreignKeys.get(column.name());
                    List<String> validIds = fetchReferenceValues(ref.pkTable(), ref.pkColumn());

                    comboBox.getItems().addAll(validIds);
                    comboBox.setEditable(false);
                    comboBox.setPromptText("Select valid " + column.name());
                } else {
                    List<String> existingValues = fetchDistinctValues(column.name());
                    comboBox.getItems().addAll(existingValues);
                    comboBox.setEditable(true);
                    comboBox.setPromptText("Select or type " + column.type());
                }

                inputNode = comboBox;
            }

            grid.add(inputNode, 1, row);
            inputControls.put(column.name(), inputNode);
            row++;
        }

        this.getDialogPane().setContent(grid);
        setupValidation();
        setupResultConverter();
    }

    // --- Helper to get FK Metadata ---
    private Map<String, ForeignKeyRef> getForeignKeyMap(String tableName) {
        Map<String, ForeignKeyRef> fks = new HashMap<>();
        try (Connection conn = ScreenManager.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            // null for catalog and schema to search current DB
            try (ResultSet rs = meta.getImportedKeys(conn.getCatalog(), null, tableName)) {
                while (rs.next()) {
                    String fkColumnName = rs.getString("FKCOLUMN_NAME");
                    String pkTableName = rs.getString("PKTABLE_NAME");
                    String pkColumnName = rs.getString("PKCOLUMN_NAME");
                    fks.put(fkColumnName, new ForeignKeyRef(pkTableName, pkColumnName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fks;
    }

    // --- Helper to fetch valid IDs from the Referenced Parent Table ---
    private List<String> fetchReferenceValues(String table, String pkColumn) {
        List<String> values = new ArrayList<>();
        String sql = "SELECT " + pkColumn + " FROM " + table + " ORDER BY " + pkColumn + " ASC";
        try (Connection conn = ScreenManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    // Existing method for non-FK columns
    private List<String> fetchDistinctValues(String columnName) {
        List<String> values = new ArrayList<>();
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName + " ORDER BY " + columnName + " ASC LIMIT 100";
        try (Connection conn = ScreenManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String val = rs.getString(1);
                if (val != null && !val.isBlank()) values.add(val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    private void setupValidation() {
        this.setOnShown(e -> {
            Button confirmAddButton = (Button) getDialogPane().lookupButton(confirmAddButtonType);
            confirmAddButton.disableProperty().bind(
                    Bindings.createBooleanBinding(() -> {
                                boolean hasEmptyFields = inputControls.values().stream().anyMatch(node -> {
                                    if (node instanceof ComboBox) {
                                        ComboBox<?> cb = (ComboBox<?>) node;
                                        // If Editable: check Editor text. If Non-Editable (FK): check SelectionModel
                                        if (cb.isEditable()) {
                                            String text = cb.getEditor().getText();
                                            return text == null || text.isBlank();
                                        } else {
                                            return cb.getValue() == null;
                                        }
                                    } else if (node instanceof DateTimePicker) {
                                        return ((DateTimePicker) node).isEmpty();
                                    }
                                    return true;
                                });

                                if (hasEmptyFields) return true;

                                // Date Logic (Restock vs Expiry)
                                if (datePickers.size() >= 2) {
                                    DateTimePicker restockPicker = datePickers.get(0);
                                    DateTimePicker expiryPicker = datePickers.get(1);
                                    LocalDateTime restockDate = restockPicker.getDateTimeValue();
                                    LocalDateTime expiryDate = expiryPicker.getDateTimeValue();

                                    if (restockDate != null && expiryDate != null) {
                                        return expiryDate.isBefore(restockDate);
                                    }
                                }
                                return false;
                            },
                            // Re-bind listeners
                            inputControls.values().stream().map(node -> {
                                if (node instanceof ComboBox) {
                                    // Must listen to both value (for strict) and text (for editable)
                                    return ((ComboBox<?>) node).valueProperty();
                                }
                                if (node instanceof DateTimePicker) return ((DateTimePicker) node).getDateProperty();
                                return null;
                            }).toArray(Observable[]::new))
            );
        });
    }

    private void setupResultConverter() {
        this.setResultConverter(dialogButton -> {
            if (dialogButton.equals(confirmAddButtonType)) {
                Map<String, String> results = new HashMap<>();
                inputControls.forEach((columnName, node) -> {
                    if (node instanceof ComboBox) {
                        ComboBox<?> cb = (ComboBox<?>) node;
                        // Get Value if strict, Text if editable
                        String val = cb.isEditable() ? cb.getEditor().getText() : (String) cb.getValue();
                        results.put(columnName, val);
                    } else if (node instanceof DateTimePicker) {
                        results.put(columnName, ((DateTimePicker) node).getTimestamp());
                    }
                });
                return results;
            }
            return null;
        });
    }
}