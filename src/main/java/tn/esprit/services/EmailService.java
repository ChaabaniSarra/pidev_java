package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "saadamaryem776@gmail.com"; // ← ton email
    private static final String PASSWORD = "dtwl bzpv zzmx mcjm";     // ← mot de passe app Gmail

    public void sendLowStockAlert(String productName, int stock, String toEmail) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("⚠️ Alerte Stock Faible — " + productName);

            String body = """
                    <html>
                    <body style="font-family: Arial; background-color: #0f172a; color: white; padding: 20px;">
                        <div style="background-color: #1e293b; padding: 20px; border-radius: 10px;">
                            <h2 style="color: #f87171;">⚠️ Alerte Stock Faible !</h2>
                            <p>Le produit <strong style="color: #7c3aed;">%s</strong> a un stock critique.</p>
                            <div style="background-color: #0f172a; padding: 15px; border-radius: 8px;">
                                <p>📦 Produit : <strong>%s</strong></p>
                                <p>🔢 Stock restant : <strong style="color: #f87171;">%d</strong></p>
                                <p>⚡ Action requise : Réapprovisionner immédiatement !</p>
                            </div>
                            <p style="color: #94a3b8; font-size: 12px;">
                                Gaming Gear Management System
                            </p>
                        </div>
                    </body>
                    </html>
                    """.formatted(productName, productName, stock);

            message.setContent(body, "text/html; charset=utf-8");
            Transport.send(message);

            System.out.println("✅ Email envoyé pour : " + productName);

        } catch (MessagingException e) {
            System.out.println("❌ Erreur email : " + e.getMessage());
        }
    }
}