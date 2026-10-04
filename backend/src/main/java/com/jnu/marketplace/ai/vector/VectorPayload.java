package com.jnu.marketplace.ai.vector;

import com.jnu.marketplace.model.Listing;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Metadata stored next to each vector. It is only what later candidate filtering and
 * ranking need. It is not a copy of the listing.
 *
 * Deliberately excluded: title, description, seller name, email, pickup location,
 * messages, and any other free text or personal data. Those stay in MongoDB.
 *
 * sellerId is an internal id, kept so a later phase can exclude a seller's own listings.
 * Nulls are dropped by toMap(), so the payload contains only values that exist.
 */
public record VectorPayload(
        String listingId,
        String status,
        String category,
        String subcategory,
        Double price,
        boolean donation,
        String sellerId,
        String updatedAt
) {

    public static VectorPayload from(Listing listing) {
        return new VectorPayload(
                listing.getId(),
                listing.getStatus() == null ? null : listing.getStatus().name(),
                listing.getCategory() == null ? null : listing.getCategory().name(),
                listing.getSubcategory(),
                listing.getPrice() == null ? null : listing.getPrice().doubleValue(),
                listing.isDonation(),
                listing.getSellerId(),
                listing.getUpdatedAt() == null ? null : listing.getUpdatedAt().toString()
        );
    }

    /** Converts to the primitive map the store expects. Null values are omitted. */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        put(map, "listingId", listingId);
        put(map, "status", status);
        put(map, "category", category);
        put(map, "subcategory", subcategory);
        put(map, "price", price);
        map.put("donation", donation);
        put(map, "sellerId", sellerId);
        put(map, "updatedAt", updatedAt);
        return map;
    }

    private static void put(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }
}
