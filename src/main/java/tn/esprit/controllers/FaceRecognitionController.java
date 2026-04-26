package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;

public class FaceRecognitionController {

    @FXML private ImageView cameraView;
    @FXML private Label statusLabel;
    @FXML private Button captureFaceButton;
    @FXML private Button disableFaceButton;

    @FXML
    public void initialize() {
        startCamera();
    }

    private void startCamera() {
        // Start webcam feed and display in cameraView
        // Use OpenCV or JavaCV to capture frames
        // Example with OpenCV:
        // VideoCapture capture = new VideoCapture(0);
        // if (!capture.isOpened()) {
        //     statusLabel.setText("Error accessing camera: Requested device not found");
        //     statusLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
        //     return;
        // }
        // Run frame capture loop in background thread
        // Detect face -> show green status + blue rectangle overlay
        // statusLabel.setText("Face detected! Click Capture Face to enroll.");
        // statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 13px;");
    }

    @FXML
    private void handleCaptureFace(ActionEvent event) {
        // Show processing state
        captureFaceButton.setText("Processing...");
        captureFaceButton.setDisable(true);

        // Capture current frame, encode face, save to DB
        // On success:
        // Platform.runLater(() -> {
        //     captureFaceButton.setText("Capture Face");
        //     captureFaceButton.setDisable(false);
        //     disableFaceButton.setVisible(true);
        //     disableFaceButton.setManaged(true);
        //     statusLabel.setText("Face enrolled successfully!");
        //     statusLabel.setStyle("-fx-text-fill: green;");
        // });
    }

    @FXML
    private void handleDisableFaceRecognition(ActionEvent event) {
        // Remove face data from DB
        // Hide disable button, reset status
        disableFaceButton.setVisible(false);
        disableFaceButton.setManaged(false);
        statusLabel.setText("Face recognition has been disabled.");
        statusLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
    }

    @FXML
    private void handleBackToProfile(ActionEvent event) {
        // Navigate back to profile view
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/views/ProfileUser.fxml"));
        // Parent root = loader.load();
        // Stage stage = (Stage) cameraView.getScene().getWindow();
        // stage.setScene(new Scene(root));
    }
}