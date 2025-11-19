package gui.controllers.transactions.restock;

import db.Query;
import gui.ScreenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.controlsfx.control.ListSelectionView;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class RestockMenuController {
    @FXML
    private ListSelectionView<RestockSelection> selectionView;

    @FXML
    public void initialize() {
        loadItems();
    }

    // TODO rework into tableview

    private void loadItems() {
        Connection conn = ScreenManager.getConnection();
        String sql = Query.selectItemsToRestock();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            selectionView.getSourceItems().clear();
            selectionView.getTargetItems().clear();

            while (rs.next()) {
                RestockSelection item = new RestockSelection(
                        rs.getInt("inventory_id"),
                        rs.getInt("item_id"),
                        rs.getString("Item"),
                        rs.getString("Storage"),
                        rs.getString("Address"),
                        rs.getDouble("Running Balance")
                );
                selectionView.getSourceItems().add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load inventory items.");
        }
    }

    @FXML
    public void onConfirmSelection() {
        List<RestockSelection> selections = selectionView.getTargetItems();
        if (selections.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Items Selected", "Please move items to the right list to select them.");
            return;
        }
        RestockDetailsDialog dialog = new RestockDetailsDialog(selections);
        dialog.showAndWait().ifPresent(this::processOrders);
    }

    public void processOrders(List<RestockEntry> entries) {
        List<RestockEntry> validOrders = entries.stream()
                .filter(e -> e.getQuantityInt() > 0)
                .toList();

        if (validOrders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "No items had a quantity greater than 0.");
            return;
        }

        Connection conn = ScreenManager.getConnection();

        try {
            conn.setAutoCommit(false);
            String insertLogSql = "INSERT INTO item_restocks " +
                    "(item_name, supplier_name, cost_per_unit, quantity, total_cost, storage_location, address) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            String updateInventorySql = "UPDATE inventory SET running_balance = running_balance + ? WHERE inventory_id = ?";

            try (PreparedStatement psLog = conn.prepareStatement(insertLogSql);
                 PreparedStatement psInv = conn.prepareStatement(updateInventorySql)) {

                for (RestockEntry order : validOrders) {
                    String itemName = order.getOriginal().name();
                    String supplierName = order.getSelectedSupplier().name();
                    double unitCost = order.getSelectedSupplier().unitCost();
                    int quantity = order.getQuantityInt();
                    double totalCost = unitCost * quantity;
                    String location = order.getOriginal().storage();
                    String address = order.getOriginal().address();
                    int inventoryId = order.getOriginal().inventoryId();

                    psLog.setString(1, itemName);
                    psLog.setString(2, supplierName);
                    psLog.setDouble(3, unitCost);
                    psLog.setInt(4, quantity);
                    psLog.setDouble(5, totalCost);
                    psLog.setString(6, location);
                    psLog.setString(7, address);
                    psLog.addBatch();

                    psInv.setDouble(1, quantity);
                    psInv.setInt(2, inventoryId);
                    psInv.addBatch();
                }


                psLog.executeBatch();
                psInv.executeBatch();

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Restock recorded successfully!");
                loadItems();

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Transaction Failed", "An error occurred. No changes saved.");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}