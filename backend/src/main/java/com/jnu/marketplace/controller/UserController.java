package com.jnu.marketplace.controller;

import com.jnu.marketplace.model.User;
import com.jnu.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User updatedUser = userService.updateProfile(email, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/wishlist")
    public ResponseEntity<List<String>> getWishlist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user.getWishlist());
    }

    @PostMapping("/wishlist/{listingId}")
    public ResponseEntity<String> addToWishlist(@PathVariable String listingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        userService.addToWishlist(user.getId(), listingId);
        return ResponseEntity.ok("Added to wishlist");
    }

    @DeleteMapping("/wishlist/{listingId}")
    public ResponseEntity<String> removeFromWishlist(@PathVariable String listingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);
        userService.removeFromWishlist(user.getId(), listingId);
        return ResponseEntity.ok("Removed from wishlist");
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        // Implementation for user search
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/follow/{userId}")
    public ResponseEntity<String> followUser(@PathVariable String userId) {
        // Implementation for following users
        return ResponseEntity.ok("User followed");
    }

    @DeleteMapping("/follow/{userId}")
    public ResponseEntity<String> unfollowUser(@PathVariable String userId) {
        // Implementation for unfollowing users
        return ResponseEntity.ok("User unfollowed");
    }

    @GetMapping("/followers")
    public ResponseEntity<List<User>> getFollowers() {
        // Implementation for getting followers
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/following")
    public ResponseEntity<List<User>> getFollowing() {
        // Implementation for getting following
        return ResponseEntity.ok(List.of());
    }
} 