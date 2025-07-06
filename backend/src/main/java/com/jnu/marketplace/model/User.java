package com.jnu.marketplace.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User entity representing a JNU community member
 * 
 * This entity stores user information including authentication details,
 * profile information, preferences, and relationships with other entities.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Document(collection = "users")
public class User implements UserDetails {

    @Id
    private String id;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Field("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Field("last_name")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Indexed(unique = true)
    @Field("email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @JsonIgnore
    @Field("password")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Field("phone_number")
    private String phoneNumber;

    @Field("profile_picture")
    private String profilePicture;

    @Field("bio")
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    @Field("department")
    private String department;

    @Field("student_id")
    private String studentId;

    @Field("faculty_id")
    private String facultyId;

    @Field("role")
    private UserRole role = UserRole.STUDENT;

    @Field("status")
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Field("email_verified")
    private boolean emailVerified = false;

    @Field("email_verification_token")
    private String emailVerificationToken;

    @Field("email_verification_expires")
    private LocalDateTime emailVerificationExpires;

    @Field("reset_password_token")
    private String resetPasswordToken;

    @Field("reset_password_expires")
    private LocalDateTime resetPasswordExpires;

    @Field("last_login")
    private LocalDateTime lastLogin;

    @Field("login_attempts")
    private int loginAttempts = 0;

    @Field("account_locked")
    private boolean accountLocked = false;

    @Field("account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Field("preferences")
    private UserPreferences preferences = new UserPreferences();

    @Field("wishlist")
    private Set<String> wishlist = new HashSet<>();

    @Field("favorites")
    private Set<String> favorites = new HashSet<>();

    @Field("following")
    private Set<String> following = new HashSet<>();

    @Field("followers")
    private Set<String> followers = new HashSet<>();

    @Field("rating")
    private double rating = 0.0;

    @Field("total_ratings")
    private int totalRatings = 0;

    @Field("total_transactions")
    private int totalTransactions = 0;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public User() {}

    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    // UserDetails implementation
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !accountLocked || (accountLockedUntil != null && LocalDateTime.now().isAfter(accountLockedUntil));
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE && emailVerified;
    }

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void incrementLoginAttempts() {
        this.loginAttempts++;
        if (this.loginAttempts >= 5) {
            this.accountLocked = true;
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountLocked = false;
        this.accountLockedUntil = null;
    }

    public void addToWishlist(String listingId) {
        this.wishlist.add(listingId);
    }

    public void removeFromWishlist(String listingId) {
        this.wishlist.remove(listingId);
    }

    public void addToFavorites(String listingId) {
        this.favorites.add(listingId);
    }

    public void removeFromFavorites(String listingId) {
        this.favorites.remove(listingId);
    }

    public void followUser(String userId) {
        this.following.add(userId);
    }

    public void unfollowUser(String userId) {
        this.following.remove(userId);
    }

    public void addFollower(String userId) {
        this.followers.add(userId);
    }

    public void removeFollower(String userId) {
        this.followers.remove(userId);
    }

    public void updateRating(double newRating) {
        this.totalRatings++;
        this.rating = ((this.rating * (this.totalRatings - 1)) + newRating) / this.totalRatings;
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

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken;
    }

    public LocalDateTime getEmailVerificationExpires() {
        return emailVerificationExpires;
    }

    public void setEmailVerificationExpires(LocalDateTime emailVerificationExpires) {
        this.emailVerificationExpires = emailVerificationExpires;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordExpires() {
        return resetPasswordExpires;
    }

    public void setResetPasswordExpires(LocalDateTime resetPasswordExpires) {
        this.resetPasswordExpires = resetPasswordExpires;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public LocalDateTime getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public Set<String> getWishlist() {
        return wishlist;
    }

    public void setWishlist(Set<String> wishlist) {
        this.wishlist = wishlist;
    }

    public Set<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(Set<String> favorites) {
        this.favorites = favorites;
    }

    public Set<String> getFollowing() {
        return following;
    }

    public void setFollowing(Set<String> following) {
        this.following = following;
    }

    public Set<String> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<String> followers) {
        this.followers = followers;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Enums
    public enum UserRole {
        STUDENT, FACULTY, STAFF, ADMIN
    }

    public enum UserStatus {
        PENDING_VERIFICATION, ACTIVE, SUSPENDED, BANNED
    }

    // Inner class for user preferences
    public static class UserPreferences {
        @Field("notifications_enabled")
        private boolean notificationsEnabled = true;

        @Field("email_notifications")
        private boolean emailNotifications = true;

        @Field("push_notifications")
        private boolean pushNotifications = true;

        @Field("sms_notifications")
        private boolean smsNotifications = false;

        @Field("privacy_level")
        private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

        @Field("language")
        private String language = "en";

        @Field("timezone")
        private String timezone = "Asia/Kolkata";

        // Getters and Setters
        public boolean isNotificationsEnabled() {
            return notificationsEnabled;
        }

        public void setNotificationsEnabled(boolean notificationsEnabled) {
            this.notificationsEnabled = notificationsEnabled;
        }

        public boolean isEmailNotifications() {
            return emailNotifications;
        }

        public void setEmailNotifications(boolean emailNotifications) {
            this.emailNotifications = emailNotifications;
        }

        public boolean isPushNotifications() {
            return pushNotifications;
        }

        public void setPushNotifications(boolean pushNotifications) {
            this.pushNotifications = pushNotifications;
        }

        public boolean isSmsNotifications() {
            return smsNotifications;
        }

        public void setSmsNotifications(boolean smsNotifications) {
            this.smsNotifications = smsNotifications;
        }

        public PrivacyLevel getPrivacyLevel() {
            return privacyLevel;
        }

        public void setPrivacyLevel(PrivacyLevel privacyLevel) {
            this.privacyLevel = privacyLevel;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public enum PrivacyLevel {
            PUBLIC, FRIENDS_ONLY, PRIVATE
        }
    }
} 