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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

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
        applyListingRequest(listing, request, false);
        listing.setSellerId(user.getId());
        listing.setSellerName(user.getFirstName() + " " + user.getLastName());
        listing.setStatus(ListingStatus.ACTIVE);
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

        applyListingRequest(listing, request, true);

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
        Query query = new Query(Criteria.where("status").is(ListingStatus.ACTIVE));

        if (request.hasKeyword()) {
            String keyword = Pattern.quote(request.getKeyword().trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(keyword, "i"),
                    Criteria.where("description").regex(keyword, "i"),
                    Criteria.where("tags").regex(keyword, "i"),
                    Criteria.where("subcategory").regex(keyword, "i"),
                    Criteria.where("pickupLocation").regex(keyword, "i")
            ));
        }
        if (hasText(request.getCategory())) query.addCriteria(Criteria.where("category").is(parseCategory(request.getCategory())));
        if (hasText(request.getSubCategory())) query.addCriteria(Criteria.where("subcategory").is(request.getSubCategory()));
        if (request.getCondition() != null) query.addCriteria(Criteria.where("condition").is(request.getCondition()));
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            Criteria price = Criteria.where("price");
            if (request.getMinPrice() != null) price = price.gte(request.getMinPrice());
            if (request.getMaxPrice() != null) price = price.lte(request.getMaxPrice());
            query.addCriteria(price);
        }
        if (hasText(request.getLocation())) query.addCriteria(Criteria.where("pickupLocation").regex(Pattern.quote(request.getLocation().trim()), "i"));
        if (hasText(request.getHostelBlock())) query.addCriteria(Criteria.where("pickupLocation").regex(Pattern.quote(request.getHostelBlock().trim()), "i"));
        if (request.isNegotiable()) query.addCriteria(Criteria.where("negotiable").is(true));
        if (request.isFeatured()) query.addCriteria(Criteria.where("isFeatured").is(true));
        if (request.getTags() != null && !request.getTags().isEmpty()) query.addCriteria(Criteria.where("tags").in(request.getTags()));
        if (hasText(request.getSellerId())) query.addCriteria(Criteria.where("sellerId").is(request.getSellerId()));

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), resolveSort(request));
        long total = mongoTemplate.count(query, Listing.class);
        List<Listing> listings = mongoTemplate.find(query.with(sortedPageable), Listing.class);
        return new PageImpl<>(listings, sortedPageable, total);
    }

    private void applyListingRequest(Listing listing, ListingRequest request, boolean preserveMissingOptionalValues) {
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(parseCategory(request.getCategory()));
        listing.setCondition(request.getCondition());
        listing.setPrice(request.getPrice());
        listing.setNegotiable(request.isNegotiable());
        listing.setImages(request.getImages());
        listing.setDonation(request.getDonation());
        listing.setLifeOfItem(request.getLifeOfItem());

        if (!preserveMissingOptionalValues || request.getSubCategory() != null) {
            listing.setSubcategory(request.getSubCategory());
        }
        if (!preserveMissingOptionalValues || request.getOriginalPrice() != null) {
            listing.setOriginalPrice(request.getOriginalPrice());
        }
        if (!preserveMissingOptionalValues || request.getTags() != null) {
            listing.setTags(normalizeTags(request.getTags()));
        }
        if (!preserveMissingOptionalValues || hasLocationFields(request)) {
            listing.setPickupLocation(buildPickupLocation(request));
        }
    }

    private Listing.Category parseCategory(String category) {
        if (!hasText(category)) throw new IllegalArgumentException("Category is required");
        return java.util.Arrays.stream(Listing.Category.values())
                .filter(value -> value.name().equalsIgnoreCase(category.trim())
                        || value.getDisplayName().equalsIgnoreCase(category.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + category));
    }

    private Sort resolveSort(SearchRequest request) {
        request.normalizeSorting();
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, request.getSortBy());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Set<String> normalizeTags(List<String> tags) {
        if (tags == null) return new HashSet<>();
        return tags.stream()
                .filter(this::hasText)
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));
    }

    private String buildPickupLocation(ListingRequest request) {
        List<String> parts = new ArrayList<>();
        if (hasText(request.getLocation())) parts.add(request.getLocation().trim());
        if (hasText(request.getHostelBlock())) parts.add("Hostel: " + request.getHostelBlock().trim());
        if (hasText(request.getRoomNumber())) parts.add("Room: " + request.getRoomNumber().trim());
        return parts.isEmpty() ? null : String.join(" | ", parts);
    }

    private boolean hasLocationFields(ListingRequest request) {
        return request.getLocation() != null || request.getHostelBlock() != null || request.getRoomNumber() != null;
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
