package gui.controllers.transactions.dish;

public record Dish(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
