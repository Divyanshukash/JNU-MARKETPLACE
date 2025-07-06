package com.jnu.marketplace.repository;

import com.jnu.marketplace.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * 
 * This repository provides data access methods for user management,
 * including authentication, profile management, and user search operations.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Authentication methods
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndEmailVerifiedTrue(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByResetPasswordToken(String token);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndEmailVerifiedTrue(String email);

    // User status methods
    List<User> findByStatus(User.UserStatus status);
    
    List<User> findByEmailVerified(boolean emailVerified);
    
    List<User> findByAccountLockedTrue();
    
    List<User> findByAccountLockedUntilBefore(LocalDateTime dateTime);

    // Role-based methods
    List<User> findByRole(User.UserRole role);
    
    List<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status);
    
    Page<User> findByRole(User.UserRole role, Pageable pageable);

    // Department and academic methods
    List<User> findByDepartment(String department);
    
    List<User> findByDepartmentAndRole(String department, User.UserRole role);
    
    List<User> findByStudentId(String studentId);
    
    List<User> findByFacultyId(String facultyId);

    // Search methods
    @Query("{'$or': [{'firstName': {'$regex': ?0, '$options': 'i'}}, {'lastName': {'$regex': ?0, '$options': 'i'}}, {'email': {'$regex': ?0, '$options': 'i'}}]}")
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String searchTerm);
    
    @Query("{'$or': [{'firstName': {'$regex': ?0, '$options': 'i'}}, {'lastName': {'$regex': ?0, '$options': 'i'}}]}")
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("{'$or': [{'firstName': {'$regex': ?0, '$options': 'i'}}, {'lastName': {'$regex': ?0, '$options': 'i'}}, {'email': {'$regex': ?0, '$options': 'i'}}, {'department': {'$regex': ?0, '$options': 'i'}}]}")
    Page<User> searchUsers(String searchTerm, Pageable pageable);

    // Rating and activity methods
    List<User> findByRatingGreaterThanEqual(double rating);
    
    List<User> findByTotalTransactionsGreaterThan(int minTransactions);
    
    List<User> findByTotalRatingsGreaterThan(int minRatings);
    
    @Query("{'rating': {'$gte': ?0}, 'totalRatings': {'$gte': ?1}}")
    List<User> findByRatingAndMinRatings(double minRating, int minRatings);

    // Date-based methods
    List<User> findByCreatedAtAfter(LocalDateTime dateTime);
    
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<User> findByLastLoginAfter(LocalDateTime dateTime);
    
    List<User> findByLastLoginBefore(LocalDateTime dateTime);

    // Account activity methods
    @Query("{'lastLogin': {'$lt': ?0}}")
    List<User> findInactiveUsers(LocalDateTime inactiveThreshold);
    
    @Query("{'createdAt': {'$gte': ?0, '$lte': ?1}}")
    List<User> findUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Verification methods
    List<User> findByEmailVerifiedFalse();
    
    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime dateTime);
    
    List<User> findByEmailVerificationExpiresBefore(LocalDateTime dateTime);
    
    List<User> findByResetPasswordExpiresBefore(LocalDateTime dateTime);

    // Login attempt methods
    List<User> findByLoginAttemptsGreaterThan(int maxAttempts);
    
    List<User> findByAccountLockedTrueAndAccountLockedUntilBefore(LocalDateTime dateTime);

    // Wishlist and favorites methods
    @Query("{'wishlist': ?0}")
    List<User> findUsersWithListingInWishlist(String listingId);
    
    @Query("{'favorites': ?0}")
    List<User> findUsersWithListingInFavorites(String listingId);
    
    @Query("{'wishlist': {'$size': {'$gt': 0}}}")
    List<User> findUsersWithWishlist();
    
    @Query("{'favorites': {'$size': {'$gt': 0}}}")
    List<User> findUsersWithFavorites();

    // Following and followers methods
    @Query("{'following': ?0}")
    List<User> findFollowersOfUser(String userId);
    
    @Query("{'followers': ?0}")
    List<User> findUsersFollowing(String userId);
    
    @Query("{'following': {'$size': {'$gt': 0}}}")
    List<User> findUsersWithFollowing();
    
    @Query("{'followers': {'$size': {'$gt': 0}}}")
    List<User> findUsersWithFollowers();

    // Complex queries
    @Query("{'$and': [{'status': 'ACTIVE'}, {'emailVerified': true}, {'role': ?0}]}")
    Page<User> findActiveVerifiedUsersByRole(User.UserRole role, Pageable pageable);
    
    @Query("{'$and': [{'status': 'ACTIVE'}, {'emailVerified': true}, {'rating': {'$gte': ?0}}, {'totalRatings': {'$gte': ?1}}]}")
    Page<User> findTopRatedUsers(double minRating, int minRatings, Pageable pageable);
    
    @Query("{'$and': [{'status': 'ACTIVE'}, {'emailVerified': true}, {'totalTransactions': {'$gte': ?0}}]}")
    Page<User> findMostActiveUsers(int minTransactions, Pageable pageable);

    // Statistics methods
    long countByStatus(User.UserStatus status);
    
    long countByRole(User.UserRole role);
    
    long countByEmailVerified(boolean emailVerified);
    
    long countByDepartment(String department);
    
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
    long countByLastLoginAfter(LocalDateTime dateTime);

    // Custom aggregation queries
    @Query(value = "{}", fields = "{'department': 1, 'role': 1, 'status': 1}")
    List<User> findUserStats();
    
    @Query(value = "{'status': 'ACTIVE'}", fields = "{'rating': 1, 'totalRatings': 1, 'totalTransactions': 1}")
    List<User> findActiveUserMetrics();
} 