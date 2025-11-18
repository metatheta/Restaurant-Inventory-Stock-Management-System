package gui.controllers.transactions.restock;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

public class RestockEntry {
    private final RestockSelection original;
    private final List<SupplierOption> availableSuppliers;
    private final ObjectProperty<SupplierOption> selectedSupplier = new SimpleObjectProperty<>();
    private final StringProperty quantity = new SimpleStringProperty("0");

    public RestockEntry(RestockSelection original, List<SupplierOption> availableSuppliers) {
        this.original = original;
        this.availableSuppliers = availableSuppliers;
        if (!availableSuppliers.isEmpty()) {
            this.selectedSupplier.set(availableSuppliers.getFirst());
        }
    }

    public RestockSelection getOriginal() {
        return original;
    }

    public List<SupplierOption> getAvailableSuppliers() {
        return availableSuppliers;
    }

    public ObjectProperty<SupplierOption> selectedSupplierProperty() {
        return selectedSupplier;
    }

    public void setSelectedSupplier(SupplierOption s) {
        this.selectedSupplier.set(s);
    }

    public SupplierOption getSelectedSupplier() {
        return selectedSupplier.get();
    }

    public StringProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(String q) {
        this.quantity.set(q);
    }

    public int getQuantityInt() {
        try {
            return Integer.parseInt(quantity.get());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}