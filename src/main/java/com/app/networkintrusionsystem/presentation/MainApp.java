package com.app.networkintrusionsystem.presentation;

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
            Parent root = FXMLLoader.load(getClass().getResource("/com/app/networkintrusionsystem/fxmlFiles/LoginPage.fxml"));
            Scene scene = new Scene(root,1000,500);
            scene.getStylesheets().add(getClass().getResource("/com/app/networkintrusionsystem/style/LoginStyle.css").toExternalForm());

            //Set up stage
            stage.setTitle("Network Intrusion Detection System");
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