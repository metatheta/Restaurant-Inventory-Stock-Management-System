package gui.controllers.transactions.disposal;

import db.DBInteractor;
import gui.ScreenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Optional;

public class DisposalMenuController {

    private static DBInteractor DB;

    @FXML
    public void initialize() {
        DB = new DBInteractor();
    }

    public void disposeItems() {
        DB.enteringTransaction3();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Disposal");
        alert.setHeaderText("Dispose of expired items?");
        alert.setContentText("This cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            DB.enteringTransaction3();
        }
    }

    public void viewRecentDisposal() {
        ResultSet rs = DB.displayRecentlyDisposedItems();
        String[] columnNames = {"Item Name", "Category", "Storage Name", "Address"};
        try {
            ScreenManager.SINGLETON.loadReadOnlyTableScreen(rs, "Recently Disposed Items", columnNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void viewAllDisposals() {
        ResultSet rs = DB.displayAllDisposedItems();
        String[] columnNames = {"Item Name", "Category", "Storage Name", "Address"};
        try {
            ScreenManager.SINGLETON.loadReadOnlyTableScreen(rs, "All Disposed Items", columnNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void returnToMainMenu() {
        try {
            DB.exitingTransaction3();
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
