package com.jnu.marketplace.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity for tracking buying and selling activities
 * 
 * This entity records all financial transactions between users,
 * including payment details, status tracking, and dispute resolution.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @NotBlank(message = "Transaction ID is required")
    @Indexed(unique = true)
    @Field("transaction_id")
    private String transactionId;

    @NotBlank(message = "Buyer ID is required")
    @Indexed
    @Field("buyer_id")
    private String buyerId;

    @NotBlank(message = "Seller ID is required")
    @Indexed
    @Field("seller_id")
    private String sellerId;

    @NotBlank(message = "Listing ID is required")
    @Indexed
    @Field("listing_id")
    private String listingId;

    @Field("listing_title")
    private String listingTitle;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Field("amount")
    private BigDecimal amount;

    @Field("delivery_cost")
    private BigDecimal deliveryCost = BigDecimal.ZERO;

    @Field("tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @NotNull(message = "Payment method is required")
    @Field("payment_method")
    private PaymentMethod paymentMethod;

    @Field("payment_gateway")
    private PaymentGateway paymentGateway;

    @Field("payment_gateway_transaction_id")
    private String paymentGatewayTransactionId;

    @Field("currency")
    private String currency = "INR";

    @Field("transaction_status")
    private TransactionStatus status = TransactionStatus.PENDING;

    @Field("payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Field("delivery_status")
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Field("dispute_status")
    private DisputeStatus disputeStatus = DisputeStatus.NONE;

    @Field("dispute_reason")
    private String disputeReason;

    @Field("dispute_description")
    private String disputeDescription;

    @Field("dispute_created_at")
    private LocalDateTime disputeCreatedAt;

    @Field("dispute_resolved_at")
    private LocalDateTime disputeResolvedAt;

    @Field("refund_amount")
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Field("refund_reason")
    private String refundReason;

    @Field("refund_processed_at")
    private LocalDateTime refundProcessedAt;

    @Field("delivery_address")
    private String deliveryAddress;

    @Field("pickup_location")
    private String pickupLocation;

    @Field("delivery_notes")
    private String deliveryNotes;

    @Field("estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;

    @Field("actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Field("buyer_rating")
    private Integer buyerRating;

    @Field("seller_rating")
    private Integer sellerRating;

    @Field("buyer_review")
    private String buyerReview;

    @Field("seller_review")
    private String sellerReview;

    @Field("review_date")
    private LocalDateTime reviewDate;

    @Field("cancelled_at")
    private LocalDateTime cancelledAt;

    @Field("cancellation_reason")
    private String cancellationReason;

    @Field("cancelled_by")
    private String cancelledBy;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Transaction() {}

    public Transaction(String transactionId, String buyerId, String sellerId, String listingId, BigDecimal amount) {
        this.transactionId = transactionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.listingId = listingId;
        this.amount = amount;
        this.totalAmount = amount.add(deliveryCost).add(taxAmount);
    }

    // Helper methods
    public void calculateTotalAmount() {
        this.totalAmount = this.amount.add(this.deliveryCost).add(this.taxAmount);
    }

    public void markAsPaid() {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.status = TransactionStatus.CONFIRMED;
    }

    public void markAsDelivered() {
        this.deliveryStatus = DeliveryStatus.DELIVERED;
        this.actualDeliveryDate = LocalDateTime.now();
        if (this.paymentStatus == PaymentStatus.COMPLETED) {
            this.status = TransactionStatus.COMPLETED;
        }
    }

    public void markAsCancelled(String reason, String cancelledBy) {
        this.status = TransactionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        this.cancelledBy = cancelledBy;
    }

    public void initiateDispute(String reason, String description) {
        this.disputeStatus = DisputeStatus.OPEN;
        this.disputeReason = reason;
        this.disputeDescription = description;
        this.disputeCreatedAt = LocalDateTime.now();
    }

    public void resolveDispute() {
        this.disputeStatus = DisputeStatus.RESOLVED;
        this.disputeResolvedAt = LocalDateTime.now();
    }

    public void processRefund(BigDecimal refundAmount, String reason) {
        this.refundAmount = refundAmount;
        this.refundReason = reason;
        this.refundProcessedAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.REFUNDED;
    }

    public void addBuyerReview(int rating, String review) {
        this.buyerRating = rating;
        this.buyerReview = review;
        this.reviewDate = LocalDateTime.now();
    }

    public void addSellerReview(int rating, String review) {
        this.sellerRating = rating;
        this.sellerReview = review;
        this.reviewDate = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == TransactionStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return this.status == TransactionStatus.CANCELLED;
    }

    public boolean hasDispute() {
        return this.disputeStatus != DisputeStatus.NONE;
    }

    public boolean isRefunded() {
        return this.paymentStatus == PaymentStatus.REFUNDED;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public void setListingTitle(String listingTitle) {
        this.listingTitle = listingTitle;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(BigDecimal deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentGateway getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String getPaymentGatewayTransactionId() {
        return paymentGatewayTransactionId;
    }

    public void setPaymentGatewayTransactionId(String paymentGatewayTransactionId) {
        this.paymentGatewayTransactionId = paymentGatewayTransactionId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public DisputeStatus getDisputeStatus() {
        return disputeStatus;
    }

    public void setDisputeStatus(DisputeStatus disputeStatus) {
        this.disputeStatus = disputeStatus;
    }

    public String getDisputeReason() {
        return disputeReason;
    }

    public void setDisputeReason(String disputeReason) {
        this.disputeReason = disputeReason;
    }

    public String getDisputeDescription() {
        return disputeDescription;
    }

    public void setDisputeDescription(String disputeDescription) {
        this.disputeDescription = disputeDescription;
    }

    public LocalDateTime getDisputeCreatedAt() {
        return disputeCreatedAt;
    }

    public void setDisputeCreatedAt(LocalDateTime disputeCreatedAt) {
        this.disputeCreatedAt = disputeCreatedAt;
    }

    public LocalDateTime getDisputeResolvedAt() {
        return disputeResolvedAt;
    }

    public void setDisputeResolvedAt(LocalDateTime disputeResolvedAt) {
        this.disputeResolvedAt = disputeResolvedAt;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public LocalDateTime getRefundProcessedAt() {
        return refundProcessedAt;
    }

    public void setRefundProcessedAt(LocalDateTime refundProcessedAt) {
        this.refundProcessedAt = refundProcessedAt;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDateTime getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(LocalDateTime actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public Integer getBuyerRating() {
        return buyerRating;
    }

    public void setBuyerRating(Integer buyerRating) {
        this.buyerRating = buyerRating;
    }

    public Integer getSellerRating() {
        return sellerRating;
    }

    public void setSellerRating(Integer sellerRating) {
        this.sellerRating = sellerRating;
    }

    public String getBuyerReview() {
        return buyerReview;
    }

    public void setBuyerReview(String buyerReview) {
        this.buyerReview = buyerReview;
    }

    public String getSellerReview() {
        return sellerReview;
    }

    public void setSellerReview(String sellerReview) {
        this.sellerReview = sellerReview;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
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
    public enum PaymentMethod {
        CASH("Cash"),
        ONLINE("Online"),
        BANK_TRANSFER("Bank Transfer"),
        UPI("UPI"),
        CARD("Card");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentGateway {
        STRIPE("Stripe"),
        PAYPAL("PayPal"),
        RAZORPAY("Razorpay"),
        PAYTM("Paytm"),
        CASH_ON_DELIVERY("Cash on Delivery");

        private final String displayName;

        PaymentGateway(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TransactionStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        FAILED("Failed");

        private final String displayName;

        TransactionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentStatus {
        PENDING("Pending"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        REFUNDED("Refunded"),
        PARTIALLY_REFUNDED("Partially Refunded");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DeliveryStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        SHIPPED("Shipped"),
        IN_TRANSIT("In Transit"),
        OUT_FOR_DELIVERY("Out for Delivery"),
        DELIVERED("Delivered"),
        FAILED("Failed"),
        RETURNED("Returned");

        private final String displayName;

        DeliveryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DisputeStatus {
        NONE("None"),
        OPEN("Open"),
        UNDER_REVIEW("Under Review"),
        RESOLVED("Resolved"),
        CLOSED("Closed");

        private final String displayName;

        DisputeStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 