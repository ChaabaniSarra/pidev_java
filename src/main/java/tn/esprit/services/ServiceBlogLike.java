package tn.esprit.services;

import tn.esprit.utils.MyDatabase;
import java.sql.*;

public class ServiceBlogLike {

    private final Connection cnx;

    public ServiceBlogLike() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    public boolean toggleLike(int blogId, int userId) {
        if (hasLiked(blogId, userId)) {
            removeLike(blogId, userId);
            return false;
        } else {
            addLike(blogId, userId);
            return true;
        }
    }

    public boolean hasLiked(int blogId, int userId) {
        String sql = "SELECT id FROM blog_like WHERE blog_id=? AND user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            ps.setInt(2, userId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getLikeCount(int blogId) {
        String sql = "SELECT like_count FROM blog WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, blogId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("like_count");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void addLike(int blogId, int userId) {
        try {
            cnx.setAutoCommit(false);
            try (PreparedStatement ps = cnx.prepareStatement(
                    "INSERT INTO blog_like (blog_id, user_id) VALUES (?,?)")) {
                ps.setInt(1, blogId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE blog SET like_count = like_count + 1 WHERE id=?")) {
                ps.setInt(1, blogId);
                ps.executeUpdate();
            }
            cnx.commit();
        } catch (SQLException e) {
            try { cnx.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { cnx.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void removeLike(int blogId, int userId) {
        try {
            cnx.setAutoCommit(false);
            try (PreparedStatement ps = cnx.prepareStatement(
                    "DELETE FROM blog_like WHERE blog_id=? AND user_id=?")) {
                ps.setInt(1, blogId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE blog SET like_count = GREATEST(like_count - 1, 0) WHERE id=?")) {
                ps.setInt(1, blogId);
                ps.executeUpdate();
            }
            cnx.commit();
        } catch (SQLException e) {
            try { cnx.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { cnx.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}