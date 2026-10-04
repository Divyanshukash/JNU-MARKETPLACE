package com.jnu.marketplace.event;

/**
 * Published after a listing is created, changed, deactivated, sold, reactivated, or deleted.
 *
 * It carries only the listing id. Listeners reload the listing from MongoDB, so they always act
 * on committed data. This event is in the shared event package, so the marketplace services do
 * not depend on the AI package.
 */
public record ListingChangedEvent(String listingId) {}
