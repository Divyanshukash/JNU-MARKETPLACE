package com.jnu.marketplace.controller;

import com.jnu.marketplace.model.Sale;
import com.jnu.marketplace.model.SaleRequest;
import com.jnu.marketplace.model.User;
import com.jnu.marketplace.repository.UserRepository;
import com.jnu.marketplace.service.SaleRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sale-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleRequestController {

    private final SaleRequestService saleRequestService;
    private final UserRepository userRepository;

    /**
     * Create a purchase request/offer
     */
    @PostMapping("/create")
    public ResponseEntity<?> createSaleRequest(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User buyer = userRepository.findByEmail(userEmail).orElse(null);
            if (buyer == null) {
                return ResponseEntity.status(401).body("Buyer not found");
            }

            String listingId = (String) request.get("listingId");
            BigDecimal offerPrice = new BigDecimal(request.get("offerPrice").toString());
            String message = (String) request.get("message");
            String paymentMethodStr = (String) request.get("paymentMethod");
            String deliveryMethodStr = (String) request.get("deliveryMethod");
            BigDecimal deliveryCost = request.get("deliveryCost") != null ? 
                new BigDecimal(request.get("deliveryCost").toString()) : BigDecimal.ZERO;

            Sale.PaymentMethod paymentMethod = Sale.PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
            Sale.DeliveryMethod deliveryMethod = Sale.DeliveryMethod.valueOf(deliveryMethodStr.toUpperCase());

            SaleRequest saleRequest = saleRequestService.createSaleRequest(
                listingId, buyer.getId(), offerPrice, message, paymentMethod, deliveryMethod, deliveryCost
            );

            return ResponseEntity.ok(saleRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating sale request: " + e.getMessage());
        }
    }

    /**
     * Accept a sale request (seller action)
     */
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<?> acceptSaleRequest(@PathVariable String requestId, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User seller = userRepository.findByEmail(userEmail).orElse(null);
            if (seller == null) {
                return ResponseEntity.status(401).body("Seller not found");
            }

            String sellerResponse = request.get("sellerResponse");
            Sale sale = saleRequestService.acceptSaleRequest(requestId, sellerResponse);

            return ResponseEntity.ok(sale);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error accepting sale request: " + e.getMessage());
        }
    }

    /**
     * Reject a sale request (seller action)
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectSaleRequest(@PathVariable String requestId, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User seller = userRepository.findByEmail(userEmail).orElse(null);
            if (seller == null) {
                return ResponseEntity.status(401).body("Seller not found");
            }

            String sellerResponse = request.get("sellerResponse");
            SaleRequest saleRequest = saleRequestService.rejectSaleRequest(requestId, sellerResponse);

            return ResponseEntity.ok(saleRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error rejecting sale request: " + e.getMessage());
        }
    }

    /**
     * Cancel a sale request (buyer action)
     */
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelSaleRequest(@PathVariable String requestId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User buyer = userRepository.findByEmail(userEmail).orElse(null);
            if (buyer == null) {
                return ResponseEntity.status(401).body("Buyer not found");
            }

            SaleRequest saleRequest = saleRequestService.cancelSaleRequest(requestId);
            return ResponseEntity.ok(saleRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error cancelling sale request: " + e.getMessage());
        }
    }

    /**
     * Get pending requests for seller
     */
    @GetMapping("/seller/pending")
    public ResponseEntity<?> getPendingRequestsForSeller(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User seller = userRepository.findByEmail(userEmail).orElse(null);
            if (seller == null) {
                return ResponseEntity.status(401).body("Seller not found");
            }

            List<SaleRequest> requests = saleRequestService.getPendingRequestsForSeller(seller.getId());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching pending requests: " + e.getMessage());
        }
    }

    /**
     * Get pending requests for buyer
     */
    @GetMapping("/buyer/pending")
    public ResponseEntity<?> getPendingRequestsForBuyer(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User buyer = userRepository.findByEmail(userEmail).orElse(null);
            if (buyer == null) {
                return ResponseEntity.status(401).body("Buyer not found");
            }

            List<SaleRequest> requests = saleRequestService.getPendingRequestsForBuyer(buyer.getId());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching pending requests: " + e.getMessage());
        }
    }

    /**
     * Get user's request history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getUserRequestHistory(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            List<SaleRequest> history = saleRequestService.getUserRequestHistory(user.getId());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching request history: " + e.getMessage());
        }
    }

    /**
     * Get sale request by ID
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getSaleRequestById(@PathVariable String requestId) {
        try {
            SaleRequest saleRequest = saleRequestService.getSaleRequestById(requestId);
            return ResponseEntity.ok(saleRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching sale request: " + e.getMessage());
        }
    }

    /**
     * Get requests for a specific listing
     */
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<?> getRequestsForListing(@PathVariable String listingId) {
        try {
            List<SaleRequest> requests = saleRequestService.getRequestsForListing(listingId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching requests for listing: " + e.getMessage());
        }
    }

    /**
     * Check if user has a pending request for a listing
     */
    @GetMapping("/check-pending/{listingId}")
    public ResponseEntity<?> checkPendingRequest(@PathVariable String listingId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            boolean hasPending = saleRequestService.hasPendingRequest(listingId, user.getId());
            return ResponseEntity.ok(Map.of("hasPendingRequest", hasPending));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error checking pending request: " + e.getMessage());
        }
    }

    /**
     * Get user's request statistics
     */
    @GetMapping("/stats/buyer")
    public ResponseEntity<?> getBuyerRequestStats(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            Map<String, Object> stats = saleRequestService.getUserRequestStats(user.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching buyer stats: " + e.getMessage());
        }
    }

    /**
     * Get seller's request statistics
     */
    @GetMapping("/stats/seller")
    public ResponseEntity<?> getSellerRequestStats(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found");
            }

            Map<String, Object> stats = saleRequestService.getSellerRequestStats(user.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching seller stats: " + e.getMessage());
        }
    }

    /**
     * Get request status options
     */
    @GetMapping("/status-options")
    public ResponseEntity<?> getRequestStatusOptions() {
        SaleRequest.RequestStatus[] statuses = SaleRequest.RequestStatus.values();
        return ResponseEntity.ok(statuses);
    }
} 