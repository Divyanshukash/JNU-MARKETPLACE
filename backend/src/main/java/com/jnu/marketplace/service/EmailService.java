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



    public void sendPasswordResetEmail(User user, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("JNU BAZAAR - Password Reset");
        message.setText(String.format(
            "Hello %s,\n\n" +
                          "You requested a password reset for your JNU BAZAAR account.\n\n" +
            "Click the link below to reset your password:\n\n" +
            "http://localhost:3000/reset-password?token=%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "Best regards,\nJNU BAZAAR Team",
            user.getFirstName(),
            resetToken
        ));

        mailSender.send(message);
    }



    public void sendListingStatusUpdate(User user, String listingTitle, String status) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("JNU BAZAAR - Listing Status Update");
        message.setText(String.format(
            "Hello %s,\n\n" +
            "Your listing '%s' status has been updated to: %s\n\n" +
            "Please log in to your account to view the details.\n\n" +
            "Best regards,\nJNU BAZAAR Team",
            user.getFirstName(),
            listingTitle,
            status
        ));

        mailSender.send(message);
    }


} 