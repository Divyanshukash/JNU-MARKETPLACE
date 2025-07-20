package com.jnu.marketplace.service;

import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.model.Sale;
import com.jnu.marketplace.model.SaleRequest;
import com.jnu.marketplace.model.User;
import com.jnu.marketplace.repository.ListingRepository;
import com.jnu.marketplace.repository.SaleRequestRepository;
import com.jnu.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for handling sale request operations
 * 
 * This service manages the complete offer/request process including
 * creating offers, accepting/rejecting requests, and converting
 * accepted requests into sales.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SaleRequestService {

    private final SaleRequestRepository saleRequestRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final SaleService saleService;

    /**
     * Create a purchase request/offer
     */
    @Transactional
    public SaleRequest createSaleRequest(String listingId, String buyerId, BigDecimal offerPrice,
                                       String message, Sale.PaymentMethod paymentMethod,
                                       Sale.DeliveryMethod deliveryMethod, BigDecimal deliveryCost) {
        
        // Get the listing
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        
        // Verify the listing is still available
        if (listing.getStatus() != Listing.ListingStatus.ACTIVE) {
            throw new RuntimeException("Listing is not available for purchase");
        }
        
        // Check if buyer already has a pending request for this listing
        Optional<SaleRequest> existingRequest = saleRequestRepository.findByListingIdAndBuyerId(listingId, buyerId);
        if (existingRequest.isPresent() && existingRequest.get().getStatus() == SaleRequest.RequestStatus.PENDING) {
            throw new RuntimeException("You already have a pending request for this item");
        }
        
        // Create sale request
        SaleRequest saleRequest = new SaleRequest(listingId, buyerId, listing.getSellerId(), offerPrice);
        saleRequest.setOriginalPrice(listing.getPrice());
        saleRequest.setMessage(message);
        saleRequest.setPaymentMethod(paymentMethod);
        saleRequest.setDeliveryMethod(deliveryMethod);
        saleRequest.setDeliveryCost(deliveryCost != null ? deliveryCost : BigDecimal.ZERO);
        
        return saleRequestRepository.save(saleRequest);
    }

    /**
     * Accept a sale request (seller action)
     */
    @Transactional
    public Sale acceptSaleRequest(String requestId, String sellerResponse) {
        SaleRequest saleRequest = saleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Sale request not found"));
        
        // Verify the request can be accepted
        if (!saleRequest.canBeAccepted()) {
            throw new RuntimeException("Sale request cannot be accepted");
        }
        
        // Accept the request
        saleRequest.accept(sellerResponse);
        saleRequestRepository.save(saleRequest);
        
        // Create a sale record
        Sale sale = saleService.markItemAsSold(
            saleRequest.getListingId(),
            saleRequest.getBuyerId(),
            saleRequest.getOfferPrice(),
            saleRequest.getPaymentMethod(),
            saleRequest.getDeliveryMethod(),
            saleRequest.getDeliveryCost(),
            "Accepted offer: " + saleRequest.getMessage() + "\nSeller response: " + sellerResponse
        );
        
        return sale;
    }

    /**
     * Reject a sale request (seller action)
     */
    @Transactional
    public SaleRequest rejectSaleRequest(String requestId, String sellerResponse) {
        SaleRequest saleRequest = saleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Sale request not found"));
        
        // Verify the request can be rejected
        if (!saleRequest.canBeAccepted()) {
            throw new RuntimeException("Sale request cannot be rejected");
        }
        
        // Reject the request
        saleRequest.reject(sellerResponse);
        return saleRequestRepository.save(saleRequest);
    }

    /**
     * Cancel a sale request (buyer action)
     */
    @Transactional
    public SaleRequest cancelSaleRequest(String requestId) {
        SaleRequest saleRequest = saleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Sale request not found"));
        
        saleRequest.cancel();
        return saleRequestRepository.save(saleRequest);
    }

    /**
     * Get pending requests for a seller
     */
    public List<SaleRequest> getPendingRequestsForSeller(String sellerId) {
        return saleRequestRepository.findPendingRequestsForSeller(sellerId);
    }

    /**
     * Get pending requests for a buyer
     */
    public List<SaleRequest> getPendingRequestsForBuyer(String buyerId) {
        return saleRequestRepository.findPendingRequestsForBuyer(buyerId);
    }

    /**
     * Get all requests involving a user (both as buyer and seller)
     */
    public List<SaleRequest> getRequestsInvolvingUser(String userId) {
        return saleRequestRepository.findRequestsInvolvingUser(userId);
    }

    /**
     * Get sale request by ID
     */
    public SaleRequest getSaleRequestById(String requestId) {
        return saleRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Sale request not found"));
    }

    /**
     * Get requests for a specific listing
     */
    public List<SaleRequest> getRequestsForListing(String listingId) {
        return saleRequestRepository.findByListingId(listingId);
    }

    /**
     * Get user's request history
     */
    public List<SaleRequest> getUserRequestHistory(String userId) {
        return saleRequestRepository.findRequestsInvolvingUser(userId);
    }

    /**
     * Check if user has a pending request for a listing
     */
    public boolean hasPendingRequest(String listingId, String userId) {
        Optional<SaleRequest> request = saleRequestRepository.findByListingIdAndBuyerId(listingId, userId);
        return request.isPresent() && request.get().getStatus() == SaleRequest.RequestStatus.PENDING;
    }

    /**
     * Get request statistics for a user
     */
    public Map<String, Object> getUserRequestStats(String userId) {
        long totalRequests = saleRequestRepository.countByBuyerId(userId);
        long pendingRequests = saleRequestRepository.countByBuyerIdAndStatus(userId, SaleRequest.RequestStatus.PENDING);
        long acceptedRequests = saleRequestRepository.countByBuyerIdAndStatus(userId, SaleRequest.RequestStatus.ACCEPTED);
        long rejectedRequests = saleRequestRepository.countByBuyerIdAndStatus(userId, SaleRequest.RequestStatus.REJECTED);
        
        return Map.of(
            "totalRequests", totalRequests,
            "pendingRequests", pendingRequests,
            "acceptedRequests", acceptedRequests,
            "rejectedRequests", rejectedRequests,
            "acceptanceRate", totalRequests > 0 ? (double) acceptedRequests / totalRequests * 100 : 0
        );
    }

    /**
     * Get seller's request statistics
     */
    public Map<String, Object> getSellerRequestStats(String sellerId) {
        long totalRequests = saleRequestRepository.countBySellerId(sellerId);
        long pendingRequests = saleRequestRepository.countBySellerIdAndStatus(sellerId, SaleRequest.RequestStatus.PENDING);
        long acceptedRequests = saleRequestRepository.countBySellerIdAndStatus(sellerId, SaleRequest.RequestStatus.ACCEPTED);
        long rejectedRequests = saleRequestRepository.countBySellerIdAndStatus(sellerId, SaleRequest.RequestStatus.REJECTED);
        
        return Map.of(
            "totalRequests", totalRequests,
            "pendingRequests", pendingRequests,
            "acceptedRequests", acceptedRequests,
            "rejectedRequests", rejectedRequests,
            "acceptanceRate", totalRequests > 0 ? (double) acceptedRequests / totalRequests * 100 : 0
        );
    }

    /**
     * Clean up expired requests
     */
    @Transactional
    public void cleanupExpiredRequests() {
        List<SaleRequest> expiredRequests = saleRequestRepository.findExpiredPendingRequests(LocalDateTime.now());
        for (SaleRequest request : expiredRequests) {
            request.setStatus(SaleRequest.RequestStatus.EXPIRED);
            saleRequestRepository.save(request);
        }
    }

    /**
     * Get recent activity
     */
    public List<SaleRequest> getRecentActivity(LocalDateTime since) {
        return saleRequestRepository.findByCreatedAtAfter(since);
    }
} 