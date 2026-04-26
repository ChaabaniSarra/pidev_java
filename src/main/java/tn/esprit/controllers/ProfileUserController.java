package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import tn.esprit.entities.Product;
import tn.esprit.entities.User;
import tn.esprit.utils.SessionManager;
import tn.esprit.services.ServiceUser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProfileUserController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleBadgeLabel;
    @FXML private Label activeStatusLabel;
    @FXML private Label userIdLabel;
    @FXML private Label securityLabel;
    @FXML private Label avatarLabel;
    @FXML private Button editProfileButton;
    @FXML private Label faceRecognitionStatusLabel;
    @FXML private Button enableFaceRecognitionButton;
    @FXML private javafx.scene.layout.VBox cameraSection;
    @FXML private ImageView cameraView;
    @FXML private Label cameraStatusLabel;
    @FXML private Button captureFaceButton;
    @FXML private Button disableFaceButton;

    private VideoCapture capture;
    private CascadeClassifier faceDetector;
    private ScheduledExecutorService timer;
    private Mat lastDetectedFace;
    private boolean faceDetected = false;
    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        try {
            // Load OpenCV
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV loaded successfully");

            // Skip cascade loading for now - causing issues
            System.out.println("Face detector initialization skipped (cascade issues)");

            refreshProfile();
        } catch (Exception e) {
            System.out.println("Error initializing profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String loadCascadePath() {
        try {
            // Try to get the cascade file from resources
            URL resource = getClass().getResource("/haarcascade_frontalface_default.xml");
            if (resource == null) {
                System.out.println("Cascade file not found in resources!");
                return null;
            }

            // For JAR files, extract to temp location
            if (resource.getProtocol().equals("jar")) {
                try (java.io.InputStream is = resource.openStream()) {
                    File temp = File.createTempFile("cascade", ".xml");
                    java.nio.file.Files.copy(is, temp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    temp.deleteOnExit();
                    System.out.println("Cascade extracted to temp: " + temp.getAbsolutePath());
                    return temp.getAbsolutePath();
                }
            } else {
                // For regular file system
                String path = resource.getPath();
                path = java.net.URLDecoder.decode(path, "UTF-8");
                // Remove leading slash on Windows if present
                if (path.startsWith("/") && path.charAt(2) == ':') {
                    path = path.substring(1);
                }
                System.out.println("Cascade path: " + path);
                if (!new File(path).exists()) {
                    System.out.println("Cascade file does not exist at: " + path);
                    return null;
                }
                return path;
            }
        } catch (Exception e) {
            System.out.println("Error loading cascade: " + e.getMessage());
            e.printStackTrace();
            return null;
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
            avatarLabel.setText(buildInitials(user.getNom(), user.getEmail()));

            // Update face recognition status
            if (user.isFaceEnabled()) {
                faceRecognitionStatusLabel.setText("✅ Face recognition is enabled");
                faceRecognitionStatusLabel.setStyle("-fx-text-fill: #22aa44;");
                enableFaceRecognitionButton.setText("MANAGE FACE RECOGNITION");
                // disableFaceButton.setVisible(true);
                // disableFaceButton.setManaged(true);
            } else {
                faceRecognitionStatusLabel.setText("⚠ Face recognition is not enabled");
                faceRecognitionStatusLabel.setStyle("-fx-text-fill: #f5a623;");
                enableFaceRecognitionButton.setText("ENABLE FACE RECOGNITION");
                // disableFaceButton.setVisible(false);
                // disableFaceButton.setManaged(false);
            }
            // Hide camera section
            // cameraSection.setVisible(false);
            // cameraSection.setManaged(false);
            enableFaceRecognitionButton.setVisible(true);
            enableFaceRecognitionButton.setManaged(true);

            // Load and display user's face image
            if (user.isFaceEnabled() && user.getFaceEncoding() != null) {
                // Face is enabled, no need to display it in profile
                System.out.println("User has face recognition enabled");
            }
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

    public void handleEnableFaceRecognition(ActionEvent actionEvent) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("No user logged in");
            return;
        }

        System.out.println("Enabling face recognition for user: " + currentUser.getNom());

        // Show camera section
        cameraSection.setVisible(true);
        cameraSection.setManaged(true);
        enableFaceRecognitionButton.setVisible(false);
        enableFaceRecognitionButton.setManaged(false);

        startCamera();
    }

    private void startCamera() {
        System.out.println("Starting camera...");
        
        if (faceDetector == null || faceDetector.empty()) {
            System.out.println("Warning: Face detector not loaded, but continuing with camera...");
        }

        capture = new VideoCapture(0, org.opencv.videoio.Videoio.CAP_DSHOW);
        System.out.println("Camera isOpened: " + capture.isOpened());

        if (!capture.isOpened()) {
            updateCameraStatus("Error: Camera not found. Try camera index 1.", "#e8314a");
            System.out.println("Trying camera index 1...");
            capture = new VideoCapture(1, org.opencv.videoio.Videoio.CAP_DSHOW);
            if (!capture.isOpened()) {
                updateCameraStatus("Error: No camera device found!", "#e8314a");
                return;
            }
        }

        System.out.println("Camera opened successfully, starting frame capture...");
        updateCameraStatus("Camera initialized, waiting for face...", "#888888");
        
        // Grab frame every 33ms (~30 FPS)
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::grabFrame, 0, 33, TimeUnit.MILLISECONDS);
    }

    private void grabFrame() {
        try {
            Mat frame = new Mat();
            if (!capture.isOpened() || !capture.read(frame) || frame.empty()) {
                return;
            }

            // Display the frame first - NO face detection for now
            Image imageToShow = matToImage(frame);
            if (imageToShow != null) {
                Platform.runLater(() -> {
                    cameraView.setImage(imageToShow);
                });
            }

            // For now, simulate face detection - user can capture any frame
            // This is a workaround while we fix the cascade issue
            faceDetected = true;
            lastDetectedFace = frame.clone();

            Platform.runLater(() -> {
                updateCameraStatus("✅ Ready to capture! Click Capture Face to save.", "#22aa44");
                captureFaceButton.setStyle("-fx-background-color: #22aa44; -fx-text-fill: white; -fx-padding: 10; -fx-font-weight: bold;");
            });

        } catch (Exception e) {
            System.out.println("Error in grabFrame: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCaptureFace(ActionEvent event) {
        User currentUser = SessionManager.getCurrentUser();
        if (lastDetectedFace == null || lastDetectedFace.empty() || currentUser == null) {
            updateCameraStatus("No valid face detected! Please look at the camera.", "#e8314a");
            return;
        }

        // Show processing state
        captureFaceButton.setText("⏳ Processing...");
        captureFaceButton.setDisable(true);

        new Thread(() -> {
            try {
                // Create directory if not exists
                Files.createDirectories(Paths.get("face_data"));

                // Save face image to disk
                String path = "face_data/user_" + currentUser.getId() + ".jpg";
                Imgcodecs.imwrite(path, lastDetectedFace);

                System.out.println("Face saved to: " + path);

                // Update user in database
                currentUser.setFaceEncoding(path);
                currentUser.setFaceEnabled(true);
                serviceUser.modifier(currentUser);

                javafx.application.Platform.runLater(() -> {
                    captureFaceButton.setText("Capture Face");
                    captureFaceButton.setDisable(false);
                    disableFaceButton.setVisible(true);
                    disableFaceButton.setManaged(true);
                    updateCameraStatus("✅ Face enrolled successfully!", "#22aa44");
                    stopCamera();
                    refreshProfile();
                });

            } catch (Exception e) {
                System.out.println("Error saving face: " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    captureFaceButton.setText("Capture Face");
                    captureFaceButton.setDisable(false);
                    updateCameraStatus("Error saving face: " + e.getMessage(), "#e8314a");
                });
            }
        }).start();
    }

    @FXML
    private void handleDisableFace(ActionEvent event) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        try {
            Files.deleteIfExists(Paths.get("face_data/user_" + currentUser.getId() + ".jpg"));
            currentUser.setFaceEnabled(false);
            currentUser.setFaceEncoding(null);
            serviceUser.modifier(currentUser);
            // disableFaceButton.setVisible(false);
            // disableFaceButton.setManaged(false);
            updateCameraStatus("Face recognition has been disabled.", "#888888");
            stopCamera();
            refreshProfile();
        } catch (Exception e) {
            updateCameraStatus("Error disabling: " + e.getMessage(), "#e8314a");
        }
    }

    private void updateCameraStatus(String message, String color) {
        try {
            Platform.runLater(() -> {
                if (cameraStatusLabel != null) {
                    cameraStatusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");
                    cameraStatusLabel.setText(message);
                }
            });
        } catch (Exception e) {
            System.out.println("Error updating status: " + e.getMessage());
        }
    }

    private Image matToImage(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            byte[] imageData = buffer.toArray();
            if (imageData != null && imageData.length > 0) {
                return new Image(new java.io.ByteArrayInputStream(imageData));
            }
        } catch (Exception e) {
            System.out.println("Error converting Mat to Image: " + e.getMessage());
        }
        return null;
    }

    private void stopCamera() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try { timer.awaitTermination(100, TimeUnit.MILLISECONDS); }
            catch (InterruptedException ignored) {}
        }
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        if (lastDetectedFace != null) {
            lastDetectedFace.release();
        }
    }

    private void loadUserFaceImage(int userId) {
        // This method is no longer used - face images are stored but not displayed in profile
        System.out.println("Face image stored for user: " + userId);
    }
}