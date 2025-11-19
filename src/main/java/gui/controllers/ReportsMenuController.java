package gui.controllers;

import gui.ScreenManager;

public class ReportsMenuController {

    public void preferredSuppliers() {

    }

    public void storageTypeDistribution() {

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
