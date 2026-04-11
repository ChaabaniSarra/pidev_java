package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjouterUserController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField rolesField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Label messageLabel;
    @FXML private Button submitBtn;

    private final ServiceUser serviceUser = new ServiceUser();
    private User userToEdit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        rolesField.setText("[\"ROLE_USER\"]");
        activeCheckBox.setSelected(true);
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;
        titleLabel.setText("Modifier Utilisateur");
        submitBtn.setText("Modifier");
        nomField.setText(user.getNom());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        rolesField.setText(user.getRoles());
        activeCheckBox.setSelected(user.isActive());
    }

    @FXML
    private void handleSubmit() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String roles = rolesField.getText().trim();

        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || roles.isEmpty()) {
            showMessage("Tous les champs sont obligatoires.", true);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showMessage("Email invalide.", true);
            return;
        }

        try {
            if (userToEdit == null && serviceUser.emailExists(email)) {
                showMessage("Cet email existe deja.", true);
                return;
            }

            if (userToEdit != null && serviceUser.emailExistsForOtherUser(email, userToEdit.getId())) {
                showMessage("Cet email est deja utilise par un autre utilisateur.", true);
                return;
            }

            if (userToEdit == null) {
                User user = new User(
                        email,
                        roles,
                        password,
                        nom,
                        activeCheckBox.isSelected(),
                        null,
                        false,
                        null,
                        null,
                        null,
                        false
                );
                serviceUser.ajouter(user);
                showMessage("Utilisateur ajoute avec succes.", false);
            } else {
                userToEdit.setNom(nom);
                userToEdit.setEmail(email);
                userToEdit.setPassword(password);
                userToEdit.setRoles(roles);
                userToEdit.setActive(activeCheckBox.isSelected());
                serviceUser.modifier(userToEdit);
                showMessage("Utilisateur modifie avec succes.", false);
            }

            closeAfterDelay();
        } catch (SQLException e) {
            showMessage("Erreur base de donnees : " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nomField.getScene().getWindow()).close();
    }

    private void showMessage(String message, boolean error) {
        messageLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        messageLabel.setText(message);
    }

    private void closeAfterDelay() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> ((Stage) nomField.getScene().getWindow()).close());
        });
        thread.setDaemon(true);
        thread.start();
    }
}
