package gui.controllers.tables;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRowDialog extends Dialog<Map<String, String>> {
    private ButtonType confirmAddButtonType;
    private GridPane grid;
    private Map<String, TextField> inputFields;

    public AddRowDialog(String tableName, List<ColumnStructure> columns) {
        buildUI(tableName);
        initializeInputFields(columns);
    }

    private void buildUI(String tableName) {
        this.setTitle("Add New Row to " + tableName);
        this.setHeaderText("Enter the details for the new row: ");

        confirmAddButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(confirmAddButtonType, ButtonType.CANCEL);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
    }

    private void initializeInputFields(List<ColumnStructure> columns) {
        inputFields = new HashMap<>();
        int row = 0;

        for (ColumnStructure column : columns) {
            if (column.isHidden()) continue;
            // TODO also skip the visibility flag column
            grid.add(new Label(column.name() + ":"), 0, row);
            TextField input = new TextField();
            input.setPromptText(column.type());

            grid.add(input, 1, row);
            inputFields.put(column.name(), input);
            row++;
        }

        this.getDialogPane().setContent(grid);

        this.setOnShown(e -> {
            Button confirmAddButton = (Button) getDialogPane().lookupButton(confirmAddButtonType);
            confirmAddButton.disableProperty().bind(
                    Bindings.createBooleanBinding(
                            () -> inputFields.values().stream().anyMatch(f -> f.getText().isBlank()),
                            inputFields.values().stream().map(TextField::textProperty).toArray(Observable[]::new)
                    )
            );
        });

        this.setResultConverter(dialogButton -> {
            if (dialogButton.equals(confirmAddButtonType)) {
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
