package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Category;
import tn.esprit.entities.Product;
import tn.esprit.services.CategoryService;
import tn.esprit.services.ProductService;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterProduitController implements Initializable {

    @FXML private TextField nameField, descriptionField, priceField, stockField, imageField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private Label messageLabel, titleLabel;
    @FXML private Button submitBtn;
    @FXML private ImageView imagePreview;

    private ProductService productService = new ProductService();
    private CategoryService categoryService = new CategoryService();
    private Product productToEdit = null;
    private List<Category> categories;
    private String selectedImagePath = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        categories = categoryService.getAllCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));

        categoryCombo.setCellFactory(lv -> new ListCell<>() {
            protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.getName());
            }
        });
        categoryCombo.setButtonCell(new ListCell<>() {
            protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.getName());
            }
        });
    }

    // ✅ Appelé depuis ProduitsController pour modifier
    public void setProductToEdit(Product p) {
        this.productToEdit = p;
        titleLabel.setText("✏️ Modifier le Produit");
        submitBtn.setText("💾 Modifier");
        nameField.setText(p.getName());
        descriptionField.setText(p.getDescription());
        priceField.setText(String.valueOf(p.getPrice()));
        stockField.setText(String.valueOf(p.getStock()));
        imageField.setText(p.getImage());
        selectedImagePath = p.getImage();

        // Afficher preview de l'image existante
        File imgFile = new File("C:/wamp64/www/uploads/" + p.getImage());
        if (imgFile.exists()) {
            imagePreview.setImage(new Image(imgFile.toURI().toString()));
            imagePreview.setVisible(true);
        }

        categories.stream()
                .filter(c -> c.getId() == p.getCategoryId())
                .findFirst()
                .ifPresent(c -> categoryCombo.setValue(c));
    }

    // ✅ FileChooser — choisir image depuis PC
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(imageField.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // ✅ Copier dans wamp
                String destDir = "C:/wamp64/www/uploads/";
                new File(destDir).mkdirs();

                Path destination = Paths.get(destDir + selectedFile.getName());
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Sauvegarder le nom
                selectedImagePath = selectedFile.getName();
                imageField.setText(selectedImagePath);

                // Afficher preview
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);

                messageLabel.setStyle("-fx-text-fill: #27ae60;");
                messageLabel.setText("✅ Image sauvegardée !");

            } catch (Exception e) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("❌ Erreur image : " + e.getMessage());
            }
        }
    }

    // ✅ Ajouter ou Modifier
    @FXML
    private void handleSubmit() {
        if (nameField.getText().isEmpty() || priceField.getText().isEmpty()
                || stockField.getText().isEmpty() || categoryCombo.getValue() == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("❌ Remplissez tous les champs !");
            return;
        }

        try {
            String name = nameField.getText();
            String desc = descriptionField.getText();
            double price = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());
            String image = selectedImagePath.isEmpty() ? imageField.getText() : selectedImagePath;
            int catId = categoryCombo.getValue().getId();

            if (productToEdit == null) {
                // ➕ AJOUTER
                productService.addProduct(
                        new Product(name, desc, price, stock, image, catId)
                );
                messageLabel.setStyle("-fx-text-fill: #27ae60;");
                messageLabel.setText("✅ Produit ajouté !");
            } else {
                // ✏️ MODIFIER
                productToEdit.setName(name);
                productToEdit.setDescription(desc);
                productToEdit.setPrice(price);
                productToEdit.setStock(stock);
                productToEdit.setImage(image);
                productToEdit.setCategoryId(catId);
                productService.updateProduct(productToEdit);
                messageLabel.setStyle("-fx-text-fill: #27ae60;");
                messageLabel.setText("✅ Produit modifié !");
            }

            // Fermer après 1 seconde
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() ->
                        ((Stage) nameField.getScene().getWindow()).close()
                );
            }).start();

        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("❌ Prix et Stock doivent être des nombres !");
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}