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
import org.mindrot.jbcrypt.BCrypt;

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
            ps.setString(3, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
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
            ps.setString(3, hashIfNeeded(user.getPassword()));
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
        String sql = "SELECT * FROM `user` WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashed = rs.getString("password");
                    if (isPasswordMatch(password, hashed)) {
                        if (!isBcryptHash(hashed)) {
                            migratePlainPasswordToBcrypt(rs.getInt("id"), password);
                        }
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
        }
        return null;
    }

    private Connection getConnectionOrThrow() throws SQLException {
        if (conn == null) {
            throw new SQLException("Database connection is not available.");
        }
        return conn;
    }

    private String hashIfNeeded(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        if (isBcryptHash(password)) {
            return password;
        }
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean isPasswordMatch(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            return BCrypt.checkpw(plainPassword, storedPassword);
        }
        return plainPassword.equals(storedPassword);
    }

    private boolean isBcryptHash(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }

    private void migratePlainPasswordToBcrypt(int userId, String plainPassword) throws SQLException {
        String sql = "UPDATE `user` SET password = ? WHERE id = ?";
        try (PreparedStatement ps = getConnectionOrThrow().prepareStatement(sql)) {
            ps.setString(1, BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}
