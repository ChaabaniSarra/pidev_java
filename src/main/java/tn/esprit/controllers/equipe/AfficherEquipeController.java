package tn.esprit.controllers.equipe;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.esprit.entities.Equipe;
import tn.esprit.services.ServiceEquipe;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AfficherEquipeController implements Initializable {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Label totalTeamsLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortCombo;

    private final ServiceEquipe serviceEquipe = new ServiceEquipe();
    private final List<Equipe> allEquipes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSearchAndSort();
        loadEquipes();
    }

    private void setupSearchAndSort() {
        sortCombo.getItems().addAll("Nom A-Z", "Nom Z-A", "Max members croissant", "Max members decroissant");
        sortCombo.getSelectionModel().select("Nom A-Z");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderCards());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> renderCards());
    }

    private void loadEquipes() {
        try {
            List<Equipe> equipes = serviceEquipe.getAll();
            allEquipes.clear();
            allEquipes.addAll(equipes);
            totalTeamsLabel.setText(String.valueOf(equipes.size()));
            renderCards();
            messageLabel.setText("");
        } catch (SQLException e) {
            messageLabel.setText("Erreur chargement equipes : " + e.getMessage());
        }
    }

    private void renderCards() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);

        List<Equipe> filtered = new ArrayList<>();
        for (Equipe equipe : allEquipes) {
            if (keyword.isEmpty()
                    || safeLower(equipe.getNom()).contains(keyword)
                    || safeLower(equipe.getLogo()).contains(keyword)
                    || String.valueOf(equipe.getId()).contains(keyword)
                    || String.valueOf(equipe.getMaxMembers()).contains(keyword)) {
                filtered.add(equipe);
            }
        }

        filtered.sort(getSortComparator(sortCombo.getValue()));

        cardsContainer.getChildren().clear();
        int rank = 1;
        for (Equipe equipe : filtered) {
            cardsContainer.getChildren().add(buildCard(equipe, rank++));
        }

        if (filtered.isEmpty()) {
            messageLabel.setText("Aucune equipe trouvee.");
        } else {
            messageLabel.setText("");
        }
    }

    private Comparator<Equipe> getSortComparator(String sortValue) {
        if ("Nom Z-A".equals(sortValue)) {
            return Comparator.comparing((Equipe e) -> safeLower(e.getNom())).reversed();
        }
        if ("Max members croissant".equals(sortValue)) {
            return Comparator.comparingInt(Equipe::getMaxMembers);
        }
        if ("Max members decroissant".equals(sortValue)) {
            return Comparator.comparingInt(Equipe::getMaxMembers).reversed();
        }
        return Comparator.comparing(e -> safeLower(e.getNom()));
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private VBox buildCard(Equipe equipe, int rank) {
        VBox card = new VBox(8);
        card.setPrefWidth(270);
        card.setPrefHeight(305);
        card.setStyle("-fx-padding: 12; -fx-background-color: #111b3e;"
            + "-fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 12;");

        Label rankLabel = new Label("#" + rank);
        rankLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: rgba(255,255,255,0.20);");

        StackPane avatarPane = createAvatar(equipe);

        Label nomLabel = new Label(equipe.getNom());
        nomLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #e5e7eb;");

        Label subtitle = new Label("ESPORT TEAM");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        VBox header = new VBox(rankLabel);
        header.setAlignment(Pos.TOP_LEFT);

        VBox body = new VBox(10, avatarPane, nomLabel, subtitle);
        body.setAlignment(Pos.CENTER);

        VBox actions = buildActions(equipe, card);
        card.getChildren().addAll(header, body, actions);
        return card;
    }

    private VBox buildActions(Equipe equipe, VBox card) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        if (SessionManager.getCurrentUser() == null || SessionManager.getCurrentUser().getId() <= 0) {
            Label guestLabel = new Label("Connectez-vous pour interagir");
            guestLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
            box.getChildren().add(guestLabel);
            return box;
        }

        try {
            boolean isOwner = serviceEquipe.isCurrentUserOwnerOfEquipe(equipe.getId());
            if (isOwner) {
                Label ownerLabel = new Label("OWNER - Cliquez la carte pour ouvrir le dashboard");
                ownerLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px; -fx-font-weight: 700;");
                box.getChildren().add(ownerLabel);

                card.setOnMouseClicked(evt -> openOwnerDashboard());
                card.setStyle(card.getStyle() + "-fx-cursor: hand;");
                return box;
            }

            if (serviceEquipe.isCurrentUserMemberOfEquipe(equipe.getId())) {
                Label memberLabel = new Label("Vous etes deja membre");
                memberLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 12px; -fx-font-weight: 700;");
                box.getChildren().add(memberLabel);
                return box;
            }

            boolean pending = serviceEquipe.hasPendingRequestForCurrentUser(equipe.getId());
            if (pending) {
                Button cancelBtn = new Button("Supprimer invite");
                cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: 700; -fx-pref-width: 180;");
                cancelBtn.setOnAction(evt -> {
                    evt.consume();
                    handleCancelInvite(equipe.getId());
                });
                box.getChildren().add(cancelBtn);
            } else {
                Button joinBtn = new Button("Demander a joindre");
                joinBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: 700; -fx-pref-width: 180;");
                joinBtn.setOnAction(evt -> {
                    evt.consume();
                    handleJoinRequest(equipe.getId());
                });
                box.getChildren().add(joinBtn);
            }
        } catch (SQLException e) {
            Label errLabel = new Label("Etat indisponible");
            errLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            box.getChildren().add(errLabel);
        }

        return box;
    }

    private void handleJoinRequest(int equipeId) {
        try {
            serviceEquipe.createJoinRequest(equipeId);
            messageLabel.setText("Demande envoyee en statut pending.");
            messageLabel.setStyle("-fx-text-fill: #22c55e;");
            renderCards();
        } catch (SQLException e) {
            messageLabel.setText("Demande impossible : " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #f87171;");
        }
    }

    private void handleCancelInvite(int equipeId) {
        try {
            serviceEquipe.cancelPendingJoinRequestForCurrentUser(equipeId);
            messageLabel.setText("Invite supprimee (pending annule).");
            messageLabel.setStyle("-fx-text-fill: #22c55e;");
            renderCards();
        } catch (SQLException e) {
            messageLabel.setText("Suppression invite impossible : " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #f87171;");
        }
    }

    private void openOwnerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/equipe/ownerDashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Owner Dashboard");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            messageLabel.setText("Ouverture Owner Dashboard impossible : " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: #f87171;");
        }
    }

    @FXML
    private void handleAddEquipe() {
        openAddEquipeForm();
    }

    private void openAddEquipeForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/equipe/ajouterEquipe.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une equipe");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            loadEquipes();
        } catch (Exception e) {
            messageLabel.setText("Erreur ouverture formulaire : " + e.getMessage());
        }
    }

    private StackPane createAvatar(Equipe equipe) {
        Circle circle = new Circle(36);
        circle.setFill(Color.web("#172554"));
        circle.setStroke(Color.web("#263564"));

        StackPane avatar = new StackPane(circle);
        avatar.setAlignment(Pos.CENTER);

        String logo = equipe.getLogo();
        if (logo != null && !logo.isBlank()) {
            File file = new File(logo);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString(), 72, 72, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(72);
                imageView.setFitHeight(72);
                imageView.setClip(new Circle(36, 36, 36));
                avatar.getChildren().add(imageView);
                return avatar;
            }
        }

        Label initials = new Label(getInitials(equipe.getNom()));
        initials.setStyle("-fx-font-size: 22px; -fx-text-fill: #e5e7eb; -fx-font-weight: 700;");
        avatar.getChildren().add(initials);
        return avatar;
    }

    private String getInitials(String nom) {
        if (nom == null || nom.isBlank()) {
            return "?";
        }
        String trimmed = nom.trim();
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase();
        }
        return trimmed.substring(0, 2).toLowerCase();
    }

}
