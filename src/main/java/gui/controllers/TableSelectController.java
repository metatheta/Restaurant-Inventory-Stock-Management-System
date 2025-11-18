package gui.controllers;

public class TableSelectController {

    public void inventoryTable() {
        try {
            String[] columnNames = {"Running Balance", "Last Restock Date", "Expiry Date"};
            gui.view.ScreenManager.SINGLETON.loadTableScreen("inventory", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stockItemsTable() {
        try {
            String[] columnNames = {"Item Name", "Unit of Measure", "Category"};
            gui.view.ScreenManager.SINGLETON.loadTableScreen("stock_items", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stockLocationsTable() {
        try {
            String[] columnNames = {"Storage Name", "Address", "Storage Type"};
            gui.view.ScreenManager.SINGLETON.loadTableScreen("stock_locations", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void suppliersTable() {
        try {
            String[] columnNames = {"Name", "Contact Person", "Contact Info"};
            gui.view.ScreenManager.SINGLETON.loadTableScreen("suppliers", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void returnToMainMenu() {
        try {
            gui.view.ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
