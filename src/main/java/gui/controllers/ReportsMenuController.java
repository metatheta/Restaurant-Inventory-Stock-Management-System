package gui.controllers;

import gui.ScreenManager;

import java.io.IOException;

public class ReportsMenuController {

    public void preferredSuppliers() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/reports/preferred-suppliers.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storageTypeDistribution() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/reports/storage-type-distribution.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seasonalStock() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/reports/seasonal-stock.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void expiryAndWaste() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/reports/expiry-and-waste.fxml");
        } catch (IOException e) {
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
