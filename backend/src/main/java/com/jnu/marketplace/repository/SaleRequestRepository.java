package com.jnu.marketplace.repository;

import com.jnu.marketplace.model.Sale;
import com.jnu.marketplace.model.SaleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SaleRequest entity
 * 
 * This repository provides data access methods for purchase requests,
 * including offer management, status tracking, and user history.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Repository
public interface SaleRequestRepository extends MongoRepository<SaleRequest, String> {

    // Basic find methods
    List<SaleRequest> findByBuyerId(String buyerId);
    
    List<SaleRequest> findBySellerId(String sellerId);
    
    Page<SaleRequest> findByBuyerId(String buyerId, Pageable pageable);
    
    Page<SaleRequest> findBySellerId(String sellerId, Pageable pageable);

    // Listing-related requests
    List<SaleRequest> findByListingId(String listingId);
    
    Optional<SaleRequest> findByListingIdAndBuyerId(String listingId, String buyerId);
    
    List<SaleRequest> findByListingIdAndStatus(String listingId, SaleRequest.RequestStatus status);

    // Status-based methods
    List<SaleRequest> findByStatus(SaleRequest.RequestStatus status);
    
    List<SaleRequest> findByBuyerIdAndStatus(String buyerId, SaleRequest.RequestStatus status);
    
    List<SaleRequest> findBySellerIdAndStatus(String sellerId, SaleRequest.RequestStatus status);

    // Date-based methods
    List<SaleRequest> findByCreatedAtAfter(LocalDateTime dateTime);
    
    List<SaleRequest> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<SaleRequest> findByAcceptedAtAfter(LocalDateTime dateTime);
    
    List<SaleRequest> findByRejectedAtAfter(LocalDateTime dateTime);

    // Expired requests
    @Query("{'expiresAt': {'$lt': ?0}, 'status': 'PENDING'}")
    List<SaleRequest> findExpiredPendingRequests(LocalDateTime now);
    
    @Query("{'expiresAt': {'$lt': ?0}}")
    List<SaleRequest> findExpiredRequests(LocalDateTime now);

    // Price-based queries
    List<SaleRequest> findByOfferPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<SaleRequest> findByOfferPriceGreaterThanEqual(BigDecimal minPrice);
    
    List<SaleRequest> findByOfferPriceLessThanEqual(BigDecimal maxPrice);

    // Payment method queries
    List<SaleRequest> findByPaymentMethod(Sale.PaymentMethod paymentMethod);
    
    List<SaleRequest> findByBuyerIdAndPaymentMethod(String buyerId, Sale.PaymentMethod paymentMethod);

    // Delivery method queries
    List<SaleRequest> findByDeliveryMethod(Sale.DeliveryMethod deliveryMethod);
    
    List<SaleRequest> findByBuyerIdAndDeliveryMethod(String buyerId, Sale.DeliveryMethod deliveryMethod);

    // Complex queries
    @Query("{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}")
    List<SaleRequest> findRequestsInvolvingUser(String userId);
    
    @Query("{'$or': [{'buyerId': ?0}, {'sellerId': ?0}], 'status': 'PENDING'}")
    List<SaleRequest> findPendingRequestsInvolvingUser(String userId);
    
    @Query("{'sellerId': ?0, 'status': 'PENDING'}")
    List<SaleRequest> findPendingRequestsForSeller(String sellerId);
    
    @Query("{'buyerId': ?0, 'status': 'PENDING'}")
    List<SaleRequest> findPendingRequestsForBuyer(String buyerId);

    // Active requests (not expired, not cancelled)
    @Query("{'status': 'PENDING', 'expiresAt': {'$gt': ?0}}")
    List<SaleRequest> findActiveRequests(LocalDateTime now);
    
    @Query("{'sellerId': ?0, 'status': 'PENDING', 'expiresAt': {'$gt': ?1}}")
    List<SaleRequest> findActiveRequestsForSeller(String sellerId, LocalDateTime now);

    // Statistics methods
    long countByBuyerId(String buyerId);
    
    long countBySellerId(String sellerId);
    
    long countByStatus(SaleRequest.RequestStatus status);
    
    long countByBuyerIdAndStatus(String buyerId, SaleRequest.RequestStatus status);
    
    long countBySellerIdAndStatus(String sellerId, SaleRequest.RequestStatus status);
    
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
    long countByAcceptedAtAfter(LocalDateTime dateTime);
    
    long countByRejectedAtAfter(LocalDateTime dateTime);

    // User activity
    @Query("{'buyerId': ?0, 'createdAt': {'$gt': ?1}}")
    long countRequestsByBuyerAfter(String buyerId, LocalDateTime since);
    
    @Query("{'sellerId': ?0, 'createdAt': {'$gt': ?1}}")
    long countRequestsForSellerAfter(String sellerId, LocalDateTime since);

    // Recent activity
    @Query("{'createdAt': {'$gte': ?0}, 'status': 'PENDING'}")
    Page<SaleRequest> findRecentPendingRequests(LocalDateTime since, Pageable pageable);
    
    @Query("{'acceptedAt': {'$gte': ?0}}")
    Page<SaleRequest> findRecentAcceptedRequests(LocalDateTime since, Pageable pageable);

    // High-value offers
    @Query("{'offerPrice': {'$gte': ?0}, 'status': 'PENDING'}")
    Page<SaleRequest> findHighValuePendingRequests(BigDecimal minPrice, Pageable pageable);

    // User request history
    @Query("{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}")
    Page<SaleRequest> findUserRequestHistory(String userId, Pageable pageable);
} 