package gui.controllers.transactions.products;

import gui.ScreenManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.*;

public class AddLocationDialog extends Dialog<AddLocationDialog.LocationDBItem> {

    private final TextField nameField;
    private final TextField addressField;
    private final ComboBox<String> typeBox;

    public AddLocationDialog() {
        this.setTitle("Add New Storage Location");
        this.setHeaderText("Enter details for new location:");

        ButtonType saveBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        nameField = new TextField();
        nameField.setPromptText("e.g., Freezer 2");

        addressField = new TextField();
        addressField.setPromptText("e.g., 123 Main St.");

        typeBox = new ComboBox<>();
        typeBox.setEditable(true);
        typeBox.getItems().addAll("Box", "Sack", "Tupperware", "Ziplock", "Barrel", "Jug", "Shelf", "Airlock");
        typeBox.setPromptText("Select Type");

        grid.add(new Label("Storage Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Storage Type:"), 0, 2);
        grid.add(typeBox, 1, 2);

        this.getDialogPane().setContent(grid);

        this.setResultConverter(btn -> {
            if (btn == saveBtn) {
                return createLocationInDb();
            }
            return null;
        });
    }

    private LocationDBItem createLocationInDb() {
        String name = nameField.getText();
        String address = addressField.getText();
        String type = typeBox.getSelectionModel().getSelectedItem();

        if (name.isEmpty() || type == null) return null;

        String sql = "INSERT INTO stock_locations (storage_name, address, storage_type) VALUES (?, ?, ?)";

        try (Connection conn = ScreenManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, type);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new LocationDBItem(rs.getInt(1), name, address);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public record LocationDBItem(int id, String name, String address) {
        @Override
        public String toString() {
            return name + " (" + address + ")";
        }
    }
}