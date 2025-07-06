package com.jnu.marketplace.repository;

import com.jnu.marketplace.model.Transaction;
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
 * Repository interface for Transaction entity
 * 
 * This repository provides data access methods for transaction management,
 * including payment tracking, dispute resolution, and financial analytics.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    // Basic find methods
    List<Transaction> findByBuyerId(String buyerId);
    
    List<Transaction> findBySellerId(String sellerId);
    
    Page<Transaction> findByBuyerId(String buyerId, Pageable pageable);
    
    Page<Transaction> findBySellerId(String sellerId, Pageable pageable);
    
    List<Transaction> findByListingId(String listingId);
    
    Page<Transaction> findByListingId(String listingId, Pageable pageable);

    // Status-based methods
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    List<Transaction> findByPaymentStatus(Transaction.PaymentStatus paymentStatus);
    
    List<Transaction> findByDeliveryStatus(Transaction.DeliveryStatus deliveryStatus);
    
    List<Transaction> findByDisputeStatus(Transaction.DisputeStatus disputeStatus);
    
    Page<Transaction> findByStatus(Transaction.TransactionStatus status, Pageable pageable);
    
    Page<Transaction> findByPaymentStatus(Transaction.PaymentStatus paymentStatus, Pageable pageable);

    // User-specific status methods
    List<Transaction> findByBuyerIdAndStatus(String buyerId, Transaction.TransactionStatus status);
    
    List<Transaction> findBySellerIdAndStatus(String sellerId, Transaction.TransactionStatus status);
    
    List<Transaction> findByBuyerIdAndPaymentStatus(String buyerId, Transaction.PaymentStatus paymentStatus);
    
    List<Transaction> findBySellerIdAndPaymentStatus(String sellerId, Transaction.PaymentStatus paymentStatus);

    // Payment method and gateway methods
    List<Transaction> findByPaymentMethod(Transaction.PaymentMethod paymentMethod);
    
    List<Transaction> findByPaymentGateway(Transaction.PaymentGateway paymentGateway);
    
    List<Transaction> findByPaymentGatewayTransactionId(String paymentGatewayTransactionId);
    
    @Query("{'paymentMethod': ?0, 'status': 'COMPLETED'}")
    Page<Transaction> findCompletedTransactionsByPaymentMethod(Transaction.PaymentMethod paymentMethod, Pageable pageable);

    // Amount-based queries
    List<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    List<Transaction> findByAmountGreaterThanEqual(BigDecimal minAmount);
    
    List<Transaction> findByAmountLessThanEqual(BigDecimal maxAmount);
    
    @Query("{'amount': {'$gte': ?0, '$lte': ?1}, 'status': 'COMPLETED'}")
    Page<Transaction> findCompletedTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    // Total amount queries
    List<Transaction> findByTotalAmountBetween(BigDecimal minTotal, BigDecimal maxTotal);
    
    List<Transaction> findByTotalAmountGreaterThanEqual(BigDecimal minTotal);
    
    List<Transaction> findByTotalAmountLessThanEqual(BigDecimal maxTotal);

    // Currency queries
    List<Transaction> findByCurrency(String currency);
    
    @Query("{'currency': ?0, 'status': 'COMPLETED'}")
    Page<Transaction> findCompletedTransactionsByCurrency(String currency, Pageable pageable);

    // Date-based methods
    List<Transaction> findByCreatedAtAfter(LocalDateTime dateTime);
    
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Transaction> findByActualDeliveryDateAfter(LocalDateTime dateTime);
    
    List<Transaction> findByActualDeliveryDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Transaction> findByCancelledAtAfter(LocalDateTime dateTime);
    
    List<Transaction> findByRefundProcessedAtAfter(LocalDateTime dateTime);

    // Recent transactions
    @Query("{'$or': [{'buyerId': ?0}, {'sellerId': ?0}], 'createdAt': {'$gt': ?1}}")
    // Find by dispute status
    List<Transaction> findByDisputeStatus(Transaction.DisputeStatus disputeStatus);

    // Find by amount range
    List<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    // Find by amount less than or equal to
    List<Transaction> findByAmountLessThanEqual(BigDecimal maxAmount);

    // Find by amount greater than or equal to
    List<Transaction> findByAmountGreaterThanEqual(BigDecimal minAmount);

    // Find by currency
    List<Transaction> findByCurrency(String currency);

    // Find by created date range
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by completed date range
    List<Transaction> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by refunded date range
    List<Transaction> findByRefundedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by dispute resolved date range
    List<Transaction> findByDisputeResolvedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by Stripe payment intent ID
    Optional<Transaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    // Find by Stripe charge ID
    Optional<Transaction> findByStripeChargeId(String stripeChargeId);

    // Find by buyer ID and seller ID
    List<Transaction> findByBuyerIdAndSellerId(String buyerId, String sellerId);

    // Find by buyer ID and seller ID with pagination
    Page<Transaction> findByBuyerIdAndSellerId(String buyerId, String sellerId, Pageable pageable);

    // Find by listing ID and payment status
    List<Transaction> findByListingIdAndPaymentStatus(String listingId, Transaction.PaymentStatus paymentStatus);

    // Find by listing ID and payment status with pagination
    Page<Transaction> findByListingIdAndPaymentStatus(String listingId, Transaction.PaymentStatus paymentStatus, Pageable pageable);

    // Find by buyer ID and payment status
    List<Transaction> findByBuyerIdAndPaymentStatus(String buyerId, Transaction.PaymentStatus paymentStatus);

    // Find by buyer ID and payment status with pagination
    Page<Transaction> findByBuyerIdAndPaymentStatus(String buyerId, Transaction.PaymentStatus paymentStatus, Pageable pageable);

    // Find by seller ID and payment status
    List<Transaction> findBySellerIdAndPaymentStatus(String sellerId, Transaction.PaymentStatus paymentStatus);

    // Find by seller ID and payment status with pagination
    Page<Transaction> findBySellerIdAndPaymentStatus(String sellerId, Transaction.PaymentStatus paymentStatus, Pageable pageable);

    // Find by transaction type and payment status
    List<Transaction> findByTransactionTypeAndPaymentStatus(Transaction.TransactionType transactionType, Transaction.PaymentStatus paymentStatus);

    // Find by payment method and payment status
    List<Transaction> findByPaymentMethodAndPaymentStatus(Transaction.PaymentMethod paymentMethod, Transaction.PaymentStatus paymentStatus);

    // Find by dispute status and payment status
    List<Transaction> findByDisputeStatusAndPaymentStatus(Transaction.DisputeStatus disputeStatus, Transaction.PaymentStatus paymentStatus);

    // Find by buyer ID and transaction type
    List<Transaction> findByBuyerIdAndTransactionType(String buyerId, Transaction.TransactionType transactionType);

    // Find by seller ID and transaction type
    List<Transaction> findBySellerIdAndTransactionType(String sellerId, Transaction.TransactionType transactionType);

    // Find by listing ID and transaction type
    List<Transaction> findByListingIdAndTransactionType(String listingId, Transaction.TransactionType transactionType);

    // Find by buyer ID and payment method
    List<Transaction> findByBuyerIdAndPaymentMethod(String buyerId, Transaction.PaymentMethod paymentMethod);

    // Find by seller ID and payment method
    List<Transaction> findBySellerIdAndPaymentMethod(String sellerId, Transaction.PaymentMethod paymentMethod);

    // Find by listing ID and payment method
    List<Transaction> findByListingIdAndPaymentMethod(String listingId, Transaction.PaymentMethod paymentMethod);

    // Find by buyer ID and dispute status
    List<Transaction> findByBuyerIdAndDisputeStatus(String buyerId, Transaction.DisputeStatus disputeStatus);

    // Find by seller ID and dispute status
    List<Transaction> findBySellerIdAndDisputeStatus(String sellerId, Transaction.DisputeStatus disputeStatus);

    // Find by listing ID and dispute status
    List<Transaction> findByListingIdAndDisputeStatus(String listingId, Transaction.DisputeStatus disputeStatus);

    // Find by buyer ID and created date range
    List<Transaction> findByBuyerIdAndCreatedAtBetween(String buyerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by seller ID and created date range
    List<Transaction> findBySellerIdAndCreatedAtBetween(String sellerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by listing ID and created date range
    List<Transaction> findByListingIdAndCreatedAtBetween(String listingId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by buyer ID and completed date range
    List<Transaction> findByBuyerIdAndCompletedAtBetween(String buyerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by seller ID and completed date range
    List<Transaction> findBySellerIdAndCompletedAtBetween(String sellerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by listing ID and completed date range
    List<Transaction> findByListingIdAndCompletedAtBetween(String listingId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by buyer ID and refunded date range
    List<Transaction> findByBuyerIdAndRefundedAtBetween(String buyerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by seller ID and refunded date range
    List<Transaction> findBySellerIdAndRefundedAtBetween(String sellerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by listing ID and refunded date range
    List<Transaction> findByListingIdAndRefundedAtBetween(String listingId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by buyer ID and dispute resolved date range
    List<Transaction> findByBuyerIdAndDisputeResolvedAtBetween(String buyerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by seller ID and dispute resolved date range
    List<Transaction> findBySellerIdAndDisputeResolvedAtBetween(String sellerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by listing ID and dispute resolved date range
    List<Transaction> findByListingIdAndDisputeResolvedAtBetween(String listingId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by buyer ID and seller ID and payment status
    List<Transaction> findByBuyerIdAndSellerIdAndPaymentStatus(String buyerId, String sellerId, Transaction.PaymentStatus paymentStatus);

    // Find by buyer ID and seller ID and transaction type
    List<Transaction> findByBuyerIdAndSellerIdAndTransactionType(String buyerId, String sellerId, Transaction.TransactionType transactionType);

    // Find by buyer ID and seller ID and payment method
    List<Transaction> findByBuyerIdAndSellerIdAndPaymentMethod(String buyerId, String sellerId, Transaction.PaymentMethod paymentMethod);

    // Find by buyer ID and seller ID and dispute status
    List<Transaction> findByBuyerIdAndSellerIdAndDisputeStatus(String buyerId, String sellerId, Transaction.DisputeStatus disputeStatus);

    // Find by buyer ID and seller ID and created date range
    List<Transaction> findByBuyerIdAndSellerIdAndCreatedAtBetween(String buyerId, String sellerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by buyer ID and seller ID and completed date range
    List<Transaction> findByBuyerIdAndSellerIdAndCompletedAtBetween(String buyerId, String sellerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by buyer ID and seller ID and refunded date range
    List<Transaction> findByBuyerIdAndSellerIdAndRefundedAtBetween(String buyerId, String sellerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by buyer ID and seller ID and dispute resolved date range
    List<Transaction> findByBuyerIdAndSellerIdAndDisputeResolvedAtBetween(String buyerId, String sellerId, LocalDateTime startDate, LocalDateTime endDate);

    // Find by transaction type with pagination
    Page<Transaction> findByTransactionType(Transaction.TransactionType transactionType, Pageable pageable);

    // Find by payment method with pagination
    Page<Transaction> findByPaymentMethod(Transaction.PaymentMethod paymentMethod, Pageable pageable);

    // Find by payment status with pagination
    Page<Transaction> findByPaymentStatus(Transaction.PaymentStatus paymentStatus, Pageable pageable);

    // Find by dispute status with pagination
    Page<Transaction> findByDisputeStatus(Transaction.DisputeStatus disputeStatus, Pageable pageable);

    // Find by amount range with pagination
    Page<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    // Find by currency with pagination
    Page<Transaction> findByCurrency(String currency, Pageable pageable);

    // Find by created date range with pagination
    Page<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by completed date range with pagination
    Page<Transaction> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by refunded date range with pagination
    Page<Transaction> findByRefundedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by dispute resolved date range with pagination
    Page<Transaction> findByDisputeResolvedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by buyer ID and transaction type with pagination
    Page<Transaction> findByBuyerIdAndTransactionType(String buyerId, Transaction.TransactionType transactionType, Pageable pageable);

    // Find by seller ID and transaction type with pagination
    Page<Transaction> findBySellerIdAndTransactionType(String sellerId, Transaction.TransactionType transactionType, Pageable pageable);

    // Find by listing ID and transaction type with pagination
    Page<Transaction> findByListingIdAndTransactionType(String listingId, Transaction.TransactionType transactionType, Pageable pageable);

    // Find by buyer ID and payment method with pagination
    Page<Transaction> findByBuyerIdAndPaymentMethod(String buyerId, Transaction.PaymentMethod paymentMethod, Pageable pageable);

    // Find by seller ID and payment method with pagination
    Page<Transaction> findBySellerIdAndPaymentMethod(String sellerId, Transaction.PaymentMethod paymentMethod, Pageable pageable);

    // Find by listing ID and payment method with pagination
    Page<Transaction> findByListingIdAndPaymentMethod(String listingId, Transaction.PaymentMethod paymentMethod, Pageable pageable);

    // Find by buyer ID and dispute status with pagination
    Page<Transaction> findByBuyerIdAndDisputeStatus(String buyerId, Transaction.DisputeStatus disputeStatus, Pageable pageable);

    // Find by seller ID and dispute status with pagination
    Page<Transaction> findBySellerIdAndDisputeStatus(String sellerId, Transaction.DisputeStatus disputeStatus, Pageable pageable);

    // Find by listing ID and dispute status with pagination
    Page<Transaction> findByListingIdAndDisputeStatus(String listingId, Transaction.DisputeStatus disputeStatus, Pageable pageable);

    // Find by buyer ID and created date range with pagination
    Page<Transaction> findByBuyerIdAndCreatedAtBetween(String buyerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by seller ID and created date range with pagination
    Page<Transaction> findBySellerIdAndCreatedAtBetween(String sellerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by listing ID and created date range with pagination
    Page<Transaction> findByListingIdAndCreatedAtBetween(String listingId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by buyer ID and completed date range with pagination
    Page<Transaction> findByBuyerIdAndCompletedAtBetween(String buyerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by seller ID and completed date range with pagination
    Page<Transaction> findBySellerIdAndCompletedAtBetween(String sellerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by listing ID and completed date range with pagination
    Page<Transaction> findByListingIdAndCompletedAtBetween(String listingId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by buyer ID and refunded date range with pagination
    Page<Transaction> findByBuyerIdAndRefundedAtBetween(String buyerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by seller ID and refunded date range with pagination
    Page<Transaction> findBySellerIdAndRefundedAtBetween(String sellerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by listing ID and refunded date range with pagination
    Page<Transaction> findByListingIdAndRefundedAtBetween(String listingId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by buyer ID and dispute resolved date range with pagination
    Page<Transaction> findByBuyerIdAndDisputeResolvedAtBetween(String buyerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by seller ID and dispute resolved date range with pagination
    Page<Transaction> findBySellerIdAndDisputeResolvedAtBetween(String sellerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by listing ID and dispute resolved date range with pagination
    Page<Transaction> findByListingIdAndDisputeResolvedAtBetween(String listingId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Count by transaction ID
    long countByTransactionId(String transactionId);

    // Count by listing ID
    long countByListingId(String listingId);

    // Count by buyer ID
    long countByBuyerId(String buyerId);

    // Count by seller ID
    long countBySellerId(String sellerId);

    // Count by transaction type
    long countByTransactionType(Transaction.TransactionType transactionType);

    // Count by payment method
    long countByPaymentMethod(Transaction.PaymentMethod paymentMethod);

    // Count by payment status
    long countByPaymentStatus(Transaction.PaymentStatus paymentStatus);

    // Count by dispute status
    long countByDisputeStatus(Transaction.DisputeStatus disputeStatus);

    // Count by currency
    long countByCurrency(String currency);

    // Count by buyer ID and seller ID
    long countByBuyerIdAndSellerId(String buyerId, String sellerId);

    // Count by listing ID and payment status
    long countByListingIdAndPaymentStatus(String listingId, Transaction.PaymentStatus paymentStatus);

    // Count by buyer ID and payment status
    long countByBuyerIdAndPaymentStatus(String buyerId, Transaction.PaymentStatus paymentStatus);

    // Count by seller ID and payment status
    long countBySellerIdAndPaymentStatus(String sellerId, Transaction.PaymentStatus paymentStatus);

    // Count by transaction type and payment status
    long countByTransactionTypeAndPaymentStatus(Transaction.TransactionType transactionType, Transaction.PaymentStatus paymentStatus);

    // Count by payment method and payment status
    long countByPaymentMethodAndPaymentStatus(Transaction.PaymentMethod paymentMethod, Transaction.PaymentStatus paymentStatus);

    // Count by dispute status and payment status
    long countByDisputeStatusAndPaymentStatus(Transaction.DisputeStatus disputeStatus, Transaction.PaymentStatus paymentStatus);

    // Count by buyer ID and transaction type
    long countByBuyerIdAndTransactionType(String buyerId, Transaction.TransactionType transactionType);

    // Count by seller ID and transaction type
    long countBySellerIdAndTransactionType(String sellerId, Transaction.TransactionType transactionType);

    // Count by listing ID and transaction type
    long countByListingIdAndTransactionType(String listingId, Transaction.TransactionType transactionType);

    // Count by buyer ID and payment method
    long countByBuyerIdAndPaymentMethod(String buyerId, Transaction.PaymentMethod paymentMethod);

    // Count by seller ID and payment method
    long countBySellerIdAndPaymentMethod(String sellerId, Transaction.PaymentMethod paymentMethod);

    // Count by listing ID and payment method
    long countByListingIdAndPaymentMethod(String listingId, Transaction.PaymentMethod paymentMethod);

    // Count by buyer ID and dispute status
    long countByBuyerIdAndDisputeStatus(String buyerId, Transaction.DisputeStatus disputeStatus);

    // Count by seller ID and dispute status
    long countBySellerIdAndDisputeStatus(String sellerId, Transaction.DisputeStatus disputeStatus);

    // Count by listing ID and dispute status
    long countByListingIdAndDisputeStatus(String listingId, Transaction.DisputeStatus disputeStatus);

    // Count by buyer ID and seller ID and payment status
    long countByBuyerIdAndSellerIdAndPaymentStatus(String buyerId, String sellerId, Transaction.PaymentStatus paymentStatus);

    // Count by buyer ID and seller ID and transaction type
    long countByBuyerIdAndSellerIdAndTransactionType(String buyerId, String sellerId, Transaction.TransactionType transactionType);

    // Count by buyer ID and seller ID and payment method
    long countByBuyerIdAndSellerIdAndPaymentMethod(String buyerId, String sellerId, Transaction.PaymentMethod paymentMethod);

    // Count by buyer ID and seller ID and dispute status
    long countByBuyerIdAndSellerIdAndDisputeStatus(String buyerId, String sellerId, Transaction.DisputeStatus disputeStatus);

    // Delete by transaction ID
    void deleteByTransactionId(String transactionId);

    // Delete by listing ID
    void deleteByListingId(String listingId);

    // Delete by buyer ID
    void deleteByBuyerId(String buyerId);

    // Delete by seller ID
    void deleteBySellerId(String sellerId);

    // Delete by transaction type
    void deleteByTransactionType(Transaction.TransactionType transactionType);

    // Delete by payment method
    void deleteByPaymentMethod(Transaction.PaymentMethod paymentMethod);

    // Delete by payment status
    void deleteByPaymentStatus(Transaction.PaymentStatus paymentStatus);

    // Delete by dispute status
    void deleteByDisputeStatus(Transaction.DisputeStatus disputeStatus);

    // Delete by currency
    void deleteByCurrency(String currency);

    // Delete by buyer ID and seller ID
    void deleteByBuyerIdAndSellerId(String buyerId, String sellerId);

    // Delete by listing ID and payment status
    void deleteByListingIdAndPaymentStatus(String listingId, Transaction.PaymentStatus paymentStatus);

    // Delete by buyer ID and payment status
    void deleteByBuyerIdAndPaymentStatus(String buyerId, Transaction.PaymentStatus paymentStatus);

    // Delete by seller ID and payment status
    void deleteBySellerIdAndPaymentStatus(String sellerId, Transaction.PaymentStatus paymentStatus);

    // Delete by transaction type and payment status
    void deleteByTransactionTypeAndPaymentStatus(Transaction.TransactionType transactionType, Transaction.PaymentStatus paymentStatus);

    // Delete by payment method and payment status
    void deleteByPaymentMethodAndPaymentStatus(Transaction.PaymentMethod paymentMethod, Transaction.PaymentStatus paymentStatus);

    // Delete by dispute status and payment status
    void deleteByDisputeStatusAndPaymentStatus(Transaction.DisputeStatus disputeStatus, Transaction.PaymentStatus paymentStatus);

    // Delete by buyer ID and transaction type
    void deleteByBuyerIdAndTransactionType(String buyerId, Transaction.TransactionType transactionType);

    // Delete by seller ID and transaction type
    void deleteBySellerIdAndTransactionType(String sellerId, Transaction.TransactionType transactionType);

    // Delete by listing ID and transaction type
    void deleteByListingIdAndTransactionType(String listingId, Transaction.TransactionType transactionType);

    // Delete by buyer ID and payment method
    void deleteByBuyerIdAndPaymentMethod(String buyerId, Transaction.PaymentMethod paymentMethod);

    // Delete by seller ID and payment method
    void deleteBySellerIdAndPaymentMethod(String sellerId, Transaction.PaymentMethod paymentMethod);

    // Delete by listing ID and payment method
    void deleteByListingIdAndPaymentMethod(String listingId, Transaction.PaymentMethod paymentMethod);

    // Delete by buyer ID and dispute status
    void deleteByBuyerIdAndDisputeStatus(String buyerId, Transaction.DisputeStatus disputeStatus);

    // Delete by seller ID and dispute status
    void deleteBySellerIdAndDisputeStatus(String sellerId, Transaction.DisputeStatus disputeStatus);

    // Delete by listing ID and dispute status
    void deleteByListingIdAndDisputeStatus(String listingId, Transaction.DisputeStatus disputeStatus);

    // Delete by buyer ID and seller ID and payment status
    void deleteByBuyerIdAndSellerIdAndPaymentStatus(String buyerId, String sellerId, Transaction.PaymentStatus paymentStatus);

    // Delete by buyer ID and seller ID and transaction type
    void deleteByBuyerIdAndSellerIdAndTransactionType(String buyerId, String sellerId, Transaction.TransactionType transactionType);

    // Delete by buyer ID and seller ID and payment method
    void deleteByBuyerIdAndSellerIdAndPaymentMethod(String buyerId, String sellerId, Transaction.PaymentMethod paymentMethod);

    // Delete by buyer ID and seller ID and dispute status
    void deleteByBuyerIdAndSellerIdAndDisputeStatus(String buyerId, String sellerId, Transaction.DisputeStatus disputeStatus);

    // Find all transactions for a user (as buyer or seller)
    @Query("{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}")
    Page<Transaction> findTransactionsForUser(String userId, Pageable pageable);

    // Find all completed transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentStatus': 'COMPLETED'}]}")
    Page<Transaction> findCompletedTransactionsForUser(String userId, Pageable pageable);

    // Find all pending transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentStatus': 'PENDING'}]}")
    Page<Transaction> findPendingTransactionsForUser(String userId, Pageable pageable);

    // Find all failed transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentStatus': 'FAILED'}]}")
    Page<Transaction> findFailedTransactionsForUser(String userId, Pageable pageable);

    // Find all refunded transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'$or': [{'paymentStatus': 'REFUNDED'}, {'paymentStatus': 'PARTIALLY_REFUNDED'}]}]}")
    Page<Transaction> findRefundedTransactionsForUser(String userId, Pageable pageable);

    // Find all disputed transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'disputeStatus': {$ne: 'NONE'}}]}")
    Page<Transaction> findDisputedTransactionsForUser(String userId, Pageable pageable);

    // Find all transactions for a listing
    @Query("{'listingId': ?0}")
    Page<Transaction> findTransactionsForListing(String listingId, Pageable pageable);

    // Find all completed transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentStatus': 'COMPLETED'}]}")
    Page<Transaction> findCompletedTransactionsForListing(String listingId, Pageable pageable);

    // Find all pending transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentStatus': 'PENDING'}]}")
    Page<Transaction> findPendingTransactionsForListing(String listingId, Pageable pageable);

    // Find all failed transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentStatus': 'FAILED'}]}")
    Page<Transaction> findFailedTransactionsForListing(String listingId, Pageable pageable);

    // Find all refunded transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'$or': [{'paymentStatus': 'REFUNDED'}, {'paymentStatus': 'PARTIALLY_REFUNDED'}]}]}")
    Page<Transaction> findRefundedTransactionsForListing(String listingId, Pageable pageable);

    // Find all disputed transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'disputeStatus': {$ne: 'NONE'}}]}")
    Page<Transaction> findDisputedTransactionsForListing(String listingId, Pageable pageable);

    // Find all transactions of a specific type for a user
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'transactionType': ?1}]}")
    Page<Transaction> findTransactionsByTypeForUser(String userId, Transaction.TransactionType transactionType, Pageable pageable);

    // Find all transactions of a specific type for a listing
    @Query("{'$and': [{'listingId': ?0}, {'transactionType': ?1}]}")
    Page<Transaction> findTransactionsByTypeForListing(String listingId, Transaction.TransactionType transactionType, Pageable pageable);

    // Find all transactions with a specific payment method for a user
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentMethod': ?1}]}")
    Page<Transaction> findTransactionsByPaymentMethodForUser(String userId, Transaction.PaymentMethod paymentMethod, Pageable pageable);

    // Find all transactions with a specific payment method for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentMethod': ?1}]}")
    Page<Transaction> findTransactionsByPaymentMethodForListing(String listingId, Transaction.PaymentMethod paymentMethod, Pageable pageable);

    // Count all transactions for a user (as buyer or seller)
    @Query("{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}")
    long countTransactionsForUser(String userId);

    // Count all completed transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentStatus': 'COMPLETED'}]}")
    long countCompletedTransactionsForUser(String userId);

    // Count all pending transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentStatus': 'PENDING'}]}")
    long countPendingTransactionsForUser(String userId);

    // Count all failed transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentStatus': 'FAILED'}]}")
    long countFailedTransactionsForUser(String userId);

    // Count all refunded transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'$or': [{'paymentStatus': 'REFUNDED'}, {'paymentStatus': 'PARTIALLY_REFUNDED'}]}]}")
    long countRefundedTransactionsForUser(String userId);

    // Count all disputed transactions for a user (as buyer or seller)
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'disputeStatus': {$ne: 'NONE'}}]}")
    long countDisputedTransactionsForUser(String userId);

    // Count all transactions for a listing
    @Query("{'listingId': ?0}")
    long countTransactionsForListing(String listingId);

    // Count all completed transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentStatus': 'COMPLETED'}]}")
    long countCompletedTransactionsForListing(String listingId);

    // Count all pending transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentStatus': 'PENDING'}]}")
    long countPendingTransactionsForListing(String listingId);

    // Count all failed transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentStatus': 'FAILED'}]}")
    long countFailedTransactionsForListing(String listingId);

    // Count all refunded transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'$or': [{'paymentStatus': 'REFUNDED'}, {'paymentStatus': 'PARTIALLY_REFUNDED'}]}]}")
    long countRefundedTransactionsForListing(String listingId);

    // Count all disputed transactions for a listing
    @Query("{'$and': [{'listingId': ?0}, {'disputeStatus': {$ne: 'NONE'}}]}")
    long countDisputedTransactionsForListing(String listingId);

    // Count all transactions of a specific type for a user
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'transactionType': ?1}]}")
    long countTransactionsByTypeForUser(String userId, Transaction.TransactionType transactionType);

    // Count all transactions of a specific type for a listing
    @Query("{'$and': [{'listingId': ?0}, {'transactionType': ?1}]}")
    long countTransactionsByTypeForListing(String listingId, Transaction.TransactionType transactionType);

    // Count all transactions with a specific payment method for a user
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentMethod': ?1}]}")
    long countTransactionsByPaymentMethodForUser(String userId, Transaction.PaymentMethod paymentMethod);

    // Count all transactions with a specific payment method for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentMethod': ?1}]}")
    long countTransactionsByPaymentMethodForListing(String listingId, Transaction.PaymentMethod paymentMethod);

    // Delete all transactions for a user (as buyer or seller)
    @Query("{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}")
    void deleteTransactionsForUser(String userId);

    // Delete all transactions for a listing
    @Query("{'listingId': ?0}")
    void deleteTransactionsForListing(String listingId);

    // Delete all transactions of a specific type for a user
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'transactionType': ?1}]}")
    void deleteTransactionsByTypeForUser(String userId, Transaction.TransactionType transactionType);

    // Delete all transactions of a specific type for a listing
    @Query("{'$and': [{'listingId': ?0}, {'transactionType': ?1}]}")
    void deleteTransactionsByTypeForListing(String listingId, Transaction.TransactionType transactionType);

    // Delete all transactions with a specific payment method for a user
    @Query("{'$and': [{'$or': [{'buyerId': ?0}, {'sellerId': ?0}]}, {'paymentMethod': ?1}]}")
    void deleteTransactionsByPaymentMethodForUser(String userId, Transaction.PaymentMethod paymentMethod);

    // Delete all transactions with a specific payment method for a listing
    @Query("{'$and': [{'listingId': ?0}, {'paymentMethod': ?1}]}")
    void deleteTransactionsByPaymentMethodForListing(String listingId, Transaction.PaymentMethod paymentMethod);
} 