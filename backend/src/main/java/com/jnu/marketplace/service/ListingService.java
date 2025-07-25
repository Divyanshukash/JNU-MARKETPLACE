package com.jnu.marketplace.service;

import com.jnu.marketplace.dto.ListingRequest;
import com.jnu.marketplace.dto.SearchRequest;
import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.model.Listing.ListingStatus;
import com.jnu.marketplace.model.User;
import com.jnu.marketplace.repository.ListingRepository;
import com.jnu.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public Listing createListing(ListingRequest request) {
        if (request.getDonation()) {
            if (request.getPrice().compareTo(java.math.BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("Donation listings must have a price of 0.");
            }
        } else {
            if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0 for non-donation listings.");
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = new Listing();
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(Listing.Category.valueOf(request.getCategory().toUpperCase()));
        listing.setCondition(request.getCondition());
        listing.setPrice(request.getPrice());
        listing.setNegotiable(request.isNegotiable());
        listing.setImages(request.getImages());
        listing.setSellerId(user.getId());
        listing.setSellerName(user.getFirstName() + " " + user.getLastName());
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setDonation(request.getDonation());
        listing.setLifeOfItem(request.getLifeOfItem());

        return listingRepository.save(listing);
    }

    public Listing updateListing(String id, ListingRequest request) {
        if (request.getDonation()) {
            if (request.getPrice().compareTo(java.math.BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("Donation listings must have a price of 0.");
            }
        } else {
            if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price must be greater than 0 for non-donation listings.");
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Check if user owns the listing
        if (!listing.getSellerId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own listings");
        }

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(Listing.Category.valueOf(request.getCategory().toUpperCase()));
        listing.setCondition(request.getCondition());
        listing.setPrice(request.getPrice());
        listing.setNegotiable(request.isNegotiable());
        listing.setImages(request.getImages());
        listing.setDonation(request.getDonation());
        listing.setLifeOfItem(request.getLifeOfItem());

        return listingRepository.save(listing);
    }

    public void deleteListing(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Check if user owns the listing
        if (!listing.getSellerId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own listings");
        }

        listingRepository.delete(listing);
    }

    public Listing getListingById(String id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
    }

    public Page<Listing> getAllListings(Pageable pageable) {
        return listingRepository.findAll(pageable);
    }

    public Page<Listing> getActiveListings(Pageable pageable) {
        return listingRepository.findByStatus(ListingStatus.ACTIVE, pageable);
    }

    public Page<Listing> searchListings(SearchRequest request, Pageable pageable) {
        // If any filter is set, build a dynamic query
        boolean hasCategory = request.getCategory() != null && !request.getCategory().isEmpty();
        boolean hasCondition = request.getCondition() != null;
        boolean hasMinPrice = request.getMinPrice() != null;
        boolean hasMaxPrice = request.getMaxPrice() != null;
        boolean hasKeyword = request.getKeyword() != null && !request.getKeyword().trim().isEmpty();

        // Handle keyword search first
        if (hasKeyword) {
            return listingRepository.searchActiveListingsByText(request.getKeyword(), pageable);
        }

        // If no filters, return all active listings
        if (!hasCategory && !hasCondition && !hasMinPrice && !hasMaxPrice) {
            return listingRepository.findByStatus(ListingStatus.ACTIVE, pageable);
        }

        // Get all active listings and filter in memory for simplicity
        List<Listing> allActive = listingRepository.findByStatus(ListingStatus.ACTIVE, Pageable.unpaged()).getContent();
        List<Listing> filtered = allActive.stream()
            .filter(listing -> {
                // Category filter
                if (hasCategory) {
                    try {
                        Listing.Category category = Listing.Category.valueOf(request.getCategory());
                        if (listing.getCategory() != category) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid category: " + request.getCategory());
                        return false; // Invalid category
                    }
                }
                
                // Condition filter
                if (hasCondition) {
                    try {
                        Listing.Condition condition = Listing.Condition.valueOf(request.getCondition().toString());
                        if (listing.getCondition() != condition) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid condition: " + request.getCondition());
                        return false; // Invalid condition
                    }
                }
                
                // Price filters
                if (hasMinPrice && listing.getPrice().compareTo(request.getMinPrice()) < 0) {
                    return false;
                }
                
                if (hasMaxPrice && listing.getPrice().compareTo(request.getMaxPrice()) > 0) {
                    return false;
                }
                
                return true;
            })
            .toList();

        System.out.println("Filtered listings count: " + filtered.size());

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        
        if (start >= filtered.size()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
        }
        
        List<Listing> pageContent = filtered.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filtered.size());
    }

    public List<Listing> getListingsByCategory(String category) {
        return listingRepository.findActiveListingsByCategory(Listing.Category.valueOf(category.toUpperCase()), Pageable.unpaged()).getContent();
    }

    public List<Listing> getListingsBySeller(String sellerId) {
        return listingRepository.findBySellerId(sellerId).stream()
                .filter(listing -> listing.getStatus() == ListingStatus.ACTIVE)
                .toList();
    }

    public List<Listing> getMyListings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return listingRepository.findBySellerId(user.getId());
    }

    public void updateListingStatus(String id, ListingStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Check if user owns the listing
        if (!listing.getSellerId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own listings");
        }

        listing.setStatus(status);
        listingRepository.save(listing);
    }

    public List<Listing> getFeaturedListings() {
        return listingRepository.findActiveFeaturedListings(LocalDateTime.now(), Pageable.unpaged()).getContent();
    }

    public List<Listing> getRecentListings() {
        return listingRepository.findRecentActiveListings(LocalDateTime.now().minusDays(7), Pageable.unpaged()).getContent();
    }

    public void incrementViewCount(String id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        listing.incrementViews();
        listingRepository.save(listing);
    }

    public void addToFavorites(String listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        listing.incrementFavorites();
        listingRepository.save(listing);
    }

    public void removeFromFavorites(String listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        listing.decrementFavorites();
        listingRepository.save(listing);
    }

    public List<Listing> getActiveDonationListings() {
        return listingRepository.findActiveDonationListings();
    }

    public Page<Listing> getActiveDonationListings(Pageable pageable) {
        return listingRepository.findActiveDonationListings(pageable);
    }
} 