package gui.controllers;

import gui.ScreenManager;

public class TableSelectController {

    public void inventoryTable() {
        try {
            String[] columnNames = {"Inventory ID", "Running Balance", "Last Restock Date", "Expiry Date", "Location " +
                    "ID", "Item ID"};
            ScreenManager.SINGLETON.loadCoreTableScreen("inventory", "Inventory", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stockItemsTable() {
        try {
            String[] columnNames = {"Item ID", "Item Name", "Unit of Measure", "Category"};
            ScreenManager.SINGLETON.loadCoreTableScreen("stock_items", "Stock Items", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stockLocationsTable() {
        try {
            String[] columnNames = {"Location ID", "Storage Name", "Address", "Storage Type"};
            ScreenManager.SINGLETON.loadCoreTableScreen("stock_locations", "Stock Locations", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void suppliersTable() {
        try {
            String[] columnNames = {"Supplier ID", "Name", "Contact Person", "Contact Info"};
            ScreenManager.SINGLETON.loadCoreTableScreen("suppliers", "Suppliers", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
