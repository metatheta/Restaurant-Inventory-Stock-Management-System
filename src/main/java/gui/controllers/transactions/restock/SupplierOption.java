package gui.controllers.transactions.restock;

public record SupplierOption(int id, String name, String contact, int availableQuantity, double unitCost) {
    @Override
    public String toString() {
        return String.format("%s | (%.2f) per unit", name, unitCost);
    }
}