package com.jnu.marketplace.service;

import com.jnu.marketplace.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("JNU Marketplace - Email Verification");
        message.setText(String.format(
            "Hello %s,\n\n" +
            "Welcome to JNU Marketplace! Please verify your email address by clicking the link below:\n\n" +
            "http://localhost:3000/verify-email?token=%s\n\n" +
            "If you didn't create an account, please ignore this email.\n\n" +
            "Best regards,\nJNU Marketplace Team",
            user.getFirstName(),
            generateVerificationToken(user)
        ));

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("JNU Marketplace - Password Reset");
        message.setText(String.format(
            "Hello %s,\n\n" +
            "You requested a password reset for your JNU Marketplace account.\n\n" +
            "Click the link below to reset your password:\n\n" +
            "http://localhost:3000/reset-password?token=%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "Best regards,\nJNU Marketplace Team",
            user.getFirstName(),
            resetToken
        ));

        mailSender.send(message);
    }

    public void sendTransactionNotification(User user, String transactionType, String itemName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("JNU Marketplace - Transaction Update");
        message.setText(String.format(
            "Hello %s,\n\n" +
            "Your %s transaction for '%s' has been updated.\n\n" +
            "Please log in to your account to view the details.\n\n" +
            "Best regards,\nJNU Marketplace Team",
            user.getFirstName(),
            transactionType,
            itemName
        ));

        mailSender.send(message);
    }

    public void sendNewMessageNotification(User user, String senderName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("JNU Marketplace - New Message");
        message.setText(String.format(
            "Hello %s,\n\n" +
            "You have received a new message from %s on JNU Marketplace.\n\n" +
            "Please log in to your account to view the message.\n\n" +
            "Best regards,\nJNU Marketplace Team",
            user.getFirstName(),
            senderName
        ));

        mailSender.send(message);
    }

    public void sendListingStatusUpdate(User user, String listingTitle, String status) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("JNU Marketplace - Listing Status Update");
        message.setText(String.format(
            "Hello %s,\n\n" +
            "Your listing '%s' status has been updated to: %s\n\n" +
            "Please log in to your account to view the details.\n\n" +
            "Best regards,\nJNU Marketplace Team",
            user.getFirstName(),
            listingTitle,
            status
        ));

        mailSender.send(message);
    }

    private String generateVerificationToken(User user) {
        // In a real implementation, this would generate a secure token
        // For now, we'll use a simple hash of the user's email and timestamp
        return java.util.Base64.getEncoder().encodeToString(
            (user.getEmail() + System.currentTimeMillis()).getBytes()
        );
    }
} 