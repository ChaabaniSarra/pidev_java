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
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    private final ServiceUser serviceUser = new ServiceUser();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        usernameField.textProperty().addListener((obs, old, val) -> validateEmail());
        passwordField.textProperty().addListener((obs, old, val) -> validatePassword());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        boolean emailOk = validateEmail();
        boolean passwordOk = validatePassword();

        if (!emailOk || !passwordOk) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Corrigez les erreurs avant de continuer.");
            return;
        }

        try {
            User user = serviceUser.authenticate(username, password);
            if (user == null) {
                messageLabel.setTextFill(Color.RED);
                messageLabel.setText(" Identifiants invalides.");
                return;
            }

            SessionManager.setCurrentUser(user);
            messageLabel.setTextFill(Color.web("#5b4cdf"));
            messageLabel.setText(" Bienvenue, " + user.getNom() + "!");
            String fxml = "/home.fxml";
            if ("admin@gmail.com".equalsIgnoreCase(user.getEmail())) {
                fxml = "/main.fxml";
            }
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 980, 720));
            stage.setTitle("Esports Community");
            stage.show();

        } catch (SQLException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Erreur base de données : " + e.getMessage());
        } catch (IOException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Impossible d'ouvrir la page d'accueil.");
        }
    }

    private boolean validateEmail() {
        String email = usernameField.getText().trim();
        
        if (email.isEmpty()) {
            setError(usernameField, emailError, "L'email est obligatoire.");
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            setError(usernameField, emailError, "Veuillez entrer un email valide.");
            return false;
        }
        
        clearError(usernameField, emailError);
        return true;
    }

    private boolean validatePassword() {
        String password = passwordField.getText();
        
        if (password.isEmpty()) {
            setError(passwordField, passwordError, "Le mot de passe est obligatoire.");
            return false;
        }
        
        if (password.length() < 6) {
            setError(passwordField, passwordError, "Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }
        
        clearError(passwordField, passwordError);
        return true;
    }

    private void setError(Control field, Label errorLabel, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-radius: 5;");
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError(Control field, Label errorLabel) {
        field.setStyle("-fx-border-color: transparent transparent #27ae60 transparent; -fx-border-width: 0 0 1.5 0;");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            loadScene(event, "/Register.fxml", "Register");
        } catch (IOException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Impossible d'ouvrir le formulaire d'inscription.");
        }
    }

    private void loadScene(ActionEvent event, String resourcePath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
        Stage stage = (Stage) ((Control) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }
}
