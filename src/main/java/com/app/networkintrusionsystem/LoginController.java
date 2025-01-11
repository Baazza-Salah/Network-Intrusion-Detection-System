package com.app.networkintrusionsystem;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private DatabaseManager dbManager;

    public LoginController() {
        dbManager = new DatabaseManager();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (dbManager.checkUserCredentials(username, password)) {
            // Navigate to VisualizationController
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/app/networkintrusionsystem/VisualizationPage.fxml"));
                Scene scene = new Scene(root, 1000, 500);

                // Load CSS for the visualization page
                scene.getStylesheets().add(getClass().getResource("/com/app/networkintrusionsystem/VisualizationStyle.css").toExternalForm());

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            displayInvalidCredentialsMessage();
        }
    }

    private void displayInvalidCredentialsMessage() {
        Label invalidCredsLabel = new Label("Invalid credentials");
        invalidCredsLabel.setStyle("-fx-text-fill: red;");
        VBox parent = (VBox) (loginButton != null ? loginButton.getParent() : null);
        if (parent != null) {
            parent.getChildren().removeIf(node -> node instanceof Label && ((Label) node).getText().equals("Invalid credentials"));
            parent.getChildren().add(invalidCredsLabel);
        }
    }

}