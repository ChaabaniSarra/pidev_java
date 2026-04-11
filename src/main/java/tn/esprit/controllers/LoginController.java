package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {}

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please fill in all fields.", true);
            return;
        }

        // TODO: replace with real UserService lookup
        if (username.equals("admin") && password.equals("1234")) {
            showMessage("Welcome, " + username + "!", false);
            // TODO: load main scene
        } else {
            showMessage("Invalid credentials.", true);
        }
    }

    @FXML
    private void goToRegister() {
        // TODO: load Register.fxml
        System.out.println("Navigate to Register");
    }

    private void showMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        messageLabel.setTextFill(isError ? Color.RED : Color.web("#5b4cdf"));
    }
}