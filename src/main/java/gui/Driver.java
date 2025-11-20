package gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Driver extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ScreenManager.SINGLETON.start(primaryStage);
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
