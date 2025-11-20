package gui.controllers.transactions.dish;

import db.DBInteractor;
import db.Query;
import gui.ScreenManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.SearchableComboBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DishMenuController {

    @FXML
    private SearchableComboBox<Dish> dishComboBox;
    @FXML
    private CheckComboBox<StockLocation> locationCheckComboBox;
    @FXML
    private Spinner<Integer> quantitySpinner;

    private final DBInteractor db = new DBInteractor();

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1);
        quantitySpinner.setValueFactory(valueFactory);

        loadDishes();
        loadLocations();
    }

    private void loadDishes() {
        try {
            ResultSet rs = db.getAllDishes();
            while (rs.next()) {
                dishComboBox.getItems().add(new Dish(rs.getInt("dish_id"), rs.getString("dish_name")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load dishes: " + e.getMessage());
        }
    }

    private void loadLocations() {
        try {
            ResultSet rs = db.getAllLocations();
            while (rs.next()) {
                locationCheckComboBox.getItems().add(new StockLocation(
                        rs.getInt("location_id"),
                        rs.getString("storage_name"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load locations: " + e.getMessage());
        }
    }

    @FXML
    private void onConfirmSelection() {
        Dish selectedDish = dishComboBox.getValue();
        List<StockLocation> selectedLocations = locationCheckComboBox.getCheckModel().getCheckedItems();
        Integer quantityVal = quantitySpinner.getValue();
        int quantityToMake = (quantityVal != null) ? quantityVal : 0;

        if (selectedDish == null || selectedLocations.isEmpty() || quantityToMake <= 0) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please select a dish, at least one kitchen, and a quantity.");
            return;
        }

        Connection conn = null;
        try {
            conn = ScreenManager.getConnection();
            conn.setAutoCommit(false);

            List<Requirement> requirements = getRequirementsForDish(conn, selectedDish.id());
            List<Integer> locationIds = selectedLocations.stream().map(StockLocation::id).toList();

            for (Requirement req : requirements) {
                double totalNeeded = req.quantityRequired() * quantityToMake;
                deductInventoryFromSelectedLocationsFEFO(conn, req.itemId(), locationIds, totalNeeded);
            }

            try (PreparedStatement ps = conn.prepareStatement(Query.recordDishConsumption())) {
                ps.setInt(1, selectedDish.id());
                ps.setInt(2, quantityToMake);
                ps.setInt(3, locationIds.get(0));
                ps.executeUpdate();
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Dish recorded! Ingredients pulled from selected kitchens.");
            quantitySpinner.getValueFactory().setValue(1);
            locationCheckComboBox.getCheckModel().clearChecks();

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "Transaction Failed", e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deductInventoryFromSelectedLocationsFEFO(Connection conn, int itemId, List<Integer> locationIds, double totalNeeded) throws SQLException {
        if (locationIds.isEmpty()) return;

        String placeholders = locationIds.stream().map(id -> "?").collect(Collectors.joining(","));

        String selectMultipleLocations = "SELECT inventory_id, running_balance, expiry_date " +
                "FROM inventory " +
                "WHERE item_id = ? " +
                "AND location_id IN (" + placeholders + ") " +
                "AND running_balance > 0 " +
                "AND visible = 1 " +
                "ORDER BY (expiry_date IS NULL), expiry_date ASC, inventory_id ASC";

        try (PreparedStatement ps = conn.prepareStatement(selectMultipleLocations)) {
            ps.setInt(1, itemId);

            int paramIndex = 2;
            for (Integer locId : locationIds) {
                ps.setInt(paramIndex++, locId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                double remainingToDeduct = totalNeeded;

                while (rs.next() && remainingToDeduct > 0) {
                    int inventoryId = rs.getInt("inventory_id");
                    double currentBalance = rs.getDouble("running_balance");

                    double deductAmount;
                    if (currentBalance >= remainingToDeduct) {
                        deductAmount = remainingToDeduct;
                        remainingToDeduct = 0;
                    } else {
                        deductAmount = currentBalance;
                        remainingToDeduct -= currentBalance;
                    }

                    try (PreparedStatement updatePs = conn.prepareStatement(Query.deductFromInventoryBatch())) {
                        updatePs.setDouble(1, deductAmount);
                        updatePs.setInt(2, inventoryId);
                        updatePs.executeUpdate();
                    }
                }

                if (remainingToDeduct > 0.0001) {
                    throw new RuntimeException("Insufficient stock across selected kitchens for Item ID: " + itemId);
                }
            }
        }
    }

    private List<Requirement> getRequirementsForDish(Connection conn, int dishId) throws SQLException {
        List<Requirement> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(Query.getDishRequirements())) {
            ps.setInt(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Requirement(rs.getInt("item_id"), rs.getDouble("quantity")));
                }
            }
        }
        return list;
    }

    @FXML
    private void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewDishes() {
        ResultSet rs = db.getDishIngredients();
        try {
            ScreenManager.SINGLETON.loadReadOnlyTableScreen(rs, "Dishes and Ingredients", "Dish Name", "Ingredient", "Quantity", "Unit of Measure");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onViewRecords() {
        ResultSet rs = db.viewDishRecords();
        try {
            ScreenManager.SINGLETON.loadReadOnlyTableScreen(rs, "Dish Creations", "Created At", "Dish Name",
                    "Servings", "Storage Location", "Restaurant Address");
        } catch (IOException e) {
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
}