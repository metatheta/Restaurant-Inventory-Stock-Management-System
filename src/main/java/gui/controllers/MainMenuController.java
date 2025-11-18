package gui.controllers;

import gui.view.ScreenManager;
import java.io.IOException;
import java.sql.SQLException;

public class MainMenuController {
    public void tableSelectMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/tables/table-selection.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void transactionMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("");
            // TODO
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void reportsMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("");
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
