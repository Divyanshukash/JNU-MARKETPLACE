package com.jnu.marketplace.repository;

import com.jnu.marketplace.model.Sale;
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
 * Repository interface for Sale entity
 * 
 * This repository provides data access methods for sale management,
 * including transaction history, user sales, and analytics.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Repository
public interface SaleRepository extends MongoRepository<Sale, String> {

    // Basic find methods
    List<Sale> findBySellerId(String sellerId);
    
    List<Sale> findByBuyerId(String buyerId);
    
    Page<Sale> findBySellerId(String sellerId, Pageable pageable);
    
    Page<Sale> findByBuyerId(String buyerId, Pageable pageable);

    // Listing-related sales
    Optional<Sale> findByListingId(String listingId);
    
    List<Sale> findByListingIdAndStatus(String listingId, Sale.SaleStatus status);

    // Status-based methods
    List<Sale> findByStatus(Sale.SaleStatus status);
    
    List<Sale> findBySellerIdAndStatus(String sellerId, Sale.SaleStatus status);
    
    List<Sale> findByBuyerIdAndStatus(String buyerId, Sale.SaleStatus status);

    // Date-based methods
    List<Sale> findBySaleDateAfter(LocalDateTime dateTime);
    
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Sale> findByCreatedAtAfter(LocalDateTime dateTime);

    // Payment method queries
    List<Sale> findByPaymentMethod(Sale.PaymentMethod paymentMethod);
    
    List<Sale> findBySellerIdAndPaymentMethod(String sellerId, Sale.PaymentMethod paymentMethod);

    // Delivery method queries
    List<Sale> findByDeliveryMethod(Sale.DeliveryMethod deliveryMethod);
    
    List<Sale> findBySellerIdAndDeliveryMethod(String sellerId, Sale.DeliveryMethod deliveryMethod);

    // Price-based queries
    List<Sale> findBySalePriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<Sale> findBySalePriceGreaterThanEqual(BigDecimal minPrice);
    
    List<Sale> findBySalePriceLessThanEqual(BigDecimal maxPrice);

    // Rating queries
    List<Sale> findByBuyerRating(Integer rating);
    
    List<Sale> findBySellerRating(Integer rating);
    
    List<Sale> findByBuyerRatingGreaterThanEqual(Integer minRating);
    
    List<Sale> findBySellerRatingGreaterThanEqual(Integer minRating);

    // Complex queries
    @Query("{'$or': [{'sellerId': ?0}, {'buyerId': ?0}]}")
    List<Sale> findSalesInvolvingUser(String userId);
    
    @Query("{'$or': [{'sellerId': ?0}, {'buyerId': ?0}], 'saleDate': {'$gt': ?1}}")
    List<Sale> findRecentSalesInvolvingUser(String userId, LocalDateTime since);
    
    @Query("{'sellerId': ?0, 'status': 'COMPLETED'}")
    List<Sale> findCompletedSalesBySeller(String sellerId);
    
    @Query("{'buyerId': ?0, 'status': 'COMPLETED'}")
    List<Sale> findCompletedSalesByBuyer(String buyerId);

    // Statistics methods
    long countBySellerId(String sellerId);
    
    long countByBuyerId(String buyerId);
    
    long countByStatus(Sale.SaleStatus status);
    
    long countBySellerIdAndStatus(String sellerId, Sale.SaleStatus status);
    
    long countByBuyerIdAndStatus(String buyerId, Sale.SaleStatus status);
    
    long countBySaleDateAfter(LocalDateTime dateTime);
    
    long countByPaymentMethod(Sale.PaymentMethod paymentMethod);
    
    long countByDeliveryMethod(Sale.DeliveryMethod deliveryMethod);

    // Revenue calculations
    @Query(value = "{'sellerId': ?0, 'status': 'COMPLETED'}", fields = "{'salePrice': 1}")
    List<Sale> findSalePricesBySeller(String sellerId);
    
    @Query(value = "{'sellerId': ?0, 'status': 'COMPLETED', 'saleDate': {'$gte': ?1}}", fields = "{'salePrice': 1}")
    List<Sale> findSalePricesBySellerAfter(String sellerId, LocalDateTime since);

    // User activity
    @Query("{'sellerId': ?0, 'saleDate': {'$gt': ?1}}")
    long countSalesBySellerAfter(String sellerId, LocalDateTime since);
    
    @Query("{'buyerId': ?0, 'saleDate': {'$gt': ?1}}")
    long countPurchasesByBuyerAfter(String buyerId, LocalDateTime since);

    // Popular items (by sales count)
    @Query(value = "{}", fields = "{'listingId': 1}")
    List<Sale> findAllListingIds();

    // Recent activity
    @Query("{'saleDate': {'$gte': ?0}, 'status': 'COMPLETED'}")
    Page<Sale> findRecentCompletedSales(LocalDateTime since, Pageable pageable);

    // High-value sales
    @Query("{'salePrice': {'$gte': ?0}, 'status': 'COMPLETED'}")
    Page<Sale> findHighValueSales(BigDecimal minPrice, Pageable pageable);

    // User transaction history
    @Query("{'$or': [{'sellerId': ?0}, {'buyerId': ?0}], 'status': 'COMPLETED'}")
    Page<Sale> findUserTransactionHistory(String userId, Pageable pageable);
} 