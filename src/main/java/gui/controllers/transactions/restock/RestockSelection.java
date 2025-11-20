package gui.controllers.transactions.restock;

import javafx.beans.property.*;

public class RestockSelection {
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    private final int inventoryId;
    private final int itemId;
    private final StringProperty name;
    private final StringProperty storage;
    private final StringProperty address;
    private final DoubleProperty balance;

    public RestockSelection(int inventoryId, int itemId, String name, String storage, String address, double balance) {
        this.inventoryId = inventoryId;
        this.itemId = itemId;
        this.name = new SimpleStringProperty(name);
        this.storage = new SimpleStringProperty(storage);
        this.address = new SimpleStringProperty(address);
        this.balance = new SimpleDoubleProperty(balance);
    }

    public BooleanProperty selectedProperty() { return selected; }
    public StringProperty nameProperty() { return name; }
    public StringProperty storageProperty() { return storage; }
    public StringProperty addressProperty() { return address; }
    public DoubleProperty balanceProperty() { return balance; }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean val) { selected.set(val); }
    public int inventoryId() { return inventoryId; }
    public int itemId() { return itemId; }
    public String name() { return name.get(); }
    public String storage() { return storage.get(); }
    public String address() { return address.get(); }
    public double balance() { return balance.get(); }
}