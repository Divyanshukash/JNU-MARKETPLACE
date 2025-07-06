package com.jnu.marketplace.service;

import com.jnu.marketplace.dto.ListingRequest;
import com.jnu.marketplace.dto.SearchRequest;
import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.model.ListingStatus;
import com.jnu.marketplace.model.User;
import com.jnu.marketplace.repository.ListingRepository;
import com.jnu.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public Listing createListing(ListingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = Listing.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .condition(request.getCondition())
                .price(request.getPrice())
                .negotiable(request.isNegotiable())
                .images(request.getImages())
                .location(request.getLocation())
                .seller(user)
                .status(ListingStatus.ACTIVE)
                .build();

        return listingRepository.save(listing);
    }

    public Listing updateListing(String id, ListingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Check if user owns the listing
        if (!listing.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own listings");
        }

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(request.getCategory());
        listing.setCondition(request.getCondition());
        listing.setPrice(request.getPrice());
        listing.setNegotiable(request.isNegotiable());
        listing.setImages(request.getImages());
        listing.setLocation(request.getLocation());

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
        if (!listing.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own listings");
        }

        listingRepository.delete(listing);
    }

    public Listing getListingById(String id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
    }

    public Page<Listing> getAllListings(Pageable pageable) {
        return listingRepository.findByStatus(ListingStatus.ACTIVE, pageable);
    }

    public Page<Listing> searchListings(SearchRequest request, Pageable pageable) {
        return listingRepository.findBySearchCriteria(
                request.getQuery(),
                request.getCategory(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getCondition(),
                request.getLocation(),
                pageable
        );
    }

    public List<Listing> getListingsByCategory(String category) {
        return listingRepository.findByCategoryAndStatus(category, ListingStatus.ACTIVE);
    }

    public List<Listing> getListingsBySeller(String sellerId) {
        return listingRepository.findBySellerIdAndStatus(sellerId, ListingStatus.ACTIVE);
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
        if (!listing.getSeller().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own listings");
        }

        listing.setStatus(status);
        listingRepository.save(listing);
    }

    public List<Listing> getFeaturedListings() {
        return listingRepository.findFeaturedListings();
    }

    public List<Listing> getRecentListings() {
        return listingRepository.findRecentListings();
    }

    public void incrementViewCount(String id) {
        listingRepository.incrementViewCount(id);
    }

    public void addToFavorites(String listingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!listing.getFavorites().contains(user.getId())) {
            listing.getFavorites().add(user.getId());
            listingRepository.save(listing);
        }
    }

    public void removeFromFavorites(String listingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        listing.getFavorites().remove(user.getId());
        listingRepository.save(listing);
    }
} 