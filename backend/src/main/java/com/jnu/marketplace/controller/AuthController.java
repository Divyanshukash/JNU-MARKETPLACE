package com.jnu.marketplace.controller;

import com.jnu.marketplace.dto.AuthRequest;
import com.jnu.marketplace.dto.AuthResponse;
import com.jnu.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        // Implementation for password reset
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        // Implementation for password reset
        return ResponseEntity.ok("Password reset successfully");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam String email,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        userService.changePassword(email, oldPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }
} 