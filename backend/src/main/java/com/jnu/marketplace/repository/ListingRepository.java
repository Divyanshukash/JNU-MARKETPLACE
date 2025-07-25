package com.jnu.marketplace.repository;

import com.jnu.marketplace.model.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Repository interface for Listing entity
 * 
 * This repository provides data access methods for listing management,
 * including search, filtering, and geospatial queries.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Repository
public interface ListingRepository extends MongoRepository<Listing, String> {

    // Basic find methods
    List<Listing> findBySellerId(String sellerId);
    
    Page<Listing> findBySellerId(String sellerId, Pageable pageable);
    
    List<Listing> findByStatus(Listing.ListingStatus status);
    
    Page<Listing> findByStatus(Listing.ListingStatus status, Pageable pageable);
    
    List<Listing> findByCategory(Listing.Category category);
    
    Page<Listing> findByCategory(Listing.Category category, Pageable pageable);

    // Price-based queries
    List<Listing> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<Listing> findByPriceLessThanEqual(BigDecimal maxPrice);
    
    List<Listing> findByPriceGreaterThanEqual(BigDecimal minPrice);
    
    @Query("{'price': {'$gte': ?0, '$lte': ?1}, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Category and subcategory queries
    List<Listing> findByCategoryAndSubcategory(Listing.Category category, String subcategory);
    
    List<Listing> findBySubcategory(String subcategory);
    
    @Query("{'category': ?0, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsByCategory(Listing.Category category, Pageable pageable);

    // Condition-based queries
    List<Listing> findByCondition(Listing.Condition condition);
    
    List<Listing> findByCategoryAndCondition(Listing.Category category, Listing.Condition condition);
    
    @Query("{'condition': ?0, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsByCondition(Listing.Condition condition, Pageable pageable);

    // Brand and model queries
    List<Listing> findByBrand(String brand);
    
    List<Listing> findByBrandAndModel(String brand, String model);
    
    List<Listing> findByCategoryAndBrand(Listing.Category category, String brand);

    // Negotiable queries
    List<Listing> findByNegotiable(boolean negotiable);
    
    @Query("{'negotiable': true, 'status': 'ACTIVE'}")
    Page<Listing> findActiveNegotiableListings(Pageable pageable);

    // Delivery queries
    List<Listing> findByDeliveryAvailable(boolean deliveryAvailable);
    
    List<Listing> findByDeliveryCostLessThanEqual(BigDecimal maxDeliveryCost);
    
    @Query("{'deliveryAvailable': true, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsWithDelivery(Pageable pageable);

    // Featured and urgent queries
    List<Listing> findByIsFeatured(boolean isFeatured);
    
    @Query("{'isFeatured': true, 'status': 'ACTIVE', '$or': [{'featuredUntil': null}, {'featuredUntil': {'$gt': ?0}}]}")
    Page<Listing> findActiveFeaturedListings(LocalDateTime now, Pageable pageable);

    // Date-based queries
    List<Listing> findByCreatedAtAfter(LocalDateTime dateTime);
    
    List<Listing> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Listing> findByExpiresAtBefore(LocalDateTime dateTime);
    
    @Query("{'createdAt': {'$gte': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> findRecentActiveListings(LocalDateTime since, Pageable pageable);

    // Expired and expiring queries
    @Query("{'expiresAt': {'$lt': ?0}, 'status': 'ACTIVE'}")
    List<Listing> findExpiredActiveListings(LocalDateTime now);
    
    @Query("{'expiresAt': {'$gte': ?0, '$lte': ?1}, 'status': 'ACTIVE'}")
    List<Listing> findListingsExpiringBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Views and favorites queries
    List<Listing> findByViewsGreaterThan(int minViews);
    
    List<Listing> findByFavoritesGreaterThan(int minFavorites);
    
    @Query("{'views': {'$gte': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> findPopularActiveListings(int minViews, Pageable pageable);
    
    @Query("{'favorites': {'$gte': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> findMostFavoritedActiveListings(int minFavorites, Pageable pageable);

    // Contact queries
    List<Listing> findByContactCountGreaterThan(int minContacts);
    
    @Query("{'contactCount': {'$gte': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> findMostContactedActiveListings(int minContacts, Pageable pageable);

    // Seller rating queries
    List<Listing> findBySellerRatingGreaterThanEqual(double minRating);
    
    @Query("{'sellerRating': {'$gte': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsBySellerRating(double minRating, Pageable pageable);

    // Text search queries
    @Query("{'$text': {'$search': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> searchActiveListings(String searchTerm, Pageable pageable);
    
    @Query("{'$or': [{'title': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}, {'tags': {'$regex': ?0, '$options': 'i'}}], 'status': 'ACTIVE'}")
    Page<Listing> searchActiveListingsByText(String searchTerm, Pageable pageable);

    // Tag-based queries
    @Query("{'tags': ?0, 'status': 'ACTIVE'}")
    List<Listing> findActiveListingsByTag(String tag);
    
    @Query("{'tags': {'$in': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsByTags(List<String> tags, Pageable pageable);

    // Complex search queries
    @Query("{'$and': [{'category': ?0}, {'price': {'$gte': ?1, '$lte': ?2}}, {'status': 'ACTIVE'}]}")
    Page<Listing> findActiveListingsByCategoryAndPriceRange(Listing.Category category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    @Query("{'$and': [{'category': ?0}, {'condition': ?1}, {'price': {'$lte': ?2}}, {'status': 'ACTIVE'}]}")
    Page<Listing> findActiveListingsByCategoryConditionAndMaxPrice(Listing.Category category, Listing.Condition condition, BigDecimal maxPrice, Pageable pageable);

    // Geospatial queries
    @Query("{'location': {'$near': {'$geometry': {'type': 'Point', 'coordinates': [?0, ?1]}, '$maxDistance': ?2}}, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsNearLocation(double longitude, double latitude, double maxDistance, Pageable pageable);
    
    List<Listing> findByLocationNear(Point location, Distance distance);
    
    @Query("{'location.coordinates': {'$geoWithin': {'$centerSphere': [[?0, ?1], ?2]}}, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsWithinRadius(double longitude, double latitude, double radiusInRadians, Pageable pageable);

    // Pickup location queries
    List<Listing> findByPickupLocationContainingIgnoreCase(String location);
    
    @Query("{'pickupLocation': {'$regex': ?0, '$options': 'i'}, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsByPickupLocation(String location, Pageable pageable);

    // Year-based queries
    List<Listing> findByYear(Integer year);
    
    List<Listing> findByYearBetween(Integer startYear, Integer endYear);
    
    @Query("{'year': {'$gte': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> findActiveListingsByMinYear(Integer minYear, Pageable pageable);

    // Combined filters
    @Query("{'$and': [{'category': ?0}, {'price': {'$lte': ?1}}, {'deliveryAvailable': ?2}, {'status': 'ACTIVE'}]}")
    Page<Listing> findActiveListingsByCategoryPriceAndDelivery(Listing.Category category, BigDecimal maxPrice, boolean deliveryAvailable, Pageable pageable);
    
    @Query("{'$and': [{'category': ?0}, {'condition': ?1}, {'negotiable': ?2}, {'status': 'ACTIVE'}]}")
    Page<Listing> findActiveListingsByCategoryConditionAndNegotiable(Listing.Category category, Listing.Condition condition, boolean negotiable, Pageable pageable);

    // Statistics queries
    long countByStatus(Listing.ListingStatus status);
    
    long countByCategory(Listing.Category category);
    
    long countBySellerId(String sellerId);
    
    long countByStatusAndSellerId(Listing.ListingStatus status, String sellerId);
    
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
    long countByCategoryAndStatus(Listing.Category category, Listing.ListingStatus status);

    // Price statistics
    @Query(value = "{'status': 'ACTIVE'}", fields = "{'price': 1}")
    List<Listing> findActiveListingPrices();
    
    @Query(value = "{'category': ?0, 'status': 'ACTIVE'}", fields = "{'price': 1}")
    List<Listing> findActiveListingPricesByCategory(Listing.Category category);

    // Popular categories
    @Query(value = "{'status': 'ACTIVE'}", fields = "{'category': 1}")
    List<Listing> findActiveListingCategories();

    // Recent activity
    @Query("{'updatedAt': {'$gte': ?0}, 'status': 'ACTIVE'}")
    List<Listing> findRecentlyUpdatedActiveListings(LocalDateTime since);

    // Expiring soon
    @Query("{'expiresAt': {'$gte': ?0, '$lte': ?1}, 'status': 'ACTIVE'}")
    List<Listing> findListingsExpiringSoon(LocalDateTime now, LocalDateTime soon);

    // High demand listings
    @Query("{'$and': [{'views': {'$gte': ?0}}, {'contactCount': {'$gte': ?1}}, {'status': 'ACTIVE'}]}")
    Page<Listing> findHighDemandActiveListings(int minViews, int minContacts, Pageable pageable);

    // New listings
    @Query("{'createdAt': {'$gte': ?0}, 'status': 'ACTIVE'}")
    Page<Listing> findNewActiveListings(LocalDateTime since, Pageable pageable);

    // Best deals (high discount)
    @Query("{'$and': [{'originalPrice': {'$exists': true, '$ne': null}}, {'price': {'$lt': '$originalPrice'}}, {'status': 'ACTIVE'}]}")
    Page<Listing> findDiscountedActiveListings(Pageable pageable);

    @Query("{'isDonation': true, 'status': 'ACTIVE'}")
    List<Listing> findActiveDonationListings();

    @Query("{'isDonation': true, 'status': 'ACTIVE'}")
    Page<Listing> findActiveDonationListings(Pageable pageable);
} 