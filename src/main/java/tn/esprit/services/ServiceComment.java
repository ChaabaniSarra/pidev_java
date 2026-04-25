package tn.esprit.services;

import tn.esprit.entities.Comment;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceComment {

    private final Connection cnx;

    public ServiceComment() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    public void ajouter(Comment comment) {
        String sql = "INSERT INTO comment (blog_id, user_id, content, created_at) VALUES (?, ?, ?, NOW())";
        String updateCount = "UPDATE blog SET comment_count = comment_count + 1 WHERE id = ?";

        try {
            cnx.setAutoCommit(false);
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, comment.getBlogId());
                ps.setInt(2, comment.getUserId());
                ps.setString(3, comment.getContent());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = cnx.prepareStatement(updateCount)) {
                ps.setInt(1, comment.getBlogId());
                ps.executeUpdate();
            }
            cnx.commit();
            System.out.println("✅ Commentaire ajouté");
        } catch (SQLException e) {
            try { cnx.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { cnx.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public List<Comment> getByBlogId(int blogId) {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, u.nom FROM comment c " +
                     "JOIN user u ON c.user_id = u.id " +
                     "WHERE c.blog_id = ? " +
                     "ORDER BY c.created_at DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment c = new Comment();
                    c.setId(rs.getInt("id"));
                    c.setBlogId(rs.getInt("blog_id"));
                    c.setUserId(rs.getInt("user_id"));
                    c.setUserName(rs.getString("nom"));
                    c.setContent(rs.getString("content"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void modifier(Comment comment) {
        String sql = "UPDATE comment SET content = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, comment.getContent());
            ps.setInt(2, comment.getId());
            ps.executeUpdate();
            System.out.println("✅ Commentaire modifié");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int commentId, int blogId) {
        String sql = "DELETE FROM comment WHERE id = ?";
        String updateCount = "UPDATE blog SET comment_count = comment_count - 1 WHERE id = ?";

        try {
            cnx.setAutoCommit(false);
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, commentId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = cnx.prepareStatement(updateCount)) {
                ps.setInt(1, blogId);
                ps.executeUpdate();
            }
            cnx.commit();
            System.out.println("✅ Commentaire supprimé");
        } catch (SQLException e) {
            try { cnx.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { cnx.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
