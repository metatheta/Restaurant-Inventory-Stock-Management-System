package gui.controllers.transactions.products;

import gui.ScreenManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.*;

public class AddSupplierDialog extends Dialog<AddSupplierDialog.SupplierDBItem> {

    private final TextField nameField;
    private final TextField contactNameField;
    private final TextField contactInfoField;

    public AddSupplierDialog() {
        this.setTitle("Add New Supplier");
        this.setHeaderText("Enter details for new supplier:");

        ButtonType saveBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        nameField = new TextField();
        nameField.setPromptText("Company Name");
        contactNameField = new TextField();
        contactNameField.setPromptText("Contact Person");
        contactInfoField = new TextField();
        contactInfoField.setPromptText("Phone/Email");

        grid.add(new Label("Company Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact Person:"), 0, 1);
        grid.add(contactNameField, 1, 1);
        grid.add(new Label("Contact Info:"), 0, 2);
        grid.add(contactInfoField, 1, 2);

        this.getDialogPane().setContent(grid);

        this.setResultConverter(btn -> {
            if (btn == saveBtn) return createSupplierInDb();
            return null;
        });
    }

    private SupplierDBItem createSupplierInDb() {
        String name = nameField.getText();
        String person = contactNameField.getText();
        String info = contactInfoField.getText();

        if (name.isEmpty()) return null;

        String sql = "INSERT INTO suppliers (name, contact_person, contact_info) VALUES (?, ?, ?)";

        try (Connection conn = ScreenManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, person);
            ps.setString(3, info);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new SupplierDBItem(rs.getInt(1), name);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public record SupplierDBItem(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}