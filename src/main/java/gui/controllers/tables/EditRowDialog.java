package gui.controllers.tables;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditRowDialog extends Dialog<Map<String, String>> {
    private ButtonType confirmSaveButtonType;
    private GridPane grid;
    private Map<String, Node> inputControls;
    private final String STYLE = "-fx-font-size: 16px";

    public EditRowDialog(String tableName, List<ColumnStructure> columns, String[] columnNames, Map<String, Object> originalData) {
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
                inputNode = picker;
            } else {
                TextField textField = new TextField();
                textField.setStyle(STYLE);
                textField.setPromptText(column.type());
                if (originalValue != null && !originalValue.toString().equals("NULL")) {
                    textField.setText(originalValue.toString());
                }
                inputNode = textField;
            }

            grid.add(inputNode, 1, row);
            inputControls.put(column.name(), inputNode);
            row++;
        }

        this.getDialogPane().setContent(grid);
        setupValidation();
        setupResultConverter();
    }

    private void setupValidation() {
        this.setOnShown(e -> {
            Button confirmButton = (Button) getDialogPane().lookupButton(confirmSaveButtonType);
            confirmButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> inputControls.values().stream().anyMatch(node -> {
                                if (node instanceof TextField) {
                                    return ((TextField) node).getText().isBlank();
                                } else if (node instanceof DateTimePicker) {
                                    return ((DateTimePicker) node).isEmpty();
                                }
                                return true;
                            }),
                            inputControls.values().stream().map(node -> {
                                if (node instanceof TextField) {
                                    return ((TextField) node).textProperty();
                                } else if (node instanceof DateTimePicker) {
                                    return ((DateTimePicker) node).getDatePicker().valueProperty();
                                }
                                return null;
                            }).toArray(Observable[]::new)
                    )
            );
        });
    }

    private void setupResultConverter() {
        this.setResultConverter(dialogButton -> {
            if (dialogButton.equals(confirmSaveButtonType)) {
                Map<String, String> results = new HashMap<>();
                inputControls.forEach((columnName, node) -> {
                    if (node instanceof TextField) {
                        results.put(columnName, ((TextField) node).getText());
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