package tn.esprit.services;

import tn.esprit.entities.Order;
import tn.esprit.entities.OrderItem;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.UUID;

public class OrderService {
    private Connection conn;

    public OrderService() {
        conn = MyDatabase.getInstance().getConnection();
    }

    public Order createOrder(Order order) {
        try {
            String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO `order` (order_number, total_price, status, first_name, last_name, " +
                            "email, address, city, postal_code, phone, user_id, created_at) " +
                            "VALUES (?, ?, 'pending', ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, orderNumber);
            ps.setDouble(2, order.getTotalPrice());
            ps.setString(3, order.getFirstName());
            ps.setString(4, order.getLastName());
            ps.setString(5, order.getEmail());
            ps.setString(6, order.getAddress());
            ps.setString(7, order.getCity());
            ps.setString(8, order.getPostalCode());
            ps.setString(9, order.getPhone());
            ps.setInt(10, order.getUserId());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                order.setId(keys.getInt(1));
                order.setOrderNumber(orderNumber);
            }
            return order;
        } catch (SQLException e) {
            System.out.println("Erreur createOrder: " + e.getMessage());
            return null;
        }
    }

    public void addOrderItem(OrderItem item) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO order_item (product_name, product_price, quantity, order_id, product_id) " +
                            "VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, item.getProductName());
            ps.setDouble(2, item.getProductPrice());
            ps.setInt(3, item.getQuantity());
            ps.setInt(4, item.getOrderId());
            ps.setInt(5, item.getProductId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur addOrderItem: " + e.getMessage());
        }
    }

    public void updateStripeSession(int orderId, String sessionId, String paymentIntentId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE `order` SET stripe_session_id=?, stripe_payment_intent_id=?, status='paid', paid_at=NOW() WHERE id=?"
            );
            ps.setString(1, sessionId);
            ps.setString(2, paymentIntentId);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur updateStripe: " + e.getMessage());
        }
    }
}