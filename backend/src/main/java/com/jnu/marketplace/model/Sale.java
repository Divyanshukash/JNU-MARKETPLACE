package com.jnu.marketplace.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sale entity for tracking completed transactions
 * 
 * This entity tracks when items are sold, including buyer/seller info,
 * sale price, and transaction details.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Document(collection = "sales")
public class Sale {

    @Id
    private String id;

    @NotNull(message = "Listing ID is required")
    @Indexed
    @Field("listing_id")
    private String listingId;

    @NotNull(message = "Seller ID is required")
    @Indexed
    @Field("seller_id")
    private String sellerId;

    @NotNull(message = "Buyer ID is required")
    @Indexed
    @Field("buyer_id")
    private String buyerId;

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.01", message = "Sale price must be greater than 0")
    @Field("sale_price")
    private BigDecimal salePrice;

    @Field("original_price")
    private BigDecimal originalPrice;

    @Field("negotiated_price")
    private BigDecimal negotiatedPrice;

    @Field("sale_date")
    private LocalDateTime saleDate;

    @Field("payment_method")
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @Field("delivery_method")
    private DeliveryMethod deliveryMethod = DeliveryMethod.PICKUP;

    @Field("delivery_cost")
    private BigDecimal deliveryCost = BigDecimal.ZERO;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @Field("sale_status")
    private SaleStatus status = SaleStatus.COMPLETED;

    @Field("notes")
    private String notes;

    @Field("buyer_rating")
    private Integer buyerRating;

    @Field("seller_rating")
    private Integer sellerRating;

    @Field("buyer_feedback")
    private String buyerFeedback;

    @Field("seller_feedback")
    private String sellerFeedback;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Sale() {}

    public Sale(String listingId, String sellerId, String buyerId, BigDecimal salePrice) {
        this.listingId = listingId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.salePrice = salePrice;
        this.saleDate = LocalDateTime.now();
        this.totalAmount = salePrice.add(deliveryCost);
    }

    // Helper methods
    public void calculateTotalAmount() {
        this.totalAmount = this.salePrice.add(this.deliveryCost);
    }

    public void addBuyerRating(int rating, String feedback) {
        this.buyerRating = rating;
        this.buyerFeedback = feedback;
    }

    public void addSellerRating(int rating, String feedback) {
        this.sellerRating = rating;
        this.sellerFeedback = feedback;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getNegotiatedPrice() {
        return negotiatedPrice;
    }

    public void setNegotiatedPrice(BigDecimal negotiatedPrice) {
        this.negotiatedPrice = negotiatedPrice;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public BigDecimal getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(BigDecimal deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getBuyerFeedback() {
        return buyerFeedback;
    }

    public void setBuyerFeedback(String buyerFeedback) {
        this.buyerFeedback = buyerFeedback;
    }

    public String getSellerFeedback() {
        return sellerFeedback;
    }

    public void setSellerFeedback(String sellerFeedback) {
        this.sellerFeedback = sellerFeedback;
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
        UPI("UPI"),
        BANK_TRANSFER("Bank Transfer"),
        DIGITAL_WALLET("Digital Wallet"),
        OTHER("Other");

        private final String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DeliveryMethod {
        PICKUP("Pickup"),
        DELIVERY("Delivery"),
        MEETUP("Meetup");

        private final String displayName;

        DeliveryMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SaleStatus {
        PENDING("Pending"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String displayName;

        SaleStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 