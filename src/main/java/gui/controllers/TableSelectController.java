package gui.controllers;

public class TableSelectController {

    public void inventoryTable() {
        try {
            gui.view.ScreenManager.SINGLETON.loadTableScreen("inventory");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stockItemsTable() {
        try {
            gui.view.ScreenManager.SINGLETON.loadTableScreen("stock_items");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void stockLocationsTable() {
        try {
            gui.view.ScreenManager.SINGLETON.loadTableScreen("stock_locations");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void suppliersTable() {
        try {
            gui.view.ScreenManager.SINGLETON.loadTableScreen("suppliers");
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
