package gui.controllers.reports;

import db.DBInteractor;
import gui.ScreenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.Year;

public class SeasonalStockReportController {

    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private ComboBox<String> seasonComboBox;

    @FXML
    public void initialize() {
        populateYears();
        populateSeasons();
    }

    private void populateYears() {
        int currentYear = Year.now().getValue();

        for (int i = currentYear - 8; i <= currentYear + 1; i++) {
            yearComboBox.getItems().add(i);
        }

        yearComboBox.setValue(currentYear);
    }

    private void populateSeasons() {
        seasonComboBox.getItems().addAll("Spring", "Summer", "Autumn", "Winter");
        int currentMonth = LocalDate.now().getMonthValue();
        String currentSeason;

        if (currentMonth >= 3 && currentMonth <= 5) {
            currentSeason = "Spring";
        } else if (currentMonth >= 6 && currentMonth <= 8) {
            currentSeason = "Summer";
        } else if (currentMonth >= 9 && currentMonth <= 11) {
            currentSeason = "Autumn";
        } else {
            currentSeason = "Winter";
        }

        seasonComboBox.setValue(currentSeason);
    }

    @FXML
    private void onViewReport() {
        Integer selectedYear = yearComboBox.getValue();
        String selectedSeason = seasonComboBox.getValue();

        if (selectedYear == null || selectedSeason == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Missing", "Please select both a year and a season.");
            return;
        }

        int startMonth = 0;
        int endMonth = 0;

        switch (selectedSeason) {
            case "Spring":
                startMonth = 9;
                endMonth = 11;
                break;
            case "Summer":
                startMonth = 12;
                endMonth = 2;
                break;
            case "Autumn":
                startMonth = 3;
                endMonth = 5;
                break;
            case "Winter":
                startMonth = 6;
                endMonth = 8;
                break;
        }

        DBInteractor db = new DBInteractor();
        ResultSet rs = db.report3(startMonth, endMonth, selectedYear);

        try {
            String reportTitle = "Seasonal Stock Report: " + selectedSeason + " " + selectedYear;
            ScreenManager.SINGLETON.loadReadOnlyTableScreen(
                    rs,
                    reportTitle,
                    "Item",
                    "Total Quantity Disposed",
                    "Total Quantity Used",
                    "Disposal-Used Ratio"
            );

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "IO Error", "Failed to load the report screen.");
        }
    }

    @FXML
    private void returnToMainMenu() {
        try {
            ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to main menu.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}