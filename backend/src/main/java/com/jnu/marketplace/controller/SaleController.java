package com.jnu.marketplace.controller;

import com.jnu.marketplace.model.Sale;
import com.jnu.marketplace.model.User;
import com.jnu.marketplace.repository.UserRepository;
import com.jnu.marketplace.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleService saleService;
    private final UserRepository userRepository;

    /**
     * Mark an item as sold
     */
    @PostMapping("/mark-sold")
    public ResponseEntity<?> markItemAsSold(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User buyer = userRepository.findByEmail(userEmail).orElse(null);
            if (buyer == null) {
                return ResponseEntity.status(401).body("Buyer not found");
            }

            String listingId = (String) request.get("listingId");
            BigDecimal salePrice = new BigDecimal(request.get("salePrice").toString());
            String paymentMethodStr = (String) request.get("paymentMethod");
            String deliveryMethodStr = (String) request.get("deliveryMethod");
            BigDecimal deliveryCost = request.get("deliveryCost") != null ? 
                new BigDecimal(request.get("deliveryCost").toString()) : BigDecimal.ZERO;
            String notes = (String) request.get("notes");

            Sale.PaymentMethod paymentMethod = Sale.PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
            Sale.DeliveryMethod deliveryMethod = Sale.DeliveryMethod.valueOf(deliveryMethodStr.toUpperCase());

            Sale sale = saleService.markItemAsSold(listingId, buyer.getId(), salePrice, 
                paymentMethod, deliveryMethod, deliveryCost, notes);

            return ResponseEntity.ok(sale);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking item as sold: " + e.getMessage());
        }
    }

    /**
     * Get user's sale history (both as buyer and seller)
     */
    @GetMapping("/history")
    public ResponseEntity<?> getUserSaleHistory(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            List<Sale> history = saleService.getUserSaleHistory(user.getId());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching sale history: " + e.getMessage());
        }
    }

    /**
     * Get user's sales (as seller)
     */
    @GetMapping("/my-sales")
    public ResponseEntity<?> getUserSales(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            List<Sale> sales = saleService.getUserSales(user.getId());
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching sales: " + e.getMessage());
        }
    }

    /**
     * Get user's purchases (as buyer)
     */
    @GetMapping("/my-purchases")
    public ResponseEntity<?> getUserPurchases(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            List<Sale> purchases = saleService.getUserPurchases(user.getId());
            return ResponseEntity.ok(purchases);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching purchases: " + e.getMessage());
        }
    }

    /**
     * Get sale by listing ID
     */
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getSaleByListingId(@PathVariable String listingId) {
        try {
            var sale = saleService.getSaleByListingId(listingId);
            return sale.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching sale: " + e.getMessage());
        }
    }

    /**
     * Add rating and feedback for a sale
     */
    @PostMapping("/{saleId}/rate")
    public ResponseEntity<?> addRating(@PathVariable String saleId, @RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            int rating = (Integer) request.get("rating");
            String feedback = (String) request.get("feedback");
            boolean isBuyerRating = (Boolean) request.get("isBuyerRating");

            Sale sale = saleService.addRating(saleId, user.getId(), rating, feedback, isBuyerRating);
            return ResponseEntity.ok(sale);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding rating: " + e.getMessage());
        }
    }

    /**
     * Get user's revenue statistics
     */
    @GetMapping("/revenue-stats")
    public ResponseEntity<?> getUserRevenueStats(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            Map<String, Object> stats = saleService.getUserRevenueStats(user.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching revenue stats: " + e.getMessage());
        }
    }

    /**
     * Check if a listing has been sold
     */
    @GetMapping("/check-sold/{listingId}")
    public ResponseEntity<?> checkIfSold(@PathVariable String listingId) {
        try {
            boolean isSold = saleService.isListingSold(listingId);
            return ResponseEntity.ok(Map.of("isSold", isSold));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error checking sale status: " + e.getMessage());
        }
    }

    /**
     * Cancel a sale (admin or seller only)
     */
    @PostMapping("/{saleId}/cancel")
    public ResponseEntity<?> cancelSale(@PathVariable String saleId, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            String reason = request.get("reason");
            Sale sale = saleService.cancelSale(saleId, reason);
            return ResponseEntity.ok(sale);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error cancelling sale: " + e.getMessage());
        }
    }

    /**
     * Get payment methods
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getPaymentMethods() {
        Sale.PaymentMethod[] methods = Sale.PaymentMethod.values();
        return ResponseEntity.ok(methods);
    }

    /**
     * Get delivery methods
     */
    @GetMapping("/delivery-methods")
    public ResponseEntity<?> getDeliveryMethods() {
        Sale.DeliveryMethod[] methods = Sale.DeliveryMethod.values();
        return ResponseEntity.ok(methods);
    }
} 