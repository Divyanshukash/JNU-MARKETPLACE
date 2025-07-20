package com.jnu.marketplace.dto;

import com.jnu.marketplace.model.Listing;
import java.math.BigDecimal;
import java.util.List;

public class SearchRequest {

    private String keyword;
    private String category;
    private String subCategory;
    private Listing.Condition condition;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String location;
    private String hostelBlock;
    private boolean negotiable;
    private boolean featured;
    private boolean urgent;
    private List<String> tags;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private int page = 0;
    private int size = 20;
    private String sellerId;

    // Constructors
    public SearchRequest() {}

    public SearchRequest(String keyword) {
        this.keyword = keyword;
    }

    // Getters and Setters
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
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

    public boolean isNegotiable() {
        return negotiable;
    }

    public void setNegotiable(boolean negotiable) {
        this.negotiable = negotiable;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    // Helper methods
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }

    public boolean hasFilters() {
        return category != null || subCategory != null || 
               condition != null || hasPriceRange() || location != null || 
               hostelBlock != null || 
               negotiable || featured || urgent || (tags != null && !tags.isEmpty());
    }

    public boolean isValidSortBy() {
        return sortBy != null && (sortBy.equals("createdAt") || sortBy.equals("price") || 
                sortBy.equals("viewsCount") || sortBy.equals("favoritesCount") || 
                sortBy.equals("title") || sortBy.equals("updatedAt"));
    }

    public boolean isValidSortOrder() {
        return sortOrder != null && (sortOrder.equals("asc") || sortOrder.equals("desc"));
    }

    public void normalizeSorting() {
        if (!isValidSortBy()) {
            this.sortBy = "createdAt";
        }
        if (!isValidSortOrder()) {
            this.sortOrder = "desc";
        }
    }

    public void normalizePagination() {
        if (this.page < 0) {
            this.page = 0;
        }
        if (this.size < 1 || this.size > 100) {
            this.size = 20;
        }
    }

    @Override
    public String toString() {
        return "SearchRequest{" +
                "keyword='" + keyword + '\'' +
                ", category='" + category + '\'' +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", sortBy='" + sortBy + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
} 