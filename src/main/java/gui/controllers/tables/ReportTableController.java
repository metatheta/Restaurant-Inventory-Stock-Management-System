package gui.controllers.tables;

import db.DBInteractor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * ReportTableController
 *
 * This controller is used for displaying SQL reports dynamically in a JavaFX TableView.
 * It can take any SQL query, build the table columns automatically, and show the data.
 * This avoids having to manually define columns for each report.
 *
 * Features:
 * - Dynamically creates TableView columns based on the SQL query result.
 * - Populates the TableView with data from the database.
 * - Includes a "Return" button to go back to the previous screen.
 *
 * Notes for beginners:
 * - ObservableList<String>: Represents a row in the table, where each cell is a string.
 * - TableColumn<ObservableList<String>, String>: Represents a single column in the table.
 */
public class ReportTableController {

    /** The TableView in the FXML file that will display the report data */
    @FXML
    private TableView<ObservableList<String>> reportTable;

    /** Database interactor object to execute queries */
    private DBInteractor db;

    /**
     * This method runs automatically when the controller is loaded.
     * It is used to initialize objects, connections, and other setup.
     */
    @FXML
    public void initialize() {
        // Create a database interactor object
        // Pass null here for simplicity; in practice you may pass a proper Query object
        db = new DBInteractor();
    }

    // loads report 1
    public void loadReport1()
    {
        try
        {
            ResultSet rs = db.report1();

            buildTable(rs);
        }
        catch (Exception e)
        {
            System.out.println("Error loading report: " + e.getMessage());
        }
    }

    // loads report 2
    public void loadReport2()
    {
        try
        {
            ResultSet rs = db.report2();

            buildTable(rs);
        }
        catch (Exception e)
        {
            System.out.println("Error loading report: " + e.getMessage());
        }
    }

    // loads report 3
    public void loadReport3()
    {
        try
        {
            ResultSet rs = db.report3();

            buildTable(rs);
        }
        catch (Exception e)
        {
            System.out.println("Error loading report: " + e.getMessage());
        }
    }

    // loads report 4
    public void loadReport4()
    {
        try
        {
            ResultSet rs = db.report4();

            buildTable(rs);
        }
        catch (Exception e)
        {
            System.out.println("Error loading report: " + e.getMessage());
        }
    }

    /**
     * Builds the TableView from a ResultSet.
     *
     * This method does two things:
     * 1. Creates TableColumns automatically based on the query result.
     * 2. Fills the table rows with data from the ResultSet.
     *
     * @param rs The ResultSet obtained from executing a SQL query
     */
    private void buildTable(ResultSet rs) {
        try {
            // Clear any previous table columns and data
            reportTable.getColumns().clear();
            reportTable.getItems().clear();

            // Get metadata to know how many columns the query returned
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Loop through each column in the ResultSet
            for (int i = 1; i <= columnCount; i++) {
                final int colIndex = i - 1; // JavaFX columns are 0-based

                // Create a TableColumn with the SQL column name as header
                TableColumn<ObservableList<String>, String> col =
                        new TableColumn<>(meta.getColumnLabel(i));

                // Tell JavaFX how to get the value for each cell in this column
                col.setCellValueFactory(param ->
                        new SimpleStringProperty(param.getValue().get(colIndex))
                );

                // Add the column to the TableView
                reportTable.getColumns().add(col);
            }

            // ObservableList to hold all rows of data
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

            // Iterate over each row in the ResultSet
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();

                // Add each column value as a string to the row
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }

                // Add the row to the data list
                data.add(row);
            }

            // Set the TableView items to the data
            reportTable.setItems(data);

        } catch (Exception e) {
            System.out.println("Error building table: " + e.getMessage());
        }
    }

    /**
     * Handles the "Return" button click.
     * Closes the current window to return to the previous screen.
     */
    @FXML
    private void returnToMainMenu() {
        // Get the current window (stage) and close it
        Stage stage = (Stage) reportTable.getScene().getWindow();
        stage.close();
    }
}
