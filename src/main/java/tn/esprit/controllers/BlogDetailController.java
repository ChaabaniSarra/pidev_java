package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Blog;
import tn.esprit.entities.Comment;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceComment;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class BlogDetailController {

    @FXML private ImageView blogImage;
    @FXML private Label categoryBadge;
    @FXML private Label dateLabel;
    @FXML private Label blogTitle;
    @FXML private Label blogContent;
    @FXML private Label commentHeaderLabel;
    @FXML private TextArea newCommentArea;
    @FXML private VBox commentsContainer;

    private static final String HTDOCS_PATH = "C:/xampp/htdocs/blog_images/";
    private final ServiceComment serviceComment = new ServiceComment();
    private Blog currentBlog;

    public void setBlog(Blog blog) {
        this.currentBlog = blog;
        displayBlogDetails();
        loadComments();
    }

    private void displayBlogDetails() {
        blogTitle.setText(currentBlog.getTitle());
        blogContent.setText(currentBlog.getCategory()); // In this project 'category' seems to be used for description/content sometimes
        dateLabel.setText(currentBlog.getCreatedAt() != null ? currentBlog.getCreatedAt().toString().substring(0, 10) : "");
        categoryBadge.setText(currentBlog.getCategory() != null ? "BLOG" : "BLOG");

        File imgFile = new File(HTDOCS_PATH + currentBlog.getContent());
        if (imgFile.exists()) {
            blogImage.setImage(new Image(imgFile.toURI().toString()));
        }
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        List<Comment> comments = serviceComment.getByBlogId(currentBlog.getId());
        commentHeaderLabel.setText("Commentaires (" + comments.size() + ")");

        for (Comment comment : comments) {
            commentsContainer.getChildren().add(createCommentCard(comment));
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 10; -fx-padding: 15; -fx-border-color: #1e293b; -fx-border-radius: 10;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(comment.getUserName().substring(0, 1).toUpperCase());
        avatar.setPrefSize(35, 35);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold;");

        VBox userMeta = new VBox(2);
        Label name = new Label(comment.getUserName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label date = new Label(comment.getCreatedAt().toString().substring(0, 16));
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        userMeta.getChildren().addAll(name, date);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, userMeta, spacer);

        // Action buttons (if owner)
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && (currentUser.getId() == comment.getUserId() || "admin@gmail.com".equals(currentUser.getEmail()))) {
            Button editBtn = new Button("✏️");
            Button deleteBtn = new Button("🗑️");
            editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand;");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");

            editBtn.setOnAction(e -> handleEditComment(comment, card));
            deleteBtn.setOnAction(e -> handleDeleteComment(comment));

            header.getChildren().addAll(editBtn, deleteBtn);
        }

        Label content = new Label(comment.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
        content.setPadding(new Insets(5, 0, 0, 0));

        card.getChildren().addAll(header, content);
        return card;
    }

    @FXML
    private void handleAddComment() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté pour commenter.");
            return;
        }

        String text = newCommentArea.getText().trim();
        if (text.isEmpty()) {
            showAlert("Erreur", "Le commentaire ne peut pas être vide.");
            return;
        }

        Comment comment = new Comment(currentBlog.getId(), currentUser.getId(), text);
        serviceComment.ajouter(comment);
        newCommentArea.clear();
        loadComments();
    }

    private void handleEditComment(Comment comment, VBox card) {
        TextArea editArea = new TextArea(comment.getContent());
        editArea.setPrefHeight(80);
        editArea.setWrapText(true);
        editArea.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white;");

        Button saveBtn = new Button("Enregistrer");
        Button cancelBtn = new Button("Annuler");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        cancelBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-cursor: hand;");

        HBox btns = new HBox(10, saveBtn, cancelBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox editLayout = new VBox(10, editArea, btns);
        
        // Replace previous content with edit layout
        card.getChildren().remove(1); 
        card.getChildren().add(editLayout);

        saveBtn.setOnAction(e -> {
            String newText = editArea.getText().trim();
            if (!newText.isEmpty()) {
                comment.setContent(newText);
                serviceComment.modifier(comment);
                loadComments();
            }
        });

        cancelBtn.setOnAction(e -> loadComments());
    }

    private void handleDeleteComment(Comment comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer ce commentaire ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceComment.supprimer(comment.getId(), currentBlog.getId());
            loadComments();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherBlogs.fxml"));
            Stage stage = (Stage) blogTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
