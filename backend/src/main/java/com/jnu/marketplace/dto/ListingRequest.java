package com.jnu.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jnu.marketplace.model.Listing;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class ListingRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private BigDecimal originalPrice;
    private boolean negotiable = false;
    @JsonProperty("isDonation")
    private boolean donation = false;

    @NotBlank(message = "Category is required")
    private String category;

    private String subCategory;
    private Listing.Condition condition;

    private List<String> images;
    private String location;
    private String hostelBlock;
    private String roomNumber;
    private List<String> tags;
    private String lifeOfItem;

    // Constructors
    public ListingRequest() {}

    public ListingRequest(String title, String description, BigDecimal price, String category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    // Getters and Setters
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

    public boolean getDonation() {
        return donation;
    }

    public void setDonation(boolean donation) {
        this.donation = donation;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public Listing.Condition getCondition() {
        return condition;
    }

    public void setCondition(Listing.Condition condition) {
        this.condition = condition;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHostelBlock() {
        return hostelBlock;
    }

    public void setHostelBlock(String hostelBlock) {
        this.hostelBlock = hostelBlock;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getLifeOfItem() {
        return lifeOfItem;
    }
    public void setLifeOfItem(String lifeOfItem) {
        this.lifeOfItem = lifeOfItem;
    }

    @Override
    public String toString() {
        return "ListingRequest{" +
                "title='" + title + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", negotiable=" + negotiable +
                '}';
    }
} 
