package gui;

import gui.view.ScreenManager;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;

public class Driver extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(getClass().getResourceAsStream("/fonts/GeistMono-Regular.ttf"), 14);
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/crisms_db?" +
                "user=root&password=p@ssword"); ;
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
