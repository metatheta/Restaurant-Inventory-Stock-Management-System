package gui.controllers;

import gui.ScreenManager;

public class TransactionSelectController {
    public void restockItems() {
        try {
            String[] columnNames = {"Running Balance", "Last Restock Date", "Expiry Date"};
            ScreenManager.SINGLETON.loadTableScreen("inventory", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void purchaseNewStock() {
        try {
            String[] columnNames = {"Item Name", "Unit of Measure", "Category"};
            ScreenManager.SINGLETON.loadTableScreen("stock_items", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void disposeUnusedStock() {
        try {
            String[] columnNames = {"Storage Name", "Address", "Storage Type"};
            ScreenManager.SINGLETON.loadTableScreen("stock_locations", columnNames);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void createDish() {
        try {
            String[] columnNames = {"Name", "Contact Person", "Contact Info"};
            ScreenManager.SINGLETON.loadTableScreen("suppliers", columnNames);
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
