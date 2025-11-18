package gui.controllers.tables;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

import java.util.Map;
import java.util.function.Consumer;

public class EditDeleteRelatedCell extends TableCell<Map<String, Object>, Void> {
    private final HBox pane;
    private final Button editButton;
    private final Button deleteButton;
    private final Button viewButton;

    private final Consumer<Map<String, Object>> onEdit;
    private final Consumer<Map<String, Object>> onDelete;
    private final Consumer<Map<String, Object>> onView;

    public EditDeleteRelatedCell(Consumer<Map<String, Object>> onEdit, Consumer<Map<String, Object>> onDelete, Consumer<Map<String, Object>> onView) {
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onView = onView;

        this.editButton = new Button("Edit");
        this.deleteButton = new Button("Delete");
        this.viewButton = new Button("View Related");

        this.editButton.getStyleClass().add("edit-button");
        this.deleteButton.getStyleClass().add("delete-button");
        this.viewButton.getStyleClass().add("view-button");

        this.pane = new HBox(8, editButton, viewButton, deleteButton);
        HBox.setMargin(editButton, new Insets(10, 0, 10, 0));
        HBox.setMargin(viewButton, new Insets(10, 0, 10, 0));
        HBox.setMargin(deleteButton, new Insets(10, 0, 10, 0));
        this.pane.setAlignment(Pos.CENTER);

        setButtonActions();
    }

    private void setButtonActions() {
        editButton.setOnAction(event -> {
            if (onEdit != null) {
                Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                onEdit.accept(rowData);
            }
        });

        deleteButton.setOnAction(event -> {
            if (onDelete != null) {
                Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                onDelete.accept(rowData);
            }
        });
        viewButton.setOnAction(event -> {
            if (onView != null) {
                Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                onView.accept(rowData);
            }
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(pane);
        }
    }
}
