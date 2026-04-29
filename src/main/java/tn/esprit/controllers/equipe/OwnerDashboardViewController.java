package tn.esprit.controllers.equipe;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.entities.EquipeMemberView;
import tn.esprit.entities.OwnerDashboardStats;
import tn.esprit.services.ServiceEquipe;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OwnerDashboardViewController implements Initializable {

    @FXML
    private Label ownedTeamsLabel;
    @FXML
    private Label joinedTeamsLabel;
    @FXML
    private Label pendingRequestsLabel;
    @FXML
    private Label finishedMatchesLabel;
    @FXML
    private Label fullTeamLabel;
    @FXML
    private Label resultsLabel;
    @FXML
    private Label winRateLabel;

    @FXML
    private TextField searchField;
    @FXML
    private FlowPane memberCardsContainer;

    @FXML
    private TextField motifField;
    @FXML
    private Label messageLabel;

    private final ServiceEquipe serviceEquipe = new ServiceEquipe();
    private List<EquipeMemberView> allMembers = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSearch();
        loadStats();
        loadTeamMembers();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> renderCards());
    }

    @FXML
    private void handleRefresh() {
        loadStats();
        loadTeamMembers();
        messageLabel.setStyle("-fx-text-fill: #22c55e;");
        messageLabel.setText("Liste mise à jour.");
    }

    private void renderCards() {
        memberCardsContainer.getChildren().clear();
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        for (EquipeMemberView member : allMembers) {
            if (matchesQuery(member, query)) {
                memberCardsContainer.getChildren().add(createMemberCard(member));
            }
        }
    }

    private boolean matchesQuery(EquipeMemberView member, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return containsIgnoreCase(member.getJoueurNom(), query)
                || containsIgnoreCase(member.getJoueurEmail(), query)
                || containsIgnoreCase(member.getEquipeNom(), query)
                || containsIgnoreCase(member.getStatus(), query);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private VBox createMemberCard(EquipeMemberView member) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95); -fx-background-radius: 18; -fx-padding: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 16, 0.0, 0.0, 8.0);");
        card.setPrefWidth(240);

        Label teamLabel = new Label(member.getEquipeNom() == null || member.getEquipeNom().isEmpty() ? "Sans équipe" : member.getEquipeNom());
        teamLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-font-weight: 700;");

        Label playerLabel = new Label(member.getJoueurNom());
        playerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 800;");

        Label emailLabel = new Label(member.getJoueurEmail());
        emailLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Label statusLabel = new Label(member.getStatus());
        statusLabel.setStyle("-fx-background-color: rgba(59, 130, 246, 0.18); -fx-text-fill: #bfdbfe; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: 700;");

        String joinedText = member.getJoinedAt() == null ? "" : "Rejoint le " + member.getJoinedAt().toString();
        Label joinedLabel = new Label(joinedText);
        joinedLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");

        HBox titleRow = new HBox(10, teamLabel, new Region());
        HBox.setHgrow(titleRow.getChildren().get(1), Priority.ALWAYS);

        HBox actionBox = new HBox(10);
        actionBox.setStyle("-fx-alignment: center-right;");

        Button primaryButton = new Button();
        primaryButton.setStyle("-fx-background-radius: 12; -fx-text-fill: white; -fx-padding: 8 14;");
        Button secondaryButton = new Button("Supprimer");
        secondaryButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 14;");

        if ("Pending".equalsIgnoreCase(member.getStatus())) {
            primaryButton.setText("Accepter");
            primaryButton.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 14;");
            primaryButton.setOnAction(e -> handleAcceptRequest(member));
            secondaryButton.setOnAction(e -> handleRejectRequest(member));
            actionBox.getChildren().addAll(primaryButton, secondaryButton);
        } else if ("Member".equalsIgnoreCase(member.getStatus())) {
            primaryButton.setText("Supprimer");
            primaryButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 14;");
            primaryButton.setOnAction(e -> handleRemoveMember(member));
            actionBox.getChildren().add(primaryButton);
        } else {
            primaryButton.setText("Ajouter");
            primaryButton.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 14;");
            primaryButton.setOnAction(e -> handleAddMember(member));
            actionBox.getChildren().add(primaryButton);
        }

        card.getChildren().addAll(titleRow, playerLabel, emailLabel, statusLabel, joinedLabel, actionBox);
        return card;
    }

    private void handleAcceptRequest(EquipeMemberView member) {
        if (member.getRequestId() == null) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Aucune demande à accepter.");
            return;
        }
        try {
            serviceEquipe.processJoinRequest(member.getRequestId(), true, motifField.getText());
            loadStats();
            loadTeamMembers();
            motifField.clear();
            messageLabel.setStyle("-fx-text-fill: #22c55e;");
            messageLabel.setText("Demande acceptée.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Acceptation impossible : " + e.getMessage());
        }
    }

    private void handleRejectRequest(EquipeMemberView member) {
        if (member.getRequestId() == null) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Aucune demande à rejeter.");
            return;
        }
        try {
            serviceEquipe.processJoinRequest(member.getRequestId(), false, motifField.getText());
            loadStats();
            loadTeamMembers();
            motifField.clear();
            messageLabel.setStyle("-fx-text-fill: #22c55e;");
            messageLabel.setText("Demande rejetée.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Rejet impossible : " + e.getMessage());
        }
    }

    private void handleAddMember(EquipeMemberView member) {
        try {
            serviceEquipe.addMemberToFirstOwnedEquipe(member.getJoueurId());
            loadStats();
            loadTeamMembers();
            messageLabel.setStyle("-fx-text-fill: #22c55e;");
            messageLabel.setText("Joueur ajouté à l'équipe.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Ajout impossible : " + e.getMessage());
        }
    }

    private void handleRemoveMember(EquipeMemberView member) {
        try {
            serviceEquipe.removeMemberFromEquipe(member.getEquipeId(), member.getJoueurId());
            loadStats();
            loadTeamMembers();
            messageLabel.setStyle("-fx-text-fill: #22c55e;");
            messageLabel.setText("Membre supprimé.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Suppression impossible : " + e.getMessage());
        }
    }

    private void loadTeamMembers() {
        try {
            allMembers = serviceEquipe.getOwnerTeamPlayersAndRequests();
            renderCards();
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Impossible de charger la liste : " + e.getMessage());
        }
    }

    private void loadStats() {
        try {
            OwnerDashboardStats stats = serviceEquipe.getOwnerDashboardStatsForCurrentOwner();
            ownedTeamsLabel.setText(String.valueOf(stats.getOwnedTeamsCount()));
            joinedTeamsLabel.setText(String.valueOf(stats.getJoinedTeamsCount()));
            pendingRequestsLabel.setText(String.valueOf(stats.getPendingRequestsCount()));
            finishedMatchesLabel.setText(String.valueOf(stats.getTotalFinishedMatches()));
            fullTeamLabel.setText(stats.isHasFullTeam() ? "Oui" : "Non");
            resultsLabel.setText("V: " + stats.getWins() + "   N: " + stats.getDraws() + "   P: " + stats.getLosses());
            winRateLabel.setText(stats.getWinRate() + "%");

            messageLabel.setStyle("-fx-text-fill: #22c55e;");
            messageLabel.setText("Dashboard chargé.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Erreur dashboard owner: " + e.getMessage());
        }
    }
}
