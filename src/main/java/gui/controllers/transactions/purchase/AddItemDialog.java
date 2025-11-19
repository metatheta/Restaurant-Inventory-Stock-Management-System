package gui.controllers.transactions.purchase;

import gui.ScreenManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.*;

public class AddItemDialog extends Dialog<AddItemDialog.ItemDBItem> {

    private final TextField nameField;
    private final TextField unitField;
    private final ComboBox<String> categoryBox;

    public AddItemDialog() {
        this.setTitle("Add New Stock Item");
        this.setHeaderText("Enter details for new item:");

        ButtonType saveBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        nameField = new TextField();
        nameField.setPromptText("e.g., Red Onion");

        unitField = new TextField();
        unitField.setPromptText("e.g., kg, liter, pcs");

        categoryBox = new ComboBox<>();
        categoryBox.setEditable(true);
        categoryBox.getItems().addAll("Vegetable", "Meat", "Dairy", "Condiment", "Grain");
        categoryBox.setPromptText("Select Category");

        grid.add(new Label("Item Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Unit:"), 0, 1);
        grid.add(unitField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryBox, 1, 2);

        this.getDialogPane().setContent(grid);

        this.setResultConverter(btn -> {
            if (btn == saveBtn) return createItemInDb();
            return null;
        });
    }

    private ItemDBItem createItemInDb() {
        String name = nameField.getText();
        String unit = unitField.getText();
        String cat = categoryBox.getValue();

        if (name.isEmpty() || unit.isEmpty() || cat == null) return null;

        String sql = "INSERT INTO stock_items (item_name, unit_of_measure, category) VALUES (?, ?, ?)";

        try (Connection conn = ScreenManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, unit);
            ps.setString(3, cat);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new ItemDBItem(rs.getInt(1), name, unit);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public record ItemDBItem(int id, String name, String unit) {
        @Override
        public String toString() {
            return name + " (" + unit + ")";
        }
    }
}