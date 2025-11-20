package gui.controllers.transactions.products;

import gui.ScreenManager;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.*;

public class AssignSupplierDialog extends Dialog<Boolean> {

    private final int itemId;
    private final ComboBox<SupplierDBItem> supplierCombo;
    private final TextField costField;
    private final TextField amountField;

    public AssignSupplierDialog(int itemId, String itemName) {
        this.itemId = itemId;
        this.setTitle("Assign Supplier");
        this.setHeaderText("Assign supplier for: " + itemName);

        ButtonType saveBtn = new ButtonType("Save Link", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        supplierCombo = new ComboBox<>();
        supplierCombo.setPromptText("Select Existing Supplier...");
        supplierCombo.setPrefWidth(300);
        loadAllSuppliers();

        Button createNewSupplierBtn = new Button("Create New Supplier");
        createNewSupplierBtn.setOnAction(e -> showCreateSupplierPopup());

        VBox supplierBox = new VBox(5, supplierCombo, createNewSupplierBtn);

        costField = new TextField();
        costField.setPromptText("Unit Cost");
        amountField = new TextField();
        amountField.setPromptText("Default Amount");

        grid.add(new Label("Supplier:"), 0, 0);
        grid.add(supplierBox, 1, 0);
        grid.add(new Label("Unit Cost:"), 0, 1);
        grid.add(costField, 1, 1);
        grid.add(new Label("Base Amount:"), 0, 2);
        grid.add(amountField, 1, 2);

        this.getDialogPane().setContent(grid);

        final Button btOk = (Button) this.getDialogPane().lookupButton(saveBtn);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            if (supplierCombo.getValue() == null ||
                    costField.getText().trim().isEmpty() ||
                    amountField.getText().trim().isEmpty()) {
                event.consume();
            }
        });

        this.setResultConverter(btn -> {
            if (btn == saveBtn) {
                return saveLinkToDB();
            }
            return false;
        });
    }

    private void loadAllSuppliers() {
        supplierCombo.getItems().clear();
        try (Connection conn = ScreenManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT supplier_id, name FROM suppliers WHERE visible = 1")) {
            while (rs.next()) {
                supplierCombo.getItems().add(new SupplierDBItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCreateSupplierPopup() {
        new AddSupplierDialog().showAndWait().ifPresent(newSupplier -> {
            loadAllSuppliers();
            supplierCombo.getItems().stream()
                    .filter(s -> s.id == newSupplier.id())
                    .findFirst()
                    .ifPresent(supplierCombo::setValue);
        });
    }

    private boolean saveLinkToDB() {
        try {
            double cost = Double.parseDouble(costField.getText().trim());
            int amount = Integer.parseInt(amountField.getText().trim());
            int supplierId = supplierCombo.getValue().id;

            String sql = "INSERT INTO supplier_products (supplier_id, item_id, unit_cost, amount) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE unit_cost = ?, amount = ?";

            try (Connection conn = ScreenManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, supplierId);
                ps.setInt(2, itemId);
                ps.setDouble(3, cost);
                ps.setInt(4, amount);
                ps.setDouble(5, cost);
                ps.setInt(6, amount);
                ps.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private record SupplierDBItem(int id, String name) {
        @Override public String toString() { return name; }
    }
}