package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.model.Listing;

/**
 * Listing quality from data the Listing entity actually stores. Range [0, 1].
 *
 *   quality = 0.5 * completeness + 0.5 * engagement
 *
 * completeness = (number of present signals) / 4, where the signals are:
 *   - at least one image
 *   - description of 50+ characters
 *   - at least one tag
 *   - a subcategory
 *
 * engagement = 0.5 + 0.5 * raw / (raw + 5), where raw = favorites + contactCount + views / 10.
 * With no engagement the component is 0.5 (neutral), so new listings are not penalized
 * for having no views yet. The function is continuous: a small amount of engagement moves
 * the score up from the neutral value, never down.
 *
 * Deliberately not used: seller identity, seller rating, and price. Price would reward or
 * punish categories, and seller identity is not a property of the listing.
 */
public final class QualityScorer {

    static final int MIN_DESCRIPTION_LENGTH = 50;
    static final double SATURATION_RAW = 5.0;

    private QualityScorer() {}

    public static double score(Listing listing) {
        if (listing == null) return 0.0;
        double completeness = completeness(listing);
        double engagement = engagement(listing);
        return ScoreRange.clamp01(0.5 * completeness + 0.5 * engagement);
    }

    static double completeness(Listing listing) {
        int present = 0;
        if (listing.getImages() != null && !listing.getImages().isEmpty()) present++;
        if (listing.getDescription() != null && listing.getDescription().trim().length() >= MIN_DESCRIPTION_LENGTH) present++;
        if (listing.getTags() != null && !listing.getTags().isEmpty()) present++;
        if (listing.getSubcategory() != null && !listing.getSubcategory().isBlank()) present++;
        return present / 4.0;
    }

    static double engagement(Listing listing) {
        double raw = listing.getFavorites() + listing.getContactCount() + listing.getViews() / 10.0;
        if (raw <= 0) return 0.5;
        return ScoreRange.clamp01(0.5 + 0.5 * raw / (raw + SATURATION_RAW));
    }
}
