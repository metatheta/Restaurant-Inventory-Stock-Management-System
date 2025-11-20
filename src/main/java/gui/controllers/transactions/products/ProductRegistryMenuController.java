package gui.controllers.transactions.products;

import db.DBInteractor;
import gui.ScreenManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Supplier");
        dialog.setHeaderText("Enter Supplier Details");

        ButtonType loginButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField name = new TextField();
        TextField person = new TextField();
        TextField info = new TextField();

        grid.add(new Label("Company Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Contact Person:"), 0, 1);
        grid.add(person, 1, 1);
        grid.add(new Label("Contact Info:"), 0, 2);
        grid.add(info, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return name.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(companyName -> {
            db.addNewSupplier(name.getText(), person.getText(), info.getText());
            loadSuppliers();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier added successfully.");
        });
    }

    @FXML
    private void onAddNewItem() {
        AddItemDialog dialog = new AddItemDialog();
        Optional<AddItemDialog.ItemDBItem> result = dialog.showAndWait();

        result.ifPresent(newItem -> {
            loadStockItems();

            for (StockItemOption option : itemSelector.getItems()) {
                if (option.itemId() == newItem.id()) {
                    itemSelector.getSelectionModel().select(option);
                    break;
                }
            }

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