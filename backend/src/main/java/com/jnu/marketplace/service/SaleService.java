package com.jnu.marketplace.service;

import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.model.Sale;
import com.jnu.marketplace.model.User;
import com.jnu.marketplace.repository.ListingRepository;
import com.jnu.marketplace.repository.SaleRepository;
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
 * Service class for handling sale operations
 * 
 * This service manages the complete sale process including
 * marking items as sold, creating sale records, and updating
 * user statistics.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    /**
     * Mark an item as sold and create a sale record
     */
    @Transactional
    public Sale markItemAsSold(String listingId, String buyerId, BigDecimal salePrice, 
                              Sale.PaymentMethod paymentMethod, Sale.DeliveryMethod deliveryMethod,
                              BigDecimal deliveryCost, String notes) {
        
        // Get the listing
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        
        // Verify the listing is still available
        if (listing.getStatus() != Listing.ListingStatus.ACTIVE) {
            throw new RuntimeException("Listing is not available for sale");
        }
        
        // Get buyer and seller
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        User seller = userRepository.findById(listing.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        // Create sale record
        Sale sale = new Sale(listingId, listing.getSellerId(), buyerId, salePrice);
        sale.setOriginalPrice(listing.getPrice());
        sale.setPaymentMethod(paymentMethod);
        sale.setDeliveryMethod(deliveryMethod);
        sale.setDeliveryCost(deliveryCost != null ? deliveryCost : BigDecimal.ZERO);
        sale.setNotes(notes);
        sale.calculateTotalAmount();
        
        // Save the sale
        Sale savedSale = saleRepository.save(sale);
        
        // Update listing status to SOLD
        listing.setStatus(Listing.ListingStatus.SOLD);
        listing.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(listing);
        
        // Update user statistics
        updateUserStatistics(seller.getId(), buyer.getId(), salePrice);
        
        return savedSale;
    }

    /**
     * Get sale history for a user (both as buyer and seller)
     */
    public List<Sale> getUserSaleHistory(String userId) {
        return saleRepository.findSalesInvolvingUser(userId);
    }

    /**
     * Get sales where user was the seller
     */
    public List<Sale> getUserSales(String userId) {
        return saleRepository.findBySellerId(userId);
    }

    /**
     * Get purchases where user was the buyer
     */
    public List<Sale> getUserPurchases(String userId) {
        return saleRepository.findByBuyerId(userId);
    }

    /**
     * Get completed sales for a seller
     */
    public List<Sale> getCompletedSalesBySeller(String sellerId) {
        return saleRepository.findCompletedSalesBySeller(sellerId);
    }

    /**
     * Get completed purchases for a buyer
     */
    public List<Sale> getCompletedPurchasesByBuyer(String buyerId) {
        return saleRepository.findCompletedSalesByBuyer(buyerId);
    }

    /**
     * Get sale by listing ID
     */
    public Optional<Sale> getSaleByListingId(String listingId) {
        return saleRepository.findByListingId(listingId);
    }

    /**
     * Add rating and feedback for a sale
     */
    @Transactional
    public Sale addRating(String saleId, String userId, int rating, String feedback, boolean isBuyerRating) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        
        if (isBuyerRating) {
            sale.addBuyerRating(rating, feedback);
        } else {
            sale.addSellerRating(rating, feedback);
        }
        
        return saleRepository.save(sale);
    }

    /**
     * Get user revenue statistics
     */
    public Map<String, Object> getUserRevenueStats(String userId) {
        List<Sale> completedSales = saleRepository.findCompletedSalesBySeller(userId);
        
        BigDecimal totalRevenue = completedSales.stream()
                .map(Sale::getSalePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalDeliveryRevenue = completedSales.stream()
                .map(Sale::getDeliveryCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
            "totalSales", completedSales.size(),
            "totalRevenue", totalRevenue,
            "totalDeliveryRevenue", totalDeliveryRevenue,
            "averageSalePrice", completedSales.isEmpty() ? BigDecimal.ZERO : 
                totalRevenue.divide(BigDecimal.valueOf(completedSales.size()), 2, BigDecimal.ROUND_HALF_UP)
        );
    }

    /**
     * Get recent sales activity
     */
    public List<Sale> getRecentSales(LocalDateTime since) {
        return saleRepository.findBySaleDateAfter(since);
    }

    /**
     * Check if a listing has been sold
     */
    public boolean isListingSold(String listingId) {
        return saleRepository.findByListingId(listingId).isPresent();
    }

    /**
     * Cancel a sale (for refunds or disputes)
     */
    @Transactional
    public Sale cancelSale(String saleId, String reason) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        
        sale.setStatus(Sale.SaleStatus.CANCELLED);
        sale.setNotes(sale.getNotes() + "\nCancelled: " + reason);
        
        // Revert listing status to ACTIVE
        Listing listing = listingRepository.findById(sale.getListingId())
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listingRepository.save(listing);
        
        return saleRepository.save(sale);
    }

    /**
     * Update user statistics after a sale
     */
    private void updateUserStatistics(String sellerId, String buyerId, BigDecimal salePrice) {
        // Update seller statistics
        User seller = userRepository.findById(sellerId).orElse(null);
        if (seller != null) {
            // You can add more statistics here like total sales, revenue, etc.
            seller.setUpdatedAt(LocalDateTime.now());
            userRepository.save(seller);
        }
        
        // Update buyer statistics
        User buyer = userRepository.findById(buyerId).orElse(null);
        if (buyer != null) {
            // You can add more statistics here like total purchases, etc.
            buyer.setUpdatedAt(LocalDateTime.now());
            userRepository.save(buyer);
        }
    }

    /**
     * Get sales analytics for admin
     */
    public Map<String, Object> getSalesAnalytics() {
        long totalSales = saleRepository.countByStatus(Sale.SaleStatus.COMPLETED);
        long totalRevenue = saleRepository.findCompletedSalesBySeller("").stream()
                .map(Sale::getSalePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .longValue();
        
        return Map.of(
            "totalSales", totalSales,
            "totalRevenue", totalRevenue,
            "averageSalePrice", totalSales > 0 ? totalRevenue / totalSales : 0
        );
    }
} 