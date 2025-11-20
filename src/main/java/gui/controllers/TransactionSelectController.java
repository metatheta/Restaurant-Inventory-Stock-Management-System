package gui.controllers;

import gui.ScreenManager;

public class TransactionSelectController {
    public void restockItems() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/transactions/restock-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void purchaseNewStock() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/transactions/product-registry.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void disposeUnusedStock() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/transactions/disposal-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void createDish() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/transactions/dish-menu.fxml");
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
