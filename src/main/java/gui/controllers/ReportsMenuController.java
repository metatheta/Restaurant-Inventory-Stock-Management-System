package gui.controllers;

import gui.ScreenManager;

import java.io.IOException;

public class ReportsMenuController {

    public void preferredSuppliers() {

    }

    public void storageTypeDistribution() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/reports/storage-type-distribution.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seasonalStock() {

    }

    public void expiryAndWaste() {

    }

    public void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
