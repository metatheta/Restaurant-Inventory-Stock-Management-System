package gui.controllers.tables;

import gui.ScreenManager;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRowDialog extends Dialog<Map<String, String>> {
    private ButtonType confirmSaveButtonType;
    private GridPane grid;
    private Map<String, Node> inputControls;
    private final String STYLE = "-fx-font-size: 16px";
    private final String tableName;
    private final List<DateTimePicker> datePickers = new ArrayList<>();

    public EditRowDialog(String tableName, List<ColumnStructure> columns, String[] columnNames, Map<String, Object> originalData) {
        this.tableName = tableName;
        buildUI(tableName);
        initializeInputFields(columns, columnNames, originalData);
        this.getDialogPane().setMinHeight(400);
        this.getDialogPane().setMinWidth(600);
    }

    private void buildUI(String tableName) {
        this.setTitle("Edit Row in " + tableName);
        this.setHeaderText("Edit the details below: ");
        this.setOnShown(e -> {
            Node header = getDialogPane().getHeader();
            header.setStyle(STYLE);
        });

        confirmSaveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(confirmSaveButtonType, ButtonType.CANCEL);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
    }

    private void initializeInputFields(List<ColumnStructure> columns, String[] columnNames, Map<String, Object> originalData) {
        inputControls = new HashMap<>();
        datePickers.clear();
        int row = 0;

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
            Object originalValue = originalData.get(column.name());

            if (colType.contains("date") || colType.contains("time") || colType.contains("stamp")) {
                DateTimePicker picker = new DateTimePicker();
                picker.setStyle(STYLE);
                if (originalValue != null) {
                    picker.setTimestamp(originalValue.toString());
                }

                datePickers.add(picker);
                inputNode = picker;
            } else {
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.setEditable(true);
                comboBox.setStyle(STYLE);
                comboBox.setPromptText("Select or type " + column.type());
                comboBox.setPrefWidth(300);

                List<String> existingValues = fetchDistinctValues(column.name());
                comboBox.getItems().addAll(existingValues);

                if (originalValue != null && !originalValue.toString().equals("NULL")) {
                    comboBox.setValue(originalValue.toString());
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
            Button confirmButton = (Button) getDialogPane().lookupButton(confirmSaveButtonType);
            confirmButton.disableProperty().bind(
                    Bindings.createBooleanBinding(() -> {
                                boolean hasEmptyFields = inputControls.values().stream().anyMatch(node -> {
                                    if (node instanceof ComboBox) {
                                        String text = ((ComboBox<?>) node).getEditor().getText();
                                        return text == null || text.isBlank();
                                    } else if (node instanceof DateTimePicker) {
                                        return ((DateTimePicker) node).isEmpty();
                                    }
                                    return true;
                                });

                                if (hasEmptyFields) return true;

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
                            inputControls.values().stream().map(node -> {
                                if (node instanceof ComboBox) return ((ComboBox<?>) node).getEditor().textProperty();
                                if (node instanceof DateTimePicker) return ((DateTimePicker) node).getDateProperty();
                                return null;
                            }).toArray(Observable[]::new))
            );
        });
    }

    private void setupResultConverter() {
        this.setResultConverter(dialogButton -> {
            if (dialogButton.equals(confirmSaveButtonType)) {
                Map<String, String> results = new HashMap<>();
                inputControls.forEach((columnName, node) -> {
                    if (node instanceof ComboBox) {
                        results.put(columnName, ((ComboBox<?>) node).getEditor().getText());
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