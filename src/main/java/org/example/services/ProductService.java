package org.example.services;

import org.example.entities.Product;
import org.example.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private Connection connection;

    public ProductService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ➕ CREATE
    public void addProduct(Product product) {
        String sql = "INSERT INTO product (name, description, price, stock, image, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setString(5, product.getImage());
            ps.setInt(6, product.getCategoryId());
            ps.executeUpdate();
            System.out.println("Produit ajouté : " + product.getName());
        } catch (SQLException e) {
            System.out.println("Erreur ajout : " + e.getMessage());
        }
    }

    // 📋 READ ALL
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM product";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("image"),
                        rs.getInt("category_id")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lecture : " + e.getMessage());
        }
        return list;
    }

    // 🔍 READ BY ID
    public Product getProductById(int id) {
        String sql = "SELECT * FROM product WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("image"),
                        rs.getInt("category_id")
                );
            }
        } catch (SQLException e) {
            System.out.println(" Erreur : " + e.getMessage());
        }
        return null;
    }

    // ✏️ UPDATE
    public void updateProduct(Product product) {
        String sql = "UPDATE product SET name=?, description=?, price=?, " +
                "stock=?, image=?, category_id=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStock());
            ps.setString(5, product.getImage());
            ps.setInt(6, product.getCategoryId());
            ps.setInt(7, product.getId());
            ps.executeUpdate();
            System.out.println("Produit mis à jour : " + product.getId());
        } catch (SQLException e) {
            System.out.println("Erreur mise à jour : " + e.getMessage());
        }
    }


    public void deleteProduct(int id) {
        String sql = "DELETE FROM product WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Produit supprimé : " + id);
        } catch (SQLException e) {
            System.out.println("Erreur suppression : " + e.getMessage());
        }
    }
}