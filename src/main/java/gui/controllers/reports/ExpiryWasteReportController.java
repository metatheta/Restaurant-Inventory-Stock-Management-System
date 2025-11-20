package gui.controllers.reports;

import db.DBInteractor;
import gui.ScreenManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.sql.ResultSet;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.Locale;

public class ExpiryWasteReportController {

    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private ComboBox<String> monthComboBox;

    @FXML
    public void initialize() {
        populateYears();
        populateMonths();
    }

    private void populateYears() {
        int currentYear = Year.now().getValue();

        for (int i = currentYear - 8; i <= currentYear + 1; i++) {
            yearComboBox.getItems().add(i);
        }

        yearComboBox.setValue(currentYear);
    }

    private void populateMonths() {
        for (Month month : Month.values()) {
            String monthName = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            monthComboBox.getItems().add(monthName);
        }
        String currentMonth = java.time.LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthComboBox.setValue(currentMonth);
    }

    @FXML
    private void onViewReport() {
        Integer selectedYear = yearComboBox.getValue();
        String selectedMonth = monthComboBox.getValue();

        if (selectedYear == null || selectedMonth == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Missing", "Please select both a year and a month.");
            return;
        }

        int monthIndex = Month.valueOf(selectedMonth.toUpperCase()).getValue();

        DBInteractor db = new DBInteractor();
        ResultSet rs = db.report2(selectedYear, monthIndex);

        try {
            String reportTitle = "Report for " + selectedMonth + " " + selectedYear;
            ScreenManager.SINGLETON.loadReadOnlyTableScreen(
                    rs,
                    reportTitle,
                    "Item Name", "Storage Name", "Address", "Total Restocked", "Total Consumed/Disposed"
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