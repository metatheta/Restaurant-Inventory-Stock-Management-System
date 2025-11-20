package gui.controllers.transactions.products;

import db.DBInteractor;
import gui.ScreenManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.control.SearchableComboBox;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.ResultSet;
import java.util.Optional;

public class ProductRegistryMenuController {

    @FXML
    private SearchableComboBox<RegistrySupplierOption> supplierSelector;
    @FXML
    private SearchableComboBox<StockItemOption> itemSelector;
    @FXML
    private TextField unitCostField;

    private final DBInteractor db = new DBInteractor();

    @FXML
    public void initialize() {
        loadSuppliers();
        loadStockItems();
        addNumericListener(unitCostField, false);
    }

    private void loadSuppliers() {
        RegistrySupplierOption current = supplierSelector.getValue();
        supplierSelector.getSelectionModel().clearSelection();
        supplierSelector.getItems().clear();

        ResultSet rs = db.getActiveSuppliers();
        if (rs != null) {
            try {
                while (rs.next()) {
                    supplierSelector.getItems().add(new RegistrySupplierOption(
                            rs.getInt("supplier_id"),
                            rs.getString("name")
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (current != null) {
            supplierSelector.getItems().stream()
                    .filter(s -> s.id() == current.id())
                    .findFirst()
                    .ifPresent(s -> supplierSelector.setValue(s));
        }
    }

    private void loadStockItems() {
        StockItemOption current = itemSelector.getValue();
        itemSelector.getSelectionModel().clearSelection();
        itemSelector.getItems().clear();

        ResultSet rs = db.getActiveStockItems();
        if (rs != null) {
            try {
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

        if (current != null) {
            itemSelector.getItems().stream()
                    .filter(item -> item.itemId() == current.itemId())
                    .findFirst()
                    .ifPresent(item -> itemSelector.setValue(item));
        }
    }

    @FXML
    private void onAddSupplier() {
        AddSupplierDialog dialog = new AddSupplierDialog();
        Optional<AddSupplierDialog.SupplierDBItem> result = dialog.showAndWait();

        result.ifPresent(newSupplier -> {
            loadSuppliers();
            supplierSelector.getItems().stream()
                    .filter(s -> s.id() == newSupplier.id())
                    .findFirst()
                    .ifPresent(s -> supplierSelector.setValue(s));

            showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier added: " + newSupplier.name());
        });
    }

    @FXML
    private void onAddNewItem() {
        AddItemDialog dialog = new AddItemDialog();
        Optional<AddItemDialog.ItemDBItem> result = dialog.showAndWait();

        result.ifPresent(newItem -> {
            loadStockItems();
            itemSelector.getItems().stream()
                    .filter(item -> item.itemId() == newItem.id())
                    .findFirst()
                    .ifPresent(item -> itemSelector.setValue(item));

            showAlert(Alert.AlertType.INFORMATION, "Success", "New stock item created: " + newItem.name());
        });
    }

    @FXML
    private void onConfirmSelection() {
        RegistrySupplierOption selectedSupplier = supplierSelector.getValue();
        StockItemOption selectedItem = itemSelector.getValue();
        String costText = unitCostField.getText();

        if (selectedSupplier == null || selectedItem == null || costText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please fill in all fields.");
            return;
        }

        double unitCost = Double.parseDouble(costText);

        if (db.checkSupplierProductExists(selectedSupplier.id, selectedItem.itemId)) {
            boolean confirm = showConfirm("Update Existing",
                    "This supplier already sells this item.\nDo you want to update the price?");

            if (confirm) {
                db.updateSupplierProduct(selectedSupplier.id, selectedItem.itemId, unitCost);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product details updated.");
                clearFields();
            }
        } else if (db.checkInvisibleRecordExists(selectedSupplier.id, selectedItem.itemId)) {
            db.registerSupplierProduct(selectedSupplier.id, selectedItem.itemId, unitCost, true);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product re-registered (was previously deleted).");
            clearFields();
        } else {
            db.registerSupplierProduct(selectedSupplier.id, selectedItem.itemId, unitCost, false);
            showAlert(Alert.AlertType.INFORMATION, "Success", "New product registered to supplier.");
            clearFields();
        }
    }

    @FXML
    public void onViewRecords() {
        ResultSet rs = db.getRegistryHistory();
        if (rs != null) {
            try {
                CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
                crs.populate(rs);
                ScreenManager.SINGLETON.loadReadOnlyTableScreen(crs,
                        "Product Registry History",
                        "Supplier", "Item", "New Cost", "Action", "Date");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        unitCostField.clear();
        unitCostField.requestFocus();
    }

    private void addNumericListener(TextField tf, boolean integerOnly) {
        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (integerOnly) {
                if (!newValue.matches("\\d*")) {
                    tf.setText(newValue.replaceAll("[^\\d]", ""));
                }
            } else {
                if (!newValue.matches("\\d*([\\.]\\d*)?")) {
                    tf.setText(oldValue);
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private record StockItemOption(int itemId, String itemName, String unit) {
        @Override
        public String toString() {
            return itemName + " (" + unit + ")";
        }
    }

    private record RegistrySupplierOption(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}