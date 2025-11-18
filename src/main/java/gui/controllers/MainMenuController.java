package gui.controllers;

import gui.ScreenManager;

import java.io.IOException;
import java.sql.SQLException;

public class MainMenuController {
    public void tableSelectMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/tables/table-selection.fxml");
            // TODO do viewing of a record with related records
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void transactionMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/transaction-menu.fxml");
            // TODO
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reportsMenu() {
        try {
            ScreenManager.SINGLETON.loadReadOnlyTableScreen("/gui/view/reports-menu.fxml");
            // TODO
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exitApplication() throws SQLException {
        ScreenManager.closeConnection();
        System.exit(0);
    }
}
