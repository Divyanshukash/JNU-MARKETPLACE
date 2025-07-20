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
 * SaleRequest entity for tracking purchase requests and offers
 * 
 * This entity tracks when buyers make offers or requests to purchase items,
 * allowing sellers to accept/reject and complete the sale process.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Document(collection = "sale_requests")
public class SaleRequest {

    @Id
    private String id;

    @NotNull(message = "Listing ID is required")
    @Indexed
    @Field("listing_id")
    private String listingId;

    @NotNull(message = "Buyer ID is required")
    @Indexed
    @Field("buyer_id")
    private String buyerId;

    @NotNull(message = "Seller ID is required")
    @Indexed
    @Field("seller_id")
    private String sellerId;

    @NotNull(message = "Offer price is required")
    @DecimalMin(value = "0.01", message = "Offer price must be greater than 0")
    @Field("offer_price")
    private BigDecimal offerPrice;

    @Field("original_price")
    private BigDecimal originalPrice;

    @Field("message")
    private String message;

    @Field("request_status")
    private RequestStatus status = RequestStatus.PENDING;

    @Field("payment_method")
    private Sale.PaymentMethod paymentMethod = Sale.PaymentMethod.CASH;

    @Field("delivery_method")
    private Sale.DeliveryMethod deliveryMethod = Sale.DeliveryMethod.PICKUP;

    @Field("delivery_cost")
    private BigDecimal deliveryCost = BigDecimal.ZERO;

    @Field("seller_response")
    private String sellerResponse;

    @Field("accepted_at")
    private LocalDateTime acceptedAt;

    @Field("rejected_at")
    private LocalDateTime rejectedAt;

    @Field("expires_at")
    private LocalDateTime expiresAt;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public SaleRequest() {}

    public SaleRequest(String listingId, String buyerId, String sellerId, BigDecimal offerPrice) {
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.offerPrice = offerPrice;
        this.expiresAt = LocalDateTime.now().plusDays(7); // 7 days expiry
    }

    // Helper methods
    public void accept(String sellerResponse) {
        this.status = RequestStatus.ACCEPTED;
        this.sellerResponse = sellerResponse;
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject(String sellerResponse) {
        this.status = RequestStatus.REJECTED;
        this.sellerResponse = sellerResponse;
        this.rejectedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = RequestStatus.CANCELLED;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canBeAccepted() {
        return status == RequestStatus.PENDING && !isExpired();
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

    public BigDecimal getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(BigDecimal offerPrice) {
        this.offerPrice = offerPrice;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Sale.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Sale.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Sale.DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(Sale.DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public BigDecimal getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(BigDecimal deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public String getSellerResponse() {
        return sellerResponse;
    }

    public void setSellerResponse(String sellerResponse) {
        this.sellerResponse = sellerResponse;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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
    public enum RequestStatus {
        PENDING("Pending"),
        ACCEPTED("Accepted"),
        REJECTED("Rejected"),
        CANCELLED("Cancelled"),
        EXPIRED("Expired");

        private final String displayName;

        RequestStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 