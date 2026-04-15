package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import tn.esprit.entities.Stream;
import tn.esprit.services.ServiceStream;

public class StreamController {

    @FXML
    private Pane videoPane;

    private final ServiceStream serviceStream = new ServiceStream();

    @FXML
    public void initialize() {

        System.out.println("StreamController loaded OK");

        WebView webView = new WebView();
        webView.setPrefSize(900, 500);

        Stream active = serviceStream.getActiveStream();

        if (active != null) {

            String url = active.getUrl();
            System.out.println("▶ Stream URL: " + url);

            webView.getEngine().load(url);

            videoPane.getChildren().add(webView);

        } else {
            System.out.println("❌ Aucun stream actif");
        }
    }
}