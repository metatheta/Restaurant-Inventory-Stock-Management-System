package gui.controllers.transactions.dish;

public record StockLocation(int id, String name, String address) {
    @Override
    public String toString() {
        return name + " (" + address + ")";
    }
}