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

    private ButtonType confirmEditButtonType;
    private GridPane grid;
    private Map<String, TextField> inputFields;
    private Map<String, Object> originalData;
    private final String STYLE = "-fx-font-size: 16px";

    public EditRowDialog(String tableName, List<ColumnStructure> columns, Map<String, Object> originalData,
                         String[] columnNames) {
        this.originalData = originalData;
        buildUI(tableName);
        initializeInputFields(columns, columnNames);
        this.getDialogPane().setMinHeight(400);
        this.getDialogPane().setMinWidth(600);
    }

    private void buildUI(String tableName) {
        this.setTitle("Edit Row in " + tableName);
        this.setHeaderText("Modify the details below:");
        this.setOnShown(e -> {
            Node header = getDialogPane().getHeader();
            header.setStyle(STYLE);
        });


        confirmEditButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(confirmEditButtonType, ButtonType.CANCEL);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
    }

    private void initializeInputFields(List<ColumnStructure> columns, String columnNames[]) {
        inputFields = new HashMap<>();
        int row = 0;

        for (ColumnStructure column : columns) {
            if (column.isHidden()) continue;

            Label columnNameLabel = new Label(columnNames[row] + ":");
            columnNameLabel.setStyle(STYLE);
            grid.add(columnNameLabel, 0, row);

            TextField input = new TextField();
            input.setStyle(STYLE);
            input.setPromptText(column.type());
            Object originalValue = originalData.get(column.name());

            if (originalValue != null && !originalValue.toString().equals("NULL")) {
                input.setText(String.valueOf(originalValue));
            }

            grid.add(input, 1, row);
            inputFields.put(column.name(), input);
            row++;
        }

        this.getDialogPane().setContent(grid);

        this.setOnShown(e -> {
            Button confirmButton = (Button) getDialogPane().lookupButton(confirmEditButtonType);
            confirmButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> inputFields.values().stream().anyMatch(f -> f.getText().isBlank()),
                            inputFields.values().stream().map(TextField::textProperty).toArray(Observable[]::new)
                    )
            );
        });

        this.setResultConverter(dialogButton -> {
            if (dialogButton.equals(confirmEditButtonType)) {
                return getResultMap();
            }
            return null;
        });
    }

    private Map<String, String> getResultMap() {
        Map<String, String> results = new HashMap<>();
        inputFields.forEach((columnName, input) -> results.put(columnName, input.getText()));
        return results;
    }
}
