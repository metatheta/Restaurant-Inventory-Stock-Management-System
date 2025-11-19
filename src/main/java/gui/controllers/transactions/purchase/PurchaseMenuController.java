package gui.controllers.transactions.purchase;

import gui.ScreenManager;
import gui.controllers.transactions.restock.RestockDetailsDialog;
import gui.controllers.transactions.restock.RestockMenuController;
import gui.controllers.transactions.restock.RestockSelection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.controlsfx.control.SearchableComboBox;

import java.sql.*;
import java.util.Collections;

public class PurchaseMenuController {

    @FXML
    private SearchableComboBox<StockItemOption> itemSelector;
    @FXML
    private SearchableComboBox<AddLocationDialog.LocationDBItem> locationSelector;

    @FXML
    public void initialize() {
        loadStockItems();
        loadLocations();
    }

    private void loadStockItems() {
        try (Connection conn = ScreenManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT item_id, item_name, unit_of_measure FROM stock_items WHERE visible = 1")) {

            while (rs.next()) {
                itemSelector.getItems().add(new StockItemOption(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("unit_of_measure")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLocations() {
        locationSelector.getItems().clear();
        try (Connection conn = ScreenManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT location_id, storage_name, address FROM stock_locations WHERE visible = 1")) {

            while (rs.next()) {
                locationSelector.getItems().add(new AddLocationDialog.LocationDBItem(
                        rs.getInt("location_id"),
                        rs.getString("storage_name"),
                        rs.getString("address")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddLocation() {
        AddLocationDialog dialog = new AddLocationDialog();
        dialog.showAndWait().ifPresent(newLoc -> {
            locationSelector.getItems().add(newLoc);
            locationSelector.setValue(newLoc);
        });
    }

    @FXML
    private void onConfirmSelection() {
        StockItemOption selectedItem = itemSelector.getValue();
        AddLocationDialog.LocationDBItem selectedLoc = locationSelector.getValue();

        if (selectedItem == null || selectedLoc == null) {
            new Alert(Alert.AlertType.WARNING, "Please select both an Item and a Storage Location.").show();
            return;
        }

        int inventoryId = getOrCreateInventoryId(selectedItem.itemId, selectedLoc.id());

        RestockSelection selection = new RestockSelection(
                inventoryId,
                selectedItem.itemId,
                selectedItem.itemName,
                selectedLoc.name(),
                selectedLoc.address(),
                getCurrentBalance(inventoryId)
        );

        RestockDetailsDialog dialog = new RestockDetailsDialog(Collections.singletonList(selection));

        dialog.showAndWait().ifPresent(entries -> {
            new RestockMenuController().processOrders(entries);
        });
    }

    private int getOrCreateInventoryId(int itemId, int locationId) {
        try (Connection conn = ScreenManager.getConnection()) {
            String checkSql = "SELECT inventory_id FROM inventory WHERE item_id = ? AND location_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, itemId);
                ps.setInt(2, locationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

            String insertSql = "INSERT INTO inventory (running_balance, item_id, location_id) VALUES (0, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, itemId);
                ps.setInt(2, locationId);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double getCurrentBalance(int inventoryId) {
        if (inventoryId == 0) return 0.0;
        try (Connection conn = ScreenManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT running_balance FROM inventory WHERE inventory_id = " + inventoryId)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @FXML
    public void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onViewRecords() {
        try {
            // TODO select * from item restocks in db interactor
//            ScreenManager.SINGLETON.loadReadOnlyTableScreen("item_restocks",
//                    "Restock ID", "Item", "Supplier", "Unit Cost", "Qty", "Total Cost", "Storage", "Address", "Date");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private record StockItemOption(int itemId, String itemName, String unit) {
        @Override
        public String toString() {
            return itemName + " (" + unit + ")";
        }
    }
}