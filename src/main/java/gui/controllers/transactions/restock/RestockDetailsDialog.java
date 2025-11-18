package gui.controllers.transactions.restock;

import gui.ScreenManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestockDetailsDialog extends Dialog<List<RestockEntry>> {

    private final TableView<RestockEntry> table = new TableView<>();

    public RestockDetailsDialog(List<RestockSelection> selectedItems) {
        this.setTitle("Restock Details");
        this.setHeaderText("Select Supplier and Quantity for each item");

        ButtonType confirmButton = new ButtonType("Confirm Order", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

        Map<Integer, List<SupplierOption>> supplierMap = fetchSuppliers(selectedItems);

        ObservableList<RestockEntry> data = FXCollections.observableArrayList();
        for (RestockSelection item : selectedItems) {
            List<SupplierOption> opts = supplierMap.getOrDefault(item.itemId(), new ArrayList<>());
            data.add(new RestockEntry(item, opts));
        }

        setupTable();
        table.setItems(data);
        table.setEditable(true);
        table.setPrefWidth(800);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        this.getDialogPane().setContent(table);

        this.setResultConverter(btn -> {
            if (btn == confirmButton) {
                return data.stream().toList();
            }
            return null;
        });
    }

    private void setupTable() {
        TableColumn<RestockEntry, String> colItem = new TableColumn<>("Item");
        colItem.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getOriginal().name()));
        colItem.setPrefWidth(200);

        TableColumn<RestockEntry, SupplierOption> colSupplier = new TableColumn<>("Supplier");
        colSupplier.setPrefWidth(300);
        colSupplier.setCellValueFactory(cell -> cell.getValue().selectedSupplierProperty());

        colSupplier.setCellFactory(param -> new ComboBoxTableCell<>() {
            @Override
            public void startEdit() {
                RestockEntry row = getTableRow().getItem();
                if (row != null) {
                    this.getItems().setAll(row.getAvailableSuppliers());
                }
                super.startEdit();
            }
        });

        colSupplier.setOnEditCommit(e -> e.getRowValue().setSelectedSupplier(e.getNewValue()));

        TableColumn<RestockEntry, String> colQty = new TableColumn<>("Quantity");
        colQty.setPrefWidth(100);
        colQty.setCellValueFactory(cell -> cell.getValue().quantityProperty());
        colQty.setCellFactory(TextFieldTableCell.forTableColumn());
        colQty.setOnEditCommit(e -> {
            if (e.getNewValue().matches("\\d+")) {
                e.getRowValue().setQuantity(e.getNewValue());
            } else {
                table.refresh();
            }
        });

        table.getColumns().addAll(colItem, colSupplier, colQty);
    }

    private Map<Integer, List<SupplierOption>> fetchSuppliers(List<RestockSelection> items) {
        Map<Integer, List<SupplierOption>> map = new HashMap<>();
        if (items.isEmpty()) return map;

        Connection conn = ScreenManager.getConnection();

        String placeholders = items.stream().map(i -> "?").collect(Collectors.joining(","));

        String sql = "SELECT s.supplier_id, s.name, s.contact_person, " +
                "p.item_id, p.amount, p.unit_cost " +
                "FROM suppliers s " +
                "LEFT JOIN supplier_products p USING (supplier_id) " +
                "WHERE p.item_id IN (" + placeholders + ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (RestockSelection item : items) {
                ps.setInt(i++, item.itemId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int itemId = rs.getInt("item_id");

                    SupplierOption sup = new SupplierOption(
                            rs.getInt("supplier_id"),
                            rs.getString("name"),
                            rs.getString("contact_person"),
                            rs.getInt("amount"),
                            rs.getDouble("unit_cost")
                    );
                    map.computeIfAbsent(itemId, k -> new ArrayList<>()).add(sup);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}