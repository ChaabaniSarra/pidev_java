package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Affiche les produits par défaut au démarrage
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
    public void showUsers() {
        loadPage("/users.fxml");
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
