package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class ProfileUserController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleBadgeLabel;
    @FXML private Label activeStatusLabel;
    @FXML private Label userIdLabel;
    @FXML private Label securityLabel;
    @FXML private Label avatarLabel;
    @FXML private Button editProfileButton;

    @FXML
    public void initialize() {
        try {
            refreshProfile();
        } catch (Exception e) {
            System.out.println("Error initializing profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditProfile() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterUser.fxml"));
            Parent root = loader.load();
            AjouterUserController controller = loader.getController();
            controller.setUserToEdit(currentUser);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Profil");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshProfile();
        } catch (IOException e) {
            nameLabel.setText("Unable to open edit form");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) editProfileButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            nameLabel.setText("Unable to open login page");
        }
    }

    private void refreshProfile() {
        try {
            User user = SessionManager.getCurrentUser();

            if (user == null) {
                nameLabel.setText("Guest User");
                emailLabel.setText("No authenticated user");
                roleBadgeLabel.setText("VISITOR");
                activeStatusLabel.setText("Offline");
                userIdLabel.setText("--");
                securityLabel.setText("2FA not configured");
                avatarLabel.setText("GU");
                return;
            }

            nameLabel.setText(safeValue(user.getNom(), "Unknown User"));
            emailLabel.setText(safeValue(user.getEmail(), "No email"));
            roleBadgeLabel.setText(formatRole(user.getRoles()));
            activeStatusLabel.setText(user.isActive() ? "Active account" : "Inactive account");
            userIdLabel.setText(String.valueOf(user.getId()));
            securityLabel.setText(user.isIs2faEnabled() ? "2FA enabled" : "2FA disabled");
            avatarLabel.setText(buildInitials(user.getNom(), user.getEmail()));
        } catch (NullPointerException e) {
            System.out.println("NullPointerException in refreshProfile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatRole(String roles) {
        if (roles == null || roles.isBlank()) {
            return "USER";
        }

        return roles
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace("ROLE_", "")
                .replace("_", " ")
                .trim()
                .toUpperCase();
    }

    private String buildInitials(String name, String email) {
        String source = (name != null && !name.isBlank()) ? name.trim() : email;
        if (source == null || source.isBlank()) {
            return "UP";
        }

        String[] parts = source.split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }

        String compact = source.replaceAll("[^A-Za-z0-9]", "");
        if (compact.length() >= 2) {
            return compact.substring(0, 2).toUpperCase();
        }

        return compact.isEmpty() ? "UP" : compact.toUpperCase();
    }
}
