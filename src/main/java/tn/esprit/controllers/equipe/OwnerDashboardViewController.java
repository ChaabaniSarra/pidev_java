package tn.esprit.controllers.equipe;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import tn.esprit.entities.OwnerDashboardStats;
import tn.esprit.services.ServiceEquipe;

import java.net.URL;
import java.sql.SQLException;
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
    private Label resultsLabel;
    @FXML
    private Label winRateLabel;
    @FXML
    private Label alertsLabel;
    @FXML
    private Label messageLabel;

    private final ServiceEquipe serviceEquipe = new ServiceEquipe();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadStats();
    }

    private void loadStats() {
        try {
            OwnerDashboardStats stats = serviceEquipe.getOwnerDashboardStatsForCurrentOwner();
            ownedTeamsLabel.setText(String.valueOf(stats.getOwnedTeamsCount()));
            joinedTeamsLabel.setText(String.valueOf(stats.getJoinedTeamsCount()));
            pendingRequestsLabel.setText(String.valueOf(stats.getPendingRequestsCount()));
            finishedMatchesLabel.setText(String.valueOf(stats.getTotalFinishedMatches()));
            resultsLabel.setText("V: " + stats.getWins() + "   N: " + stats.getDraws() + "   P: " + stats.getLosses());
            winRateLabel.setText(stats.getWinRate() + "%");

            String alerts = "Aucune alerte";
            if (stats.getPendingRequestsCount() > 0 || stats.isHasFullTeam()) {
                StringBuilder sb = new StringBuilder();
                if (stats.getPendingRequestsCount() > 0) {
                    sb.append("Demandes en attente: ").append(stats.getPendingRequestsCount());
                }
                if (stats.isHasFullTeam()) {
                    if (!sb.isEmpty()) {
                        sb.append(" | ");
                    }
                    sb.append("Au moins une equipe est pleine");
                }
                alerts = sb.toString();
            }
            alertsLabel.setText(alerts);
            messageLabel.setStyle("-fx-text-fill: #22c55e;");
            messageLabel.setText("Dashboard charge.");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Erreur dashboard owner: " + e.getMessage());
        }
    }
}
