package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all fields.", true);
            return;
        }

        try {
            User user = serviceUser.authenticate(username, password);
            if (user == null) {
                showMessage("Invalid credentials.", true);
                return;
            }

            showMessage("Welcome, " + user.getNom() + "!", false);
        } catch (SQLException e) {
            showMessage("Database error: " + e.getMessage(), true);
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            loadScene(event, "/Register.fxml", "Register");
        } catch (IOException e) {
            showMessage("Unable to open register form.", true);
        }
    }
    @FXML
    private void goToHome(ActionEvent event) {
        try {
            loadScene(event, "/home.fxml", "Home");
        } catch (IOException e) {
            showMessage("Unable to open Home page.", true);
        }
    }

    private void loadScene(ActionEvent event, String resourcePath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
        Stage stage = (Stage) ((Control) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    private void showMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        messageLabel.setTextFill(isError ? Color.RED : Color.web("#5b4cdf"));
    }
}
