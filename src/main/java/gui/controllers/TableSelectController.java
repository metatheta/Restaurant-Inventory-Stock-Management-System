package gui.controllers;

import gui.ScreenManager;

public class TableSelectController {

    public void inventoryTable() {
        try {
            String[] columnNames = {"Running Balance", "Last Restock Date", "Expiry Date", "Location ID", "Item ID"};
            ScreenManager.SINGLETON.loadCoreTableScreen("inventory", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stockItemsTable() {
        try {
            String[] columnNames = {"Item Name", "Unit of Measure", "Category"};
            ScreenManager.SINGLETON.loadCoreTableScreen("stock_items", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stockLocationsTable() {
        try {
            String[] columnNames = {"Storage Name", "Address", "Storage Type"};
            ScreenManager.SINGLETON.loadCoreTableScreen("stock_locations", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void suppliersTable() {
        try {
            String[] columnNames = {"Name", "Contact Person", "Contact Info"};
            ScreenManager.SINGLETON.loadCoreTableScreen("suppliers", columnNames);
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
