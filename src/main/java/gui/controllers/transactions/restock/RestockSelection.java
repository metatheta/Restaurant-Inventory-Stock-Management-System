package gui.controllers.transactions.restock;

public record RestockSelection(int inventoryId, int itemId, String name, String storage, String address,
                               double balance) {
    @Override
    public String toString() {
        return String.format("%s | %s (%s) | Balance: %.2f", name, storage, address, balance);
    }
}
