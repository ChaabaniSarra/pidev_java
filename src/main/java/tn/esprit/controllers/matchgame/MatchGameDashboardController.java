package tn.esprit.controllers.matchgame;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.MatchGame;
import tn.esprit.services.ServiceMatchGame;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MatchGameDashboardController implements Initializable {

    @FXML
    private TableView<MatchGame> matchTable;
    @FXML
    private TableColumn<MatchGame, java.sql.Timestamp> dateCol;
    @FXML
    private TableColumn<MatchGame, Integer> score1Col;
    @FXML
    private TableColumn<MatchGame, Integer> score2Col;
    @FXML
    private TableColumn<MatchGame, String> statutCol;
    @FXML
    private TableColumn<MatchGame, Integer> equipe1Col;
    @FXML
    private TableColumn<MatchGame, Integer> equipe2Col;
    @FXML
    private TableColumn<MatchGame, Integer> tournoiCol;
    @FXML
    private TableColumn<MatchGame, String> actionsCol;
    @FXML
    private Label messageLabel;
    @FXML
    private Label totalMatchsLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortCombo;

    private final ServiceMatchGame serviceMatchGame = new ServiceMatchGame();
    private final List<MatchGame> allMatchs = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        setupSearchAndSort();
        loadMatchs();
    }

    private void setupSearchAndSort() {
        sortCombo.setItems(FXCollections.observableArrayList(
                "Date recente",
                "Date ancienne",
                "ID croissant",
                "ID decroissant",
                "Statut A-Z",
                "Statut Z-A"
        ));
        sortCombo.getSelectionModel().select("Date recente");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSort());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSort());
    }

    private void setupColumns() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateMatch"));
        score1Col.setCellValueFactory(new PropertyValueFactory<>("scoreTeam1"));
        score2Col.setCellValueFactory(new PropertyValueFactory<>("scoreTeam2"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        equipe1Col.setCellValueFactory(new PropertyValueFactory<>("equipe1Id"));
        equipe2Col.setCellValueFactory(new PropertyValueFactory<>("equipe2Id"));
        tournoiCol.setCellValueFactory(new PropertyValueFactory<>("tournoiId"));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
            editBtn.setStyle("-fx-background-color: #6e5cff; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    MatchGame match = getTableView().getItems().get(getIndex());
                    openEditWindow(match);
                });

                deleteBtn.setOnAction(e -> {
                    MatchGame match = getTableView().getItems().get(getIndex());
                    confirmDelete(match);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadMatchs() {
        try {
            List<MatchGame> list = serviceMatchGame.getAll();
            allMatchs.clear();
            allMatchs.addAll(list);
            applyFilterAndSort();
            totalMatchsLabel.setText(String.valueOf(list.size()));
            messageLabel.setText("");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Erreur chargement matchs : " + e.getMessage());
        }
    }

    private void applyFilterAndSort() {
        String keyword = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        List<MatchGame> filtered = new ArrayList<>();
        for (MatchGame match : allMatchs) {
            if (keyword.isEmpty() || matchesKeyword(match, keyword)) {
                filtered.add(match);
            }
        }

        Comparator<MatchGame> comparator = getSortComparator(sortCombo == null ? null : sortCombo.getValue());
        filtered.sort(comparator);
        matchTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private boolean matchesKeyword(MatchGame match, String keyword) {
        return String.valueOf(match.getId()).contains(keyword)
                || String.valueOf(match.getEquipe1Id()).contains(keyword)
                || String.valueOf(match.getEquipe2Id()).contains(keyword)
                || String.valueOf(match.getTournoiId()).contains(keyword)
                || safeLower(match.getStatut()).contains(keyword)
                || safeLower(String.valueOf(match.getDateMatch())).contains(keyword)
                || safeLower(String.valueOf(match.getScoreTeam1())).contains(keyword)
                || safeLower(String.valueOf(match.getScoreTeam2())).contains(keyword);
    }

    private Comparator<MatchGame> getSortComparator(String sortValue) {
        if ("Date ancienne".equals(sortValue)) {
            return Comparator.comparing(MatchGame::getDateMatch, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if ("ID croissant".equals(sortValue)) {
            return Comparator.comparingInt(MatchGame::getId);
        }
        if ("ID decroissant".equals(sortValue)) {
            return Comparator.comparingInt(MatchGame::getId).reversed();
        }
        if ("Statut A-Z".equals(sortValue)) {
            return Comparator.comparing(m -> safeLower(m.getStatut()));
        }
        if ("Statut Z-A".equals(sortValue)) {
            return Comparator.comparing((MatchGame m) -> safeLower(m.getStatut())).reversed();
        }
        return Comparator.comparing(MatchGame::getDateMatch, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    @FXML
    private void handleNewMatch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchgame/ajouterMatchGame.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter match");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadMatchs();
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Erreur ouverture formulaire : " + e.getMessage());
        }
    }

    private void openEditWindow(MatchGame match) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchgame/ajouterMatchGame.fxml"));
            Parent root = loader.load();
            AjouterMatchGameController controller = loader.getController();
            controller.setMatchToEdit(match);
            Stage stage = new Stage();
            stage.setTitle("Modifier match");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadMatchs();
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Erreur ouverture formulaire : " + e.getMessage());
        }
    }

    private void confirmDelete(MatchGame match) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le match #" + match.getId() + " ?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    serviceMatchGame.supprimer(match.getId());
                    messageLabel.setStyle("-fx-text-fill: #27ae60;");
                    messageLabel.setText("Match supprime.");
                    loadMatchs();
                } catch (SQLException e) {
                    messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                    messageLabel.setText("Suppression impossible : " + e.getMessage());
                }
            }
        });
    }
}
