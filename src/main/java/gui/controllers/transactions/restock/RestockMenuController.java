package gui.controllers.transactions.restock;

import db.DBInteractor;
import gui.ScreenManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public class RestockMenuController {

    @FXML private TableView<RestockSelection> table;
    @FXML private ComboBox<String> filterColumnComboBox;
    @FXML private TextField filterField;

    private ObservableList<RestockSelection> masterData = FXCollections.observableArrayList();
    private FilteredList<RestockSelection> filteredData;
    private SortedList<RestockSelection> sortedData;

    @FXML
    public void initialize() {
        setupTable();
        setupFiltering();
        loadItems();
        setupSortingDefault();
    }

    private void setupTable() {
        TableColumn<RestockSelection, Boolean> selectCol = new TableColumn<>("Select");
        selectCol.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        selectCol.setEditable(true);
        selectCol.setPrefWidth(80);
        selectCol.setMaxWidth(80);

        TableColumn<RestockSelection, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(cell -> cell.getValue().nameProperty());

        TableColumn<RestockSelection, String> locCol = new TableColumn<>("Storage");
        locCol.setCellValueFactory(cell -> cell.getValue().storageProperty());

        TableColumn<RestockSelection, String> addrCol = new TableColumn<>("Address");
        addrCol.setCellValueFactory(cell -> cell.getValue().addressProperty());

        TableColumn<RestockSelection, Number> balCol = new TableColumn<>("Balance");
        balCol.setCellValueFactory(cell -> cell.getValue().balanceProperty());

        table.getColumns().addAll(selectCol, itemCol, locCol, addrCol, balCol);
        table.setEditable(true);
    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(masterData, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        filterColumnComboBox.getItems().addAll("All", "Item", "Storage", "Address");
        filterColumnComboBox.getSelectionModel().selectFirst();

        filterField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        filterColumnComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
    }

    private void setupSortingDefault() {
        TableColumn<RestockSelection, ?> col = table.getColumns().get(4);
        // Sort by running balance asc

        col.setSortType(TableColumn.SortType.ASCENDING);

        table.getSortOrder().clear();
        table.getSortOrder().add(col);

        table.sort();
    }

    private void updateFilter() {
        String filterText = filterField.getText();
        String selectedCol = filterColumnComboBox.getValue();

        filteredData.setPredicate(item -> {
            if (filterText == null || filterText.isEmpty()) return true;

            String lowerCaseFilter = filterText.toLowerCase();

            if ("Item".equals(selectedCol)) {
                return item.name().toLowerCase().contains(lowerCaseFilter);
            } else if ("Storage".equals(selectedCol)) {
                return item.storage().toLowerCase().contains(lowerCaseFilter);
            } else if ("Address".equals(selectedCol)) {
                return item.address().toLowerCase().contains(lowerCaseFilter);
            } else {
                return item.name().toLowerCase().contains(lowerCaseFilter) ||
                        item.storage().toLowerCase().contains(lowerCaseFilter) ||
                        item.address().toLowerCase().contains(lowerCaseFilter);
            }
        });
    }

    private void loadItems() {
        if (table == null) return;

        try{
            DBInteractor dbInteractor = new DBInteractor();
            ResultSet rs = dbInteractor.getItemsToRestock();
            masterData.clear();

            while (rs.next()) {
                masterData.add(new RestockSelection(
                        rs.getInt("inventory_id"),
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("storage_name"),
                        rs.getString("address"),
                        rs.getDouble("running_balance")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load inventory items.");
        }
    }

    @FXML
    public void onConfirmSelection() {
        List<RestockSelection> selections = masterData.stream()
                .filter(RestockSelection::isSelected)
                .collect(Collectors.toList());

        if (selections.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please check the boxes of the items you want to restock.");
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
            String insertLogSql = "INSERT INTO item_restocks (item_name, supplier_name, cost_per_unit, quantity, total_cost, storage_location, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String updateInventorySql = "UPDATE inventory SET running_balance = running_balance + ? WHERE inventory_id = ?";

            try (PreparedStatement psLog = conn.prepareStatement(insertLogSql);
                 PreparedStatement psInv = conn.prepareStatement(updateInventorySql)) {

                for (RestockEntry order : validOrders) {
                    psLog.setString(1, order.getOriginal().name());
                    psLog.setString(2, order.getSelectedSupplier().name());
                    psLog.setDouble(3, order.getSelectedSupplier().unitCost());
                    psLog.setInt(4, order.getQuantityInt());
                    psLog.setDouble(5, order.getSelectedSupplier().unitCost() * order.getQuantityInt());
                    psLog.setString(6, order.getOriginal().storage());
                    psLog.setString(7, order.getOriginal().address());
                    psLog.addBatch();

                    psInv.setDouble(1, order.getQuantityInt());
                    psInv.setInt(2, order.getOriginal().inventoryId());
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