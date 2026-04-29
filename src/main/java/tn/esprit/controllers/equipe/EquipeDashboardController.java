package tn.esprit.controllers.equipe;

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
import tn.esprit.entities.Equipe;
import tn.esprit.services.ServiceEquipe;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class EquipeDashboardController implements Initializable {

    @FXML
    private TableView<Equipe> equipeTable;
    @FXML
    private TableColumn<Equipe, String> nomCol;
    @FXML
    private TableColumn<Equipe, Integer> maxMembersCol;
    @FXML
    private TableColumn<Equipe, String> logoCol;
    @FXML
    private TableColumn<Equipe, String> actionsCol;
    @FXML
    private Label messageLabel;
    @FXML
    private Label totalEquipesLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortCombo;

    private final ServiceEquipe serviceEquipe = new ServiceEquipe();
    private final List<Equipe> allEquipes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupSearchAndSort();
        loadEquipes();
    }

    private void setupSearchAndSort() {
        sortCombo.setItems(FXCollections.observableArrayList(
                "ID croissant",
                "ID decroissant",
                "Nom A-Z",
                "Nom Z-A",
                "Max members croissant",
                "Max members decroissant"
        ));
        sortCombo.getSelectionModel().select("ID decroissant");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSort());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSort());
    }

    private void setupColumns() {
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        maxMembersCol.setCellValueFactory(new PropertyValueFactory<>("maxMembers"));
        logoCol.setCellValueFactory(new PropertyValueFactory<>("logo"));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
            editBtn.setStyle("-fx-background-color: #6e5cff; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    Equipe equipe = getTableView().getItems().get(getIndex());
                    openEditWindow(equipe);
                });

                deleteBtn.setOnAction(e -> {
                    Equipe equipe = getTableView().getItems().get(getIndex());
                    confirmDelete(equipe);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void loadEquipes() {
        try {
            List<Equipe> list = serviceEquipe.getAll();
            allEquipes.clear();
            allEquipes.addAll(list);
            applyFilterAndSort();
            totalEquipesLabel.setText(String.valueOf(list.size()));
            messageLabel.setText("");
        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Erreur chargement equipes : " + e.getMessage());
        }
    }

    private void applyFilterAndSort() {
        String keyword = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);

        List<Equipe> filtered = new ArrayList<>();
        for (Equipe equipe : allEquipes) {
            if (keyword.isEmpty()
                    || String.valueOf(equipe.getId()).contains(keyword)
                    || safeLower(equipe.getNom()).contains(keyword)
                    || safeLower(equipe.getLogo()).contains(keyword)
                    || String.valueOf(equipe.getMaxMembers()).contains(keyword)) {
                filtered.add(equipe);
            }
        }

        Comparator<Equipe> comparator = getSortComparator(sortCombo == null ? null : sortCombo.getValue());
        filtered.sort(comparator);
        equipeTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private Comparator<Equipe> getSortComparator(String sortValue) {
        if ("ID croissant".equals(sortValue)) {
            return Comparator.comparingInt(Equipe::getId);
        }
        if ("Nom A-Z".equals(sortValue)) {
            return Comparator.comparing(e -> safeLower(e.getNom()));
        }
        if ("Nom Z-A".equals(sortValue)) {
            return Comparator.comparing((Equipe e) -> safeLower(e.getNom())).reversed();
        }
        if ("Max members croissant".equals(sortValue)) {
            return Comparator.comparingInt(Equipe::getMaxMembers);
        }
        if ("Max members decroissant".equals(sortValue)) {
            return Comparator.comparingInt(Equipe::getMaxMembers).reversed();
        }
        return Comparator.comparingInt(Equipe::getId).reversed();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    @FXML
    private void handleNewEquipe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/equipe/ajouterEquipe.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une equipe");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadEquipes();
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Erreur ouverture formulaire : " + e.getMessage());
        }
    }

    private void openEditWindow(Equipe equipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/equipe/ajouterEquipe.fxml"));
            Parent root = loader.load();
            AjouterEquipeController controller = loader.getController();
            controller.setEquipeToEdit(equipe);
            Stage stage = new Stage();
            stage.setTitle("Modifier une equipe");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadEquipes();
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Erreur ouverture formulaire : " + e.getMessage());
        }
    }

    private void confirmDelete(Equipe equipe) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'equipe " + equipe.getNom() + " ?");
        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                try {
                    serviceEquipe.supprimer(equipe.getId());
                    messageLabel.setStyle("-fx-text-fill: #27ae60;");
                    messageLabel.setText("Equipe supprimee avec succes.");
                    loadEquipes();
                } catch (SQLException e) {
                    messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                    messageLabel.setText("Suppression impossible : " + e.getMessage());
                }
            }
        });
    }
}
