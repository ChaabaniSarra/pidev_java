package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadPage("/produits.fxml");
    }

    @FXML
    public void showUsers() {
        loadPage("/users.fxml");
    }

    @FXML
    public void showProducts() {
        loadPage("/produits.fxml");
    }

    @FXML
    public void showCategories() {
        loadPage("/categories.fxml");
    }

    @FXML
    public void showJeux() {
        loadPage("/jeuxDashboard.fxml");
    }

    @FXML
    public void showTournois() {
        loadPage("/tournoiDashboard.fxml");
    }

    @FXML
    public void showBlogs() {
        loadPage("/Blogs.fxml");
    }

    @FXML
    public void showHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.setTitle("Esports Community");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}