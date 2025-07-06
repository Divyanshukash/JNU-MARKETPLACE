package com.jnu.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jnu.marketplace.model.User;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Authentication response DTO for login and registration responses
 * 
 * This DTO handles authentication responses including user data,
 * tokens, and authentication status information.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserDto user;
    private String message;
    private boolean success;
    private LocalDateTime timestamp;

    // Constructors
    public AuthResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public AuthResponse(String token, UserDto user) {
        this();
        this.token = token;
        this.user = user;
        this.success = true;
    }

    public AuthResponse(String token, String refreshToken, UserDto user) {
        this(token, user);
        this.refreshToken = refreshToken;
    }

    public AuthResponse(String message, boolean success) {
        this();
        this.message = message;
        this.success = success;
    }

    // Static factory methods
    public static AuthResponse success(String token, UserDto user) {
        return new AuthResponse(token, user);
    }

    public static AuthResponse success(String token, String refreshToken, UserDto user) {
        return new AuthResponse(token, refreshToken, user);
    }

    public static AuthResponse success(String message) {
        return new AuthResponse(message, true);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(message, false);
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Inner User DTO class
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserDto {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String profilePicture;
        private String bio;
        private String department;
        private String studentId;
        private String facultyId;
        private String role;
        private String status;
        private boolean emailVerified;
        private double rating;
        private int totalRatings;
        private int totalTransactions;
        private LocalDateTime lastLogin;
        private LocalDateTime createdAt;

        // Constructors
        public UserDto() {}

        public UserDto(User user) {
            this.id = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
            this.phoneNumber = user.getPhoneNumber();
            this.profilePicture = user.getProfilePicture();
            this.bio = user.getBio();
            this.department = user.getDepartment();
            this.studentId = user.getStudentId();
            this.facultyId = user.getFacultyId();
            this.role = user.getRole().name();
            this.status = user.getStatus().name();
            this.emailVerified = user.isEmailVerified();
            this.rating = user.getRating();
            this.totalRatings = user.getTotalRatings();
            this.totalTransactions = user.getTotalTransactions();
            this.lastLogin = user.getLastLogin();
            this.createdAt = user.getCreatedAt();
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getFacultyId() {
            return facultyId;
        }

        public void setFacultyId(String facultyId) {
            this.facultyId = facultyId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isEmailVerified() {
            return emailVerified;
        }

        public void setEmailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public int getTotalRatings() {
            return totalRatings;
        }

        public void setTotalRatings(int totalRatings) {
            this.totalRatings = totalRatings;
        }

        public int getTotalTransactions() {
            return totalTransactions;
        }

        public void setTotalTransactions(int totalTransactions) {
            this.totalTransactions = totalTransactions;
        }

        public LocalDateTime getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        // Helper methods
        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + (token != null ? "***" : null) + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", user=" + user +
                ", message='" + message + '\'' +
                ", success=" + success +
                ", timestamp=" + timestamp +
                '}';
    }
} 