package com.jnu.marketplace.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listing entity representing items or services for sale
 * 
 * This entity stores information about items and services that users
 * want to sell, including details, pricing, images, and status.
 * 
 * @author JNU Marketplace Team
 * @version 1.0.0
 */
@Document(collection = "listings")
public class Listing {

    @Id
    private String id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    @TextIndexed(weight = 3)
    @Field("title")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @TextIndexed(weight = 2)
    @Field("description")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Field("price")
    private BigDecimal price;

    @Field("original_price")
    private BigDecimal originalPrice;

    @Field("negotiable")
    private boolean negotiable = false;

    @NotNull(message = "Category is required")
    @Field("category")
    private Category category;

    @Field("subcategory")
    private String subcategory;

    @Field("condition")
    private Condition condition = Condition.NEW;

    @Field("brand")
    private String brand;

    @Field("model")
    private String model;

    @Field("year")
    private Integer year;

    @Field("images")
    private List<String> images = new ArrayList<>();

    @Field("main_image")
    private String mainImage;

    @Field("tags")
    private Set<String> tags = new HashSet<>();

    @Field("location")
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private Location location;

    @Field("pickup_location")
    private String pickupLocation;

    @Field("delivery_available")
    private boolean deliveryAvailable = false;

    @Field("delivery_cost")
    private BigDecimal deliveryCost = BigDecimal.ZERO;

    @Field("delivery_radius")
    private Integer deliveryRadius = 5; // in kilometers

    @Field("seller_id")
    @Indexed
    private String sellerId;

    @Field("seller_name")
    private String sellerName;

    @Field("seller_rating")
    private Double sellerRating = 0.0;

    @Field("status")
    private ListingStatus status = ListingStatus.ACTIVE;

    @Field("views")
    private int views = 0;

    @Field("favorites")
    private int favorites = 0;

    @Field("contact_count")
    private int contactCount = 0;

    @Field("is_featured")
    private boolean isFeatured = false;

    @Field("featured_until")
    private LocalDateTime featuredUntil;

    @Field("expires_at")
    private LocalDateTime expiresAt;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Listing() {}

    public Listing(String title, String description, BigDecimal price, Category category, String sellerId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.sellerId = sellerId;
    }

    // Helper methods
    public void incrementViews() {
        this.views++;
    }

    public void incrementFavorites() {
        this.favorites++;
    }

    public void decrementFavorites() {
        if (this.favorites > 0) {
            this.favorites--;
        }
    }

    public void incrementContactCount() {
        this.contactCount++;
    }

    public void addImage(String imageUrl) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imageUrl);
        if (this.mainImage == null) {
            this.mainImage = imageUrl;
        }
    }

    public void removeImage(String imageUrl) {
        if (this.images != null) {
            this.images.remove(imageUrl);
            if (this.mainImage.equals(imageUrl) && !this.images.isEmpty()) {
                this.mainImage = this.images.get(0);
            }
        }
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.add(tag.toLowerCase());
    }

    public void removeTag(String tag) {
        if (this.tags != null) {
            this.tags.remove(tag.toLowerCase());
        }
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isFeatured() {
        return isFeatured && (featuredUntil == null || LocalDateTime.now().isBefore(featuredUntil));
    }

    public BigDecimal getDiscountedPrice() {
        if (originalPrice != null && originalPrice.compareTo(price) > 0) {
            return originalPrice.subtract(price);
        }
        return BigDecimal.ZERO;
    }

    public double getDiscountPercentage() {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            return ((originalPrice.doubleValue() - price.doubleValue()) / originalPrice.doubleValue()) * 100;
        }
        return 0.0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public boolean isNegotiable() {
        return negotiable;
    }

    public void setNegotiable(boolean negotiable) {
        this.negotiable = negotiable;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public boolean isDeliveryAvailable() {
        return deliveryAvailable;
    }

    public void setDeliveryAvailable(boolean deliveryAvailable) {
        this.deliveryAvailable = deliveryAvailable;
    }

    public BigDecimal getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(BigDecimal deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public Integer getDeliveryRadius() {
        return deliveryRadius;
    }

    public void setDeliveryRadius(Integer deliveryRadius) {
        this.deliveryRadius = deliveryRadius;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public Double getSellerRating() {
        return sellerRating;
    }

    public void setSellerRating(Double sellerRating) {
        this.sellerRating = sellerRating;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public int getContactCount() {
        return contactCount;
    }

    public void setContactCount(int contactCount) {
        this.contactCount = contactCount;
    }

    public boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public LocalDateTime getFeaturedUntil() {
        return featuredUntil;
    }

    public void setFeaturedUntil(LocalDateTime featuredUntil) {
        this.featuredUntil = featuredUntil;
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
    public enum Category {
        BOOKS("Books"),
        ELECTRONICS("Electronics"),
        FURNITURE("Furniture"),
        CLOTHING("Clothing"),
        SPORTS("Sports & Fitness"),
        MUSICAL_INSTRUMENTS("Musical Instruments"),
        VEHICLES("Vehicles"),
        SERVICES("Services"),
        FOOD("Food & Beverages"),
        ART("Art & Collectibles"),
        BEAUTY("Beauty & Personal Care"),
        HOME("Home & Garden"),
        TOYS("Toys & Games"),
        HEALTH("Health & Wellness"),
        EDUCATION("Education & Training"),
        OTHER("Other");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Condition {
        NEW("New"),
        LIKE_NEW("Like New"),
        EXCELLENT("Excellent"),
        GOOD("Good"),
        FAIR("Fair"),
        POOR("Poor");

        private final String displayName;

        Condition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ListingStatus {
        DRAFT("Draft"),
        ACTIVE("Active"),
        SOLD("Sold"),
        EXPIRED("Expired"),
        SUSPENDED("Suspended"),
        DELETED("Deleted");

        private final String displayName;

        ListingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Inner class for location
    public static class Location {
        @Field("type")
        private String type = "Point";

        @Field("coordinates")
        private double[] coordinates; // [longitude, latitude]

        @Field("address")
        private String address;

        @Field("city")
        private String city;

        @Field("state")
        private String state;

        @Field("pincode")
        private String pincode;

        // Constructors
        public Location() {}

        public Location(double longitude, double latitude, String address) {
            this.coordinates = new double[]{longitude, latitude};
            this.address = address;
        }

        // Getters and Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double[] getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(double[] coordinates) {
            this.coordinates = coordinates;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPincode() {
            return pincode;
        }

        public void setPincode(String pincode) {
            this.pincode = pincode;
        }
    }
} 