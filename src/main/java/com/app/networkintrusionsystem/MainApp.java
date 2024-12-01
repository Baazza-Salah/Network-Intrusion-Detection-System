package com.app.networkintrusionsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // Load the LoginController
            Parent root = FXMLLoader.load(getClass().getResource("/com/app/networkintrusionsystem/LoginPage.fxml"));

            // Create scene
            Scene scene = new Scene(root, 1000, 500);

            // Load CSS
            scene.getStylesheets().add(getClass().getResource("/com/app/networkintrusionsystem/LoginStyle.css").toExternalForm());

            // Set up stage
            stage.setTitle("Login");
            stage.setScene(scene);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}