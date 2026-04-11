package  tn.esprit.services;

import  tn.esprit.entities.User;
import  tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {
    private Connection conn;

    public ServiceUser() {
        conn = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO `user`(email, roles, password, nom, is_active, google2fa_secret, is_2fa_enabled, google_oauth_id, oauth_provider, face_encoding, is_face_enabled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getRoles());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getNom());
            ps.setBoolean(5, user.isActive());
            ps.setString(6, user.getGoogle2faSecret());
            ps.setBoolean(7, user.isIs2faEnabled());
            ps.setString(8, user.getGoogleOauthId());
            ps.setString(9, user.getOauthProvider());
            ps.setString(10, user.getFaceEncoding());
            ps.setBoolean(11, user.isFaceEnabled());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(User user) throws SQLException {
        String sql = "UPDATE `user` SET email=?, roles=?, password=?, nom=?, is_active=?, google2fa_secret=?, is_2fa_enabled=?, google_oauth_id=?, oauth_provider=?, face_encoding=?, is_face_enabled=? WHERE id=?";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getRoles());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getNom());
            ps.setBoolean(5, user.isActive());
            ps.setString(6, user.getGoogle2faSecret());
            ps.setBoolean(7, user.isIs2faEnabled());
            ps.setString(8, user.getGoogleOauthId());
            ps.setString(9, user.getOauthProvider());
            ps.setString(10, user.getFaceEncoding());
            ps.setBoolean(11, user.isFaceEnabled());
            ps.setInt(12, user.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `user` WHERE id=?";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<User> getAll() throws SQLException {
        String sql = "SELECT * FROM `user`";
        List<User> users = new ArrayList<>();

        try (Statement stmt = getConnectionOrThrow().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("roles"),
                        rs.getString("password"),
                        rs.getString("nom"),
                        rs.getBoolean("is_active"),
                        rs.getString("google2fa_secret"),
                        rs.getBoolean("is_2fa_enabled"),
                        rs.getString("google_oauth_id"),
                        rs.getString("oauth_provider"),
                        rs.getString("face_encoding"),
                        rs.getBoolean("is_face_enabled")
                );
                users.add(user);
            }
        }

        return users;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM `user` WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean emailExistsForOtherUser(String email, int userId) throws SQLException {
        String sql = "SELECT 1 FROM `user` WHERE email = ? AND id <> ? LIMIT 1";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM `user` WHERE email = ? AND password = ? LIMIT 1";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("roles"),
                            rs.getString("password"),
                            rs.getString("nom"),
                            rs.getBoolean("is_active"),
                            rs.getString("google2fa_secret"),
                            rs.getBoolean("is_2fa_enabled"),
                            rs.getString("google_oauth_id"),
                            rs.getString("oauth_provider"),
                            rs.getString("face_encoding"),
                            rs.getBoolean("is_face_enabled")
                    );
                }
            }
        }
        return null;
    }

    private Connection getConnectionOrThrow() throws SQLException {
        if (conn == null) {
            throw new SQLException("Database connection is not available.");
        }
        return conn;
    }
}
