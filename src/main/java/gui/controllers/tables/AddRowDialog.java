package gui.controllers.tables;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRowDialog extends Dialog<Map<String, String>> {
    private ButtonType confirmAddButtonType;
    private GridPane grid;
    private Map<String, Node> inputControls;
    private final String STYLE = "-fx-font-size: 16px";

    public AddRowDialog(String tableName, List<ColumnStructure> columns, String[] columnNames) {
        buildUI(tableName);
        initializeInputFields(columns, columnNames);
        this.getDialogPane().setMinHeight(400);
        this.getDialogPane().setMinWidth(600);
    }

    private void buildUI(String tableName) {
        this.setTitle("Add New Row to " + tableName);
        this.setHeaderText("Enter the details for the new row: ");
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
        int row = 0;

        for (ColumnStructure column : columns) {
            if (column.isHidden()) continue;
            Label columnNameLabel = new Label(columnNames[row] + ":");
            columnNameLabel.setStyle(STYLE);
            grid.add(columnNameLabel, 0, row);

            Node inputNode;
            String colType = column.type().toLowerCase();

            if (colType.contains("date") || colType.contains("time") || colType.contains("stamp")) {
                DateTimePicker dateTimePicker = new DateTimePicker();
                dateTimePicker.setStyle(STYLE);
                inputNode = dateTimePicker;
            } else {
                TextField textField = new TextField();
                textField.setStyle(STYLE);
                textField.setPromptText(column.type());
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
            Button confirmAddButton = (Button) getDialogPane().lookupButton(confirmAddButtonType);
            confirmAddButton.disableProperty().bind(
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
            if (dialogButton.equals(confirmAddButtonType)) {
                Map<String, String> results = new HashMap<>();
                inputControls.forEach((columnName, node) -> {
                    if (node instanceof TextField) {
                        results.put(columnName, ((TextField) node).getText());
                    } else if (node instanceof DateTimePicker) {
                        // Use the custom getTimestamp method
                        results.put(columnName, ((DateTimePicker) node).getTimestamp());
                    }
                });
                return results;
            }
            return null;
        });
    }
}