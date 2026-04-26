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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Blog;
import tn.esprit.entities.Comment;
import tn.esprit.entities.User;
import tn.esprit.services.*;
import tn.esprit.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlogDetailController {

    @FXML private ImageView blogImage;
    @FXML private Label     categoryBadge;
    @FXML private Label     dateLabel;
    @FXML private Label     blogTitle;
    @FXML private Label     blogContent;
    @FXML private Label     commentHeaderLabel;
    @FXML private TextArea  newCommentArea;
    @FXML private VBox      commentsContainer;
    @FXML private Button    likeButton;
    @FXML private Label     likeCountLabel;
    @FXML private HBox      starsContainer;
    @FXML private Label     ratingLabel;

    private static final String HTDOCS_PATH = "C:/xampp/htdocs/blog_images/";

    private final ServiceComment         serviceComment  = new ServiceComment();
    private final ServiceBlogLike        serviceLike     = new ServiceBlogLike();
    private final ServiceBlogRating      serviceRating   = new ServiceBlogRating();
    private final ServiceCommentReaction serviceReaction = new ServiceCommentReaction();
    private final ServiceReport          serviceReport   = new ServiceReport();

    private Blog currentBlog;

    public void setBlog(Blog blog) {
        this.currentBlog = blog;
        displayBlogDetails();
        loadLikeSection();
        loadRatingSection();
        loadComments();
    }

    // ── Blog Details ──────────────────────────────────────────────────────

    private void displayBlogDetails() {
        blogTitle.setText(currentBlog.getTitle());
        blogContent.setText(currentBlog.getCategory());
        dateLabel.setText(currentBlog.getCreatedAt() != null
                ? currentBlog.getCreatedAt().toString().substring(0, 10) : "");
        categoryBadge.setText("BLOG");
        File imgFile = new File(HTDOCS_PATH + currentBlog.getContent());
        if (imgFile.exists()) {
            blogImage.setImage(new Image(imgFile.toURI().toString()));
        }
    }

    // ── MÉTIER 1 : Likes ─────────────────────────────────────────────────

    private void loadLikeSection() {
        User user = SessionManager.getCurrentUser();
        int count = serviceLike.getLikeCount(currentBlog.getId());
        boolean liked = user != null && serviceLike.hasLiked(currentBlog.getId(), user.getId());
        updateLikeUI(liked, count);
    }

    private void updateLikeUI(boolean liked, int count) {
        likeCountLabel.setText(String.valueOf(count));
        likeButton.setText(liked ? "❤️ Aimé" : "🤍 J'aime");
        likeButton.setStyle(
                "-fx-background-color: " + (liked ? "#ef4444" : "#1e293b") + ";" +
                        "-fx-text-fill: white; -fx-background-radius: 20;" +
                        "-fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 6 16 6 16;");
    }

    @FXML
    private void handleLike() {
        User user = SessionManager.getCurrentUser();
        if (user == null) { showAlert("Erreur", "Connectez-vous pour liker."); return; }

        // Toggle en DB
        boolean nowLiked = serviceLike.toggleLike(currentBlog.getId(), user.getId());
        int newCount = serviceLike.getLikeCount(currentBlog.getId());

        // ✅ Mise à jour dynamique — sans recharger la page
        updateLikeUI(nowLiked, newCount);
    }

    // ── MÉTIER 3 : Rating ────────────────────────────────────────────────

    private void loadRatingSection() {
        User user = SessionManager.getCurrentUser();
        double avg        = serviceRating.getAverageRating(currentBlog.getId());
        int    count      = serviceRating.getRatingCount(currentBlog.getId());
        int    userRating = user != null
                ? serviceRating.getUserRating(currentBlog.getId(), user.getId()) : 0;
        updateRatingUI(avg, count, userRating);
    }

    private void updateRatingUI(double avg, int count, int userRating) {
        ratingLabel.setText(count > 0
                ? String.format("⭐ %.1f / 5  (%d votes)", avg, count)
                : "⭐ Pas encore noté");

        starsContainer.getChildren().clear();
        User user = SessionManager.getCurrentUser();

        for (int i = 1; i <= 5; i++) {
            final int star = i;
            Label starLabel = new Label(i <= userRating ? "★" : "☆");
            starLabel.setStyle(
                    "-fx-font-size: 28px; -fx-cursor: hand; -fx-text-fill: " +
                            (i <= userRating ? "#f59e0b" : "#475569") + ";");

            // Hover — allume les étoiles
            starLabel.setOnMouseEntered(e -> {
                for (int j = 0; j < starsContainer.getChildren().size(); j++) {
                    Label s = (Label) starsContainer.getChildren().get(j);
                    s.setText(j < star ? "★" : "☆");
                    s.setStyle("-fx-font-size: 28px; -fx-cursor: hand; -fx-text-fill: " +
                            (j < star ? "#f59e0b" : "#475569") + ";");
                }
            });

            // Quitter hover — restaure l'état réel
            starLabel.setOnMouseExited(e -> {
                for (int j = 0; j < starsContainer.getChildren().size(); j++) {
                    Label s = (Label) starsContainer.getChildren().get(j);
                    s.setText(j < userRating ? "★" : "☆");
                    s.setStyle("-fx-font-size: 28px; -fx-cursor: hand; -fx-text-fill: " +
                            (j < userRating ? "#f59e0b" : "#475569") + ";");
                }
            });

            // Clic — sauvegarde en DB + mise à jour dynamique
            starLabel.setOnMouseClicked(e -> {
                if (user == null) { showAlert("Erreur", "Connectez-vous pour noter."); return; }
                serviceRating.rateOrUpdate(currentBlog.getId(), user.getId(), star);

                // ✅ Mise à jour dynamique — sans recharger la page
                double newAvg   = serviceRating.getAverageRating(currentBlog.getId());
                int    newCount = serviceRating.getRatingCount(currentBlog.getId());
                updateRatingUI(newAvg, newCount, star);
            });

            starsContainer.getChildren().add(starLabel);
        }
    }

    // ── Comments ─────────────────────────────────────────────────────────

    private void loadComments() {
        commentsContainer.getChildren().clear();
        List<Comment> comments = serviceComment.getByBlogId(currentBlog.getId());
        commentHeaderLabel.setText("Commentaires (" + comments.size() + ")");
        for (Comment c : comments) {
            commentsContainer.getChildren().add(createCommentCard(c));
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 10;" +
                "-fx-padding: 15; -fx-border-color: #1e293b; -fx-border-radius: 10;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(comment.getUserName().substring(0, 1).toUpperCase());
        avatar.setPrefSize(35, 35);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle("-fx-background-color: #334155; -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-font-weight: bold;");

        VBox userMeta = new VBox(2);
        Label name = new Label(comment.getUserName());
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label date = new Label(comment.getCreatedAt().toString().substring(0, 16));
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        userMeta.getChildren().addAll(name, date);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(avatar, userMeta, spacer);

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && (currentUser.getId() == comment.getUserId()
                || "admin@gmail.com".equals(currentUser.getEmail()))) {
            Button editBtn   = new Button("✏️");
            Button deleteBtn = new Button("🗑️");
            editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
            editBtn.setOnAction(e   -> handleEditComment(comment, card));
            deleteBtn.setOnAction(e -> handleDeleteComment(comment));
            header.getChildren().addAll(editBtn, deleteBtn);
        }

        // Content
        Label content = new Label(comment.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
        content.setPadding(new Insets(5, 0, 0, 0));

        // ── MÉTIER 2 : Réactions ──────────────────────────────────────
        HBox reactionsBar = new HBox(6);
        reactionsBar.setAlignment(Pos.CENTER_LEFT);
        refreshReactionBar(reactionsBar, comment);

        // ── MÉTIER 4 : Signalement ────────────────────────────────────
        Button reportBtn = new Button("🚩 Signaler");
        reportBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b;" +
                "-fx-font-size: 11px; -fx-cursor: hand;");
        reportBtn.setOnAction(e -> handleReport("comment", comment.getId()));

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);
        HBox bottomBar = new HBox(10, reactionsBar, bottomSpacer, reportBtn);
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(header, content, bottomBar);
        return card;
    }

    // ── MÉTIER 2 : Réactions dynamiques ──────────────────────────────────

    private void refreshReactionBar(HBox bar, Comment comment) {
        bar.getChildren().clear();

        String[] emojis     = {"👍", "👎", "😂", "😮", "🔥"};
        User     user       = SessionManager.getCurrentUser();
        Map<String, Integer> counts = serviceReaction.getReactionCounts(comment.getId());
        String userReaction = user != null
                ? serviceReaction.getUserReaction(comment.getId(), user.getId()) : null;

        for (String emoji : emojis) {
            int     count    = counts.getOrDefault(emoji, 0);
            boolean selected = emoji.equals(userReaction);

            Button btn = new Button(count > 0 ? emoji + " " + count : emoji);
            btn.setStyle(
                    "-fx-background-color: " + (selected ? "#7c3aed" : "#1e293b") + ";" +
                            "-fx-text-fill: white; -fx-background-radius: 12;" +
                            "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 10 4 10;");

            btn.setOnAction(e -> {
                if (user == null) { showAlert("Erreur", "Connectez-vous pour réagir."); return; }
                // ✅ Toggle en DB
                serviceReaction.toggleReaction(comment.getId(), user.getId(), emoji);
                // ✅ Mise à jour dynamique — seulement cette barre
                refreshReactionBar(bar, comment);
            });

            bar.getChildren().add(btn);
        }
    }

    // ── MÉTIER 4 : Signalement ────────────────────────────────────────────

    private void handleReport(String targetType, int targetId) {
        User user = SessionManager.getCurrentUser();
        if (user == null) { showAlert("Erreur", "Connectez-vous pour signaler."); return; }

        if (serviceReport.alreadyReported(user.getId(), targetType, targetId)) {
            showAlert("Info", "Vous avez déjà signalé ce contenu.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "spam", "spam", "offensant", "hors_sujet", "faux");
        dialog.setTitle("Signalement");
        dialog.setHeaderText("Pourquoi signalez-vous ce contenu ?");
        dialog.setContentText("Raison :");

        dialog.showAndWait().ifPresent(reason -> {
            serviceReport.addReport(user.getId(), targetType, targetId, reason);
            showAlert("Merci", "Votre signalement a été envoyé.");
        });
    }

    // ── Comments CRUD ─────────────────────────────────────────────────────

    @FXML
    private void handleAddComment() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) { showAlert("Erreur", "Connectez-vous pour commenter."); return; }
        String text = newCommentArea.getText().trim();
        if (text.isEmpty()) { showAlert("Erreur", "Le commentaire ne peut pas être vide."); return; }
        serviceComment.ajouter(new Comment(currentBlog.getId(), currentUser.getId(), text));
        newCommentArea.clear();
        loadComments();
    }

    private void handleEditComment(Comment comment, VBox card) {
        TextArea editArea = new TextArea(comment.getContent());
        editArea.setPrefHeight(80);
        editArea.setWrapText(true);
        editArea.setStyle("-fx-control-inner-background: #1e293b; -fx-text-fill: white;");

        Button saveBtn   = new Button("Enregistrer");
        Button cancelBtn = new Button("Annuler");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        cancelBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-cursor: hand;");

        HBox btns = new HBox(10, saveBtn, cancelBtn);
        btns.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().remove(1);
        card.getChildren().add(new VBox(10, editArea, btns));

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