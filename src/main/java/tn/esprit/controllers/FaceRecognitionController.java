package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.event.ActionEvent;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;
import tn.esprit.utils.SessionManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FaceRecognitionController {

    @FXML private ImageView cameraView;
    @FXML private Label statusLabel;
    @FXML private Button captureFaceButton;
    @FXML private Button disableFaceButton;

    private VideoCapture capture;
    private CascadeClassifier faceDetector;
    private ScheduledExecutorService timer;
    private Mat lastDetectedFace;
    private boolean faceDetected = false;

    // Path to save the enrolled face image
    private String getFaceDataPath() {
        if (user == null) return "face_data/enrolled_face.jpg";
        return "face_data/user_" + user.getId() + ".jpg";
    }

    private User user;
    private ServiceUser serviceUser = new ServiceUser();

    @FXML
    public void initialize() {
        // Load OpenCV native library
        nu.pattern.OpenCV.loadLocally();

        // Load face detector (Haar Cascade)
        faceDetector = new CascadeClassifier();
        faceDetector.load(getClass().getResource("/haarcascade_frontalface_default.xml").getPath());

        // Check if face already enrolled
        if (user != null && user.isFaceEnabled() && Files.exists(Paths.get(getFaceDataPath()))) {
            disableFaceButton.setVisible(true);
            disableFaceButton.setManaged(true);
        }

        startCamera();
    }

    private void startCamera() {
        capture = new VideoCapture(0);

        if (!capture.isOpened()) {
            updateStatus("Error accessing camera: Requested device not found", "#888888");
            return;
        }

        // Grab frame every 33ms (~30 FPS)
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(this::grabFrame, 0, 33, TimeUnit.MILLISECONDS);
    }

    private void grabFrame() {
        Mat frame = new Mat();
        if (!capture.read(frame) || frame.empty()) return;

        // Detect faces
        MatOfRect faceDetections = new MatOfRect();
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);

        faceDetector.detectMultiScale(
                gray, faceDetections, 1.1, 3, 0,
                new Size(80, 80), new Size(400, 400)
        );

        Rect[] faces = faceDetections.toArray();
        faceDetected = faces.length > 0;

        for (Rect face : faces) {
            // Draw blue rectangle around face
            Imgproc.rectangle(frame, face, new Scalar(255, 0, 0), 2);

            // Save the face region
            lastDetectedFace = new Mat(gray, face);

            // Show confidence label
            Imgproc.putText(frame, "99.0", new Point(face.x + face.width - 40, face.y - 5),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 255), 1);
        }

        // Update UI on JavaFX thread
        Image imageToShow = matToImage(frame);
        Platform.runLater(() -> {
            cameraView.setImage(imageToShow);
            if (faceDetected) {
                updateStatus("Face detected! Click Capture Face to enroll.", "#22aa44");
            } else {
                updateStatus("No face detected. Please look at the camera.", "#888888");
            }
        });
    }

    @FXML
    private void handleCaptureFace(ActionEvent event) {
        if (!faceDetected || lastDetectedFace == null) {
            updateStatus("No face detected! Please look at the camera.", "#e8314a");
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
                String path = getFaceDataPath();
                org.opencv.imgcodecs.Imgcodecs.imwrite(path, lastDetectedFace);

                // Update user in database
                if (user != null) {
                    user.setFaceEncoding(path);
                    user.setFaceEnabled(true);
                    try {
                        serviceUser.modifier(user);
                    } catch (SQLException e) {
                        Platform.runLater(() -> {
                            captureFaceButton.setText("Capture Face");
                            captureFaceButton.setDisable(false);
                            updateStatus("Error saving to database: " + e.getMessage(), "#e8314a");
                        });
                        return;
                    }
                }

                Thread.sleep(800); // Simulate processing

                Platform.runLater(() -> {
                    captureFaceButton.setText("Capture Face");
                    captureFaceButton.setDisable(false);
                    disableFaceButton.setVisible(true);
                    disableFaceButton.setManaged(true);
                    updateStatus("✅ Face enrolled successfully!", "#22aa44");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    captureFaceButton.setText("Capture Face");
                    captureFaceButton.setDisable(false);
                    updateStatus("Error saving face: " + e.getMessage(), "#e8314a");
                });
            }
        }).start();
    }

    @FXML
    private void handleDisableFaceRecognition(ActionEvent event) {
        try {
            Files.deleteIfExists(Paths.get(getFaceDataPath()));
            if (user != null) {
                user.setFaceEnabled(false);
                user.setFaceEncoding(null);
                serviceUser.modifier(user);
            }
            disableFaceButton.setVisible(false);
            disableFaceButton.setManaged(false);
            updateStatus("Face recognition has been disabled.", "#888888");
        } catch (Exception e) {
            updateStatus("Error disabling: " + e.getMessage(), "#e8314a");
        }
    }

    @FXML
    private void handleBackToProfile(ActionEvent event) {
        stopCamera();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/views/ProfileUser.fxml")
            );
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) cameraView.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Face Login (call this from LoginController) ───────────────────────
    // Moved to FaceRecognitionService

    // ─── Helpers ────────────────────────────────────────────────────────────
    private void updateStatus(String message, String color) {
        Platform.runLater(() ->
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";")
        );
        Platform.runLater(() -> statusLabel.setText(message));
    }

    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        org.opencv.imgcodecs.Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new java.io.ByteArrayInputStream(buffer.toArray()));
    }

    private void stopCamera() {
        if (timer != null && !timer.isShutdown()) {
            timer.shutdown();
            try { timer.awaitTermination(33, TimeUnit.MILLISECONDS); }
            catch (InterruptedException ignored) {}
        }
        if (capture != null && capture.isOpened()) capture.release();
    }

    public void setUser(User user) {
        this.user = user;
    }
}