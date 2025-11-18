package gui;

import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;

public class Driver extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/crisms_db?" +
                "user=root&password=p@ssword");
        // TODO migrate from using one connection to making a connection for every query
        // use the DBInteractor guel made
        ScreenManager.SINGLETON.start(primaryStage, conn);
        ScreenManager.SINGLETON.displayScreen("/gui/view/main-menu.fxml");
        primaryStage.setTitle("Restaurant Inventory Stock Management System");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
