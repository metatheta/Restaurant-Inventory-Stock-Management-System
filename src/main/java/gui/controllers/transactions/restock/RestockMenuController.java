package gui.controllers.transactions.restock;

import db.Query;
import gui.ScreenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.controlsfx.control.ListSelectionView;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestockMenuController {
    @FXML
    private ListSelectionView<RestockSelection> selectionView;

    @FXML
    public void initialize() {
        loadItems();
    }

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

    private void processOrders(List<RestockEntry> entries) {
        List<RestockEntry> validOrders = entries.stream()
                .filter(e -> e.getQuantityInt() > 0)
                .toList();

        if (validOrders.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "No items had a quantity greater than 0.");
            return;
        }

        Map<SupplierOption, List<RestockEntry>> ordersBySupplier = validOrders.stream()
                .collect(Collectors.groupingBy(RestockEntry::getSelectedSupplier));

        Connection conn = ScreenManager.getConnection();

        try {
            conn.setAutoCommit(false);

            String createPurchaseSql = "INSERT INTO purchases (order_date, receive_date, total_cost, supplier_id) VALUES (?, ?, ?, ?)";
            String purchaseLineSql = "INSERT INTO purchase_line (quantity, purchase_id, item_id, inventory_id) VALUES (?, ?, ?, ?)";
            String updateInventorySql = "UPDATE inventory SET running_balance = running_balance + ? WHERE inventory_id = ?";
            String insertMovementSql = "INSERT INTO stock_movement (quantity, transaction_type, item_id, location_id, inventory_id) " +
                    "SELECT ?, 'RESTOCK', item_id, location_id, inventory_id FROM inventory WHERE inventory_id = ?";

            try (PreparedStatement psPurchase = conn.prepareStatement(createPurchaseSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psLine = conn.prepareStatement(purchaseLineSql);
                 PreparedStatement psInv = conn.prepareStatement(updateInventorySql);
                 PreparedStatement psMove = conn.prepareStatement(insertMovementSql)) {

                for (Map.Entry<SupplierOption, List<RestockEntry>> entry : ordersBySupplier.entrySet()) {
                    SupplierOption supplier = entry.getKey();
                    List<RestockEntry> items = entry.getValue();


                    double totalCost = items.stream()
                            .mapToDouble(e -> e.getQuantityInt() * supplier.unitCost())
                            .sum();

                    psPurchase.setDate(1, Date.valueOf(LocalDate.now()));
                    psPurchase.setDate(2, Date.valueOf(LocalDate.now()));
                    psPurchase.setDouble(3, totalCost);
                    psPurchase.setInt(4, supplier.id());
                    psPurchase.executeUpdate();

                    int purchaseId;
                    try (ResultSet generatedKeys = psPurchase.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            purchaseId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating purchase failed, no ID obtained.");
                        }
                    }

                    for (RestockEntry item : items) {
                        int qty = item.getQuantityInt();
                        int invId = item.getOriginal().inventoryId();
                        int itemId = item.getOriginal().itemId();

                        psLine.setInt(1, qty);
                        psLine.setInt(2, purchaseId);
                        psLine.setInt(3, itemId);
                        psLine.setInt(4, invId);
                        psLine.addBatch();

                        psInv.setDouble(1, qty);
                        psInv.setInt(2, invId);
                        psInv.addBatch();

                        psMove.setInt(1, qty);
                        psMove.setInt(2, invId);
                        psMove.addBatch();
                    }
                }

                psLine.executeBatch();
                psInv.executeBatch();
                psMove.executeBatch();

                conn.commit();
                loadItems();

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
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