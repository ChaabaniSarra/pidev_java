package org.example.services;

import org.example.entities.Blog;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceBlog {

    private Connection cnx;

    public ServiceBlog() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    // CREATE
    public void ajouter(Blog blog) {
        String sql = "INSERT INTO blog (title, content, created_at, category, image_name, comment_count) VALUES (?, ?, NOW(), ?, ?, 0)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getContent());
            ps.setString(3, blog.getCategory());
            ps.setString(4, blog.getImageName());
            ps.executeUpdate();

            System.out.println("✅ Blog ajouté");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<Blog> afficher() {
        List<Blog> list = new ArrayList<>();
        String sql = "SELECT * FROM blog";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Blog b = new Blog();
                b.setId(rs.getInt("id"));
                b.setTitle(rs.getString("title"));
                b.setContent(rs.getString("content"));
                b.setCreatedAt(rs.getTimestamp("created_at"));
                b.setCategory(rs.getString("category"));
                b.setImageName(rs.getString("image_name"));
                b.setCommentCount(rs.getInt("comment_count"));

                list.add(b);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public void modifier(Blog blog) {
        String sql = "UPDATE blog SET title=?, content=?, category=?, image_name=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getContent());
            ps.setString(3, blog.getCategory());
            ps.setString(4, blog.getImageName());
            ps.setInt(5, blog.getId());

            ps.executeUpdate();
            System.out.println("✅ Blog modifié");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimer(int id) {
        String sql = "DELETE FROM blog WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Blog supprimé");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}