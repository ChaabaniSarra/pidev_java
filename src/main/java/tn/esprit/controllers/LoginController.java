package tn.esprit.controllers;

import javafx.application.Platform;
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
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import tn.esprit.entities.User;
import tn.esprit.services.FaceRecognitionService;
import tn.esprit.services.GoogleOAuthService;
import tn.esprit.services.ServiceUser;
import tn.esprit.services.TwoFactorService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class LoginController {

    private final ServiceUser serviceUser = new ServiceUser();
    private final TwoFactorService twoFactorService = new TwoFactorService();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Button loginButton;

    // ── Init ─────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        usernameField.textProperty().addListener((obs, old, val) -> validateEmail());
        passwordField.textProperty().addListener((obs, old, val) -> validatePassword());
    }

    // ── Email / Password Login ────────────────────────────────────────────────
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
                messageLabel.setText("Identifiants invalides.");
                return;
            }

            SessionManager.setCurrentUser(user);
            messageLabel.setTextFill(Color.web("#5b4cdf"));
            messageLabel.setText("Bienvenue, " + user.getNom() + "!");

            // ── Check if 2FA is required ──────────────────────────────────
            if (twoFactorService.requires2FA(user)) {
                Parent root = FXMLLoader.load(
                        getClass().getResource("/TwoFactorVerify.fxml")
                );
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Two-Factor Authentication");
                stage.show();
                return;
            }

            // No 2FA → go straight home
            loadHomePage();

        } catch (SQLException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Erreur base de données : " + e.getMessage());
        } catch (IOException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Impossible d'ouvrir la page d'accueil.");
        }
    }

    // ── Face Recognition Login ────────────────────────────────────────────────
    @FXML
    private void handleFaceRecognition(ActionEvent event) {
        messageLabel.setTextFill(Color.web("#5b4cdf"));
        messageLabel.setText("📷 Opening camera for face login...");

        new Thread(() -> {
            try {
                nu.pattern.OpenCV.loadLocally();

                VideoCapture capture = new VideoCapture(0, org.opencv.videoio.Videoio.CAP_DSHOW);
                if (!capture.isOpened()) {
                    capture = new VideoCapture(1, org.opencv.videoio.Videoio.CAP_DSHOW);
                }

                if (!capture.isOpened()) {
                    Platform.runLater(() -> {
                        messageLabel.setTextFill(Color.RED);
                        messageLabel.setText("❌ Camera not found!");
                    });
                    return;
                }

                // Warm up camera
                Mat frame = new Mat();
                for (int i = 0; i < 10; i++) {
                    capture.read(frame);
                    Thread.sleep(50);
                }
                capture.read(frame);
                capture.release();

                if (frame.empty()) {
                    Platform.runLater(() -> {
                        messageLabel.setTextFill(Color.RED);
                        messageLabel.setText("❌ Could not read camera frame!");
                    });
                    return;
                }

                // Get all users with face enabled
                List<User> usersWithFace = serviceUser.getUsersWithFaceEnabled();

                if (usersWithFace.isEmpty()) {
                    Platform.runLater(() -> {
                        messageLabel.setTextFill(Color.RED);
                        messageLabel.setText("❌ No users with face recognition enrolled!");
                    });
                    return;
                }

                // Convert to grayscale for matching
                Mat capturedGray = new Mat();
                Imgproc.cvtColor(frame, capturedGray, Imgproc.COLOR_BGR2GRAY);

                User matchedUser = null;
                double bestMatch = 0.0;

                for (User user : usersWithFace) {
                    String facePath = user.getFaceEncoding();
                    if (facePath == null || facePath.isEmpty()) continue;

                    Mat enrolledMat = org.opencv.imgcodecs.Imgcodecs.imread(facePath, 0);
                    if (enrolledMat.empty()) continue;

                    Mat resized = new Mat();
                    Imgproc.resize(capturedGray, resized, enrolledMat.size());

                    Mat result = new Mat();
                    Imgproc.matchTemplate(resized, enrolledMat, result, Imgproc.TM_CCOEFF_NORMED);

                    double similarity = Core.minMaxLoc(result).maxVal;
                    System.out.println("User: " + user.getNom() + " similarity: " + similarity);

                    if (similarity > bestMatch) {
                        bestMatch = similarity;
                        matchedUser = user;
                    }
                }

                final User finalMatchedUser = matchedUser;
                final double finalBestMatch = bestMatch;

                Platform.runLater(() -> {
                    if (finalMatchedUser != null && finalBestMatch >= 0.4) {
                        SessionManager.setCurrentUser(finalMatchedUser);
                        messageLabel.setTextFill(Color.GREEN);
                        messageLabel.setText("✅ Face recognized! Logging in as "
                                + finalMatchedUser.getNom() + "...");

                        // ── Check 2FA after face login too ────────────────
                        if (twoFactorService.requires2FA(finalMatchedUser)) {
                            try {
                                Parent root = FXMLLoader.load(
                                        getClass().getResource("/tn/esprit/views/TwoFactorVerify.fxml")
                                );
                                Stage stage = (Stage) loginButton.getScene().getWindow();
                                stage.setScene(new Scene(root));
                                stage.setTitle("Two-Factor Authentication");
                                stage.show();
                            } catch (IOException e) {
                                messageLabel.setTextFill(Color.RED);
                                messageLabel.setText("❌ Error opening 2FA screen.");
                            }
                        } else {
                            loadHomePage();
                        }

                    } else {
                        messageLabel.setTextFill(Color.RED);
                        messageLabel.setText(
                                "❌ Face not recognized! Please enroll first or use email login."
                        );
                    }
                });

            } catch (Exception e) {
                System.out.println("Face recognition error: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    messageLabel.setTextFill(Color.RED);
                    messageLabel.setText("❌ Error: " + e.getMessage());
                });
            }
        }).start();
    }

    // ── Forgot Password ───────────────────────────────────────────────────────
    @FXML
    public void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ForgotPassword.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Forgot Password");
            stage.show();
        } catch (IOException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Impossible d'ouvrir la page.");
        }
    }

    // ── Google OAuth ──────────────────────────────────────────────────────────
    @FXML
    private void handleGoogleSignUp() {
        try {
            String clientId = "460245401741-m94mns2ae0675cvo0fsiiahrr9qqbagu.apps.googleusercontent.com";
            String clientSecret = "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";

            GoogleOAuthService oauthService = new GoogleOAuthService(clientId, clientSecret);
            User googleUser = oauthService.authenticateAndGetUser();

            User existingUser = serviceUser.findByGoogleOauthId(googleUser.getGoogleOauthId());
            if (existingUser == null) {
                User existingByEmail = serviceUser.findByEmail(googleUser.getEmail());
                if (existingByEmail != null) {
                    existingUser = serviceUser.linkGoogleAccount(existingByEmail, googleUser);
                }
            }
            if (existingUser != null) {
                SessionManager.setCurrentUser(existingUser);
                messageLabel.setTextFill(Color.web("#5b4cdf"));
                messageLabel.setText("Bienvenue, " + existingUser.getNom() + "!");

                // Check 2FA for Google login too
                if (twoFactorService.requires2FA(existingUser)) {
                    Parent root = FXMLLoader.load(
                            getClass().getResource("/TwoFactorVerify.fxml")
                    );
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Two-Factor Authentication");
                    stage.show();
                } else {
                    loadHomePage();
                }

            } else {
                if (serviceUser.emailExists(googleUser.getEmail())) {
                    messageLabel.setTextFill(Color.RED);
                    messageLabel.setText("Un compte avec cet email existe déjà.");
                    return;
                }
                User newUser = serviceUser.createOAuthUser(googleUser);
                SessionManager.setCurrentUser(newUser);
                messageLabel.setTextFill(Color.web("#27ae60"));
                messageLabel.setText("Compte créé ! Bienvenue, " + newUser.getNom() + "!");
                loadHomePage();
            }

        } catch (IOException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Erreur Google Auth : " + e.getMessage());
        } catch (SQLException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Erreur base de données : " + e.getMessage());
        } catch (Exception e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Erreur : " + e.getMessage());
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────
    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            loadScene(event, "/Register.fxml", "Register");
        } catch (IOException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Impossible d'ouvrir le formulaire d'inscription.");
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────
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
            setError(passwordField, passwordError, "Au moins 6 caractères.");
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
        field.setStyle("-fx-border-color: transparent transparent #27ae60 transparent;"
                + " -fx-border-width: 0 0 1.5 0;");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    private void loadScene(ActionEvent event, String resourcePath, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
        Stage stage = (Stage) ((Control) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    private void loadHomePage() {
        try {
            User user = SessionManager.getCurrentUser();
            String fxml = "admin@gmail.com".equalsIgnoreCase(user.getEmail())
                    ? "/main.fxml" : "/home.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 980, 720));
            stage.setTitle("Esports Community");
            stage.show();
        } catch (IOException e) {
            messageLabel.setTextFill(Color.RED);
            messageLabel.setText("Impossible d'ouvrir la page d'accueil.");
        }
    }
}
