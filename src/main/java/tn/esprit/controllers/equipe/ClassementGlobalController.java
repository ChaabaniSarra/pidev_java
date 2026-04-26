package tn.esprit.controllers.equipe;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Pos;
import tn.esprit.entities.EquipeStanding;
import tn.esprit.services.ServiceEquipe;

import java.text.DecimalFormat;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ClassementGlobalController implements Initializable {

    @FXML
    private TableView<EquipeStanding> rankingTable;
    @FXML
    private TableColumn<EquipeStanding, Integer> rankCol;
    @FXML
    private TableColumn<EquipeStanding, String> equipeCol;
    @FXML
    private TableColumn<EquipeStanding, Integer> mjCol;
    @FXML
    private TableColumn<EquipeStanding, Integer> vCol;
    @FXML
    private TableColumn<EquipeStanding, Integer> nCol;
    @FXML
    private TableColumn<EquipeStanding, Integer> pCol;
    @FXML
    private TableColumn<EquipeStanding, Integer> bpCol;
    @FXML
    private TableColumn<EquipeStanding, Integer> bcCol;
    @FXML
    private TableColumn<EquipeStanding, Integer> diffCol;
    @FXML
    private TableColumn<EquipeStanding, Integer> pointsCol;
    @FXML
    private TableColumn<EquipeStanding, Double> ppmCol;
    @FXML
    private TableColumn<EquipeStanding, String> badgeCol;
    @FXML
    private Label messageLabel;

    private final ServiceEquipe serviceEquipe = new ServiceEquipe();
    private final DecimalFormat ppmFormat = new DecimalFormat("0.00");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyTableTheme();

        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        equipeCol.setCellValueFactory(new PropertyValueFactory<>("equipeNom"));
        mjCol.setCellValueFactory(new PropertyValueFactory<>("mj"));
        vCol.setCellValueFactory(new PropertyValueFactory<>("v"));
        nCol.setCellValueFactory(new PropertyValueFactory<>("n"));
        pCol.setCellValueFactory(new PropertyValueFactory<>("p"));
        bpCol.setCellValueFactory(new PropertyValueFactory<>("bp"));
        bcCol.setCellValueFactory(new PropertyValueFactory<>("bc"));
        diffCol.setCellValueFactory(new PropertyValueFactory<>("diff"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        ppmCol.setCellValueFactory(new PropertyValueFactory<>("ppm"));
        badgeCol.setCellValueFactory(new PropertyValueFactory<>("badge"));

        setupCellRendering();

        loadRanking();
    }

    private void applyTableTheme() {
        URL css = getClass().getResource("/styles/classement-global.css");
        if (css != null) {
            rankingTable.getStylesheets().add(css.toExternalForm());
        }
        rankingTable.getStyleClass().add("classement-table");
        rankingTable.setFixedCellSize(34);

        Label placeholder = new Label("Aucune equipe a afficher.");
        placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        rankingTable.setPlaceholder(placeholder);
    }

    private void setupCellRendering() {
        setCenteredNumberCell(rankCol);
        setCenteredNumberCell(mjCol);
        setCenteredNumberCell(vCol);
        setCenteredNumberCell(nCol);
        setCenteredNumberCell(pCol);
        setCenteredNumberCell(bpCol);
        setCenteredNumberCell(bcCol);
        setCenteredNumberCell(diffCol);
        setCenteredNumberCell(pointsCol);

        ppmCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setAlignment(Pos.CENTER);
                setText(ppmFormat.format(item));
                setStyle("-fx-text-fill: #7dd3fc; -fx-font-weight: 700;");
            }
        });

        badgeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setStyle("");
                    return;
                }

                String badge = item.trim();
                setText(badge);
                setAlignment(Pos.CENTER_LEFT);

                if ("Elite".equalsIgnoreCase(badge)) {
                    setStyle("-fx-background-color: rgba(245,158,11,0.20); -fx-text-fill: #fcd34d; -fx-font-weight: 700;");
                } else if ("Competitive".equalsIgnoreCase(badge)) {
                    setStyle("-fx-background-color: rgba(34,197,94,0.18); -fx-text-fill: #86efac; -fx-font-weight: 700;");
                } else {
                    setStyle("-fx-background-color: rgba(148,163,184,0.16); -fx-text-fill: #cbd5e1; -fx-font-weight: 700;");
                }
            }
        });

        rankCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setAlignment(Pos.CENTER);
                setText(String.valueOf(item));
                if (item == 1) {
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: 800;");
                } else if (item == 2) {
                    setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 800;");
                } else if (item == 3) {
                    setStyle("-fx-text-fill: #fb7185; -fx-font-weight: 800;");
                } else {
                    setStyle("-fx-text-fill: #e2e8f0; -fx-font-weight: 700;");
                }
            }
        });
    }

    private <T extends Number> void setCenteredNumberCell(TableColumn<EquipeStanding, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setAlignment(Pos.CENTER);
                setText(String.valueOf(item));
                setStyle("-fx-text-fill: #e2e8f0;");
            }
        });
    }

    private void loadRanking() {
        try {
            List<EquipeStanding> ranking = serviceEquipe.getGlobalRanking();
            rankingTable.setItems(FXCollections.observableArrayList(ranking));
            messageLabel.setText(ranking.isEmpty() ? "Aucune equipe disponible." : "Classement charge.");
            messageLabel.setStyle("-fx-text-fill: #22c55e;");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #ef4444;");
            messageLabel.setText("Erreur chargement classement: " + e.getMessage());
        }
    }
}
