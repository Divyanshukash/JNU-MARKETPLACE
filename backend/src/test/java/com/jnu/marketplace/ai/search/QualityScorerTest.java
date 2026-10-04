package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.model.Listing;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class QualityScorerTest {

    private static final String LONG_DESCRIPTION = "A well looked after item with a complete service history and all original parts.";

    @Test
    void completeListingWithEngagementScoresNormalizedValue() {
        Listing listing = completeListing();
        listing.setFavorites(3);
        listing.setContactCount(2);
        listing.setViews(50);

        double raw = 3 + 2 + 50 / 10.0;
        double expected = 0.5 * 1.0 + 0.5 * (0.5 + 0.5 * raw / (raw + 5));

        assertThat(QualityScorer.score(listing)).isCloseTo(expected, within(1e-9));
        assertThat(QualityScorer.score(listing)).isBetween(0.0, 1.0);
    }

    @Test
    void insufficientDataGetsNeutralEngagement() {
        // Nothing but title and price: no images, no tags, no subcategory, short description, zero engagement.
        Listing sparse = new Listing();
        sparse.setTitle("Lamp");
        sparse.setDescription("short");

        // completeness 0, engagement neutral 0.5  ->  0.5 * 0 + 0.5 * 0.5
        assertThat(QualityScorer.score(sparse)).isCloseTo(0.25, within(1e-9));
    }

    @Test
    void nullFieldsDoNotThrow() {
        Listing bare = new Listing();
        bare.setImages(null);
        bare.setTags(null);
        bare.setDescription(null);
        bare.setSubcategory(null);

        assertThat(QualityScorer.score(bare)).isBetween(0.0, 1.0);
    }

    @Test
    void engagementMovesScoreUpFromNeutral() {
        Listing none = completeListing();
        Listing some = completeListing();
        some.setFavorites(1);

        assertThat(QualityScorer.score(some)).isGreaterThan(QualityScorer.score(none));
    }

    @Test
    void engagementSaturatesBelowOne() {
        Listing viral = completeListing();
        viral.setFavorites(100_000);
        viral.setViews(10_000_000);

        assertThat(QualityScorer.score(viral)).isLessThanOrEqualTo(1.0).isGreaterThan(0.9);
    }

    @Test
    void sellerIdentityIsNotAQualitySignal() {
        Listing a = completeListing();
        Listing b = completeListing();
        b.setSellerId("different-seller");
        b.setSellerName("Different Name");

        assertThat(QualityScorer.score(a)).isEqualTo(QualityScorer.score(b));
    }

    @Test
    void completenessCountsEachSignalOnce() {
        Listing listing = new Listing();
        listing.setImages(List.of("/uploads/a.png"));
        listing.setDescription(LONG_DESCRIPTION);

        assertThat(QualityScorer.completeness(listing)).isEqualTo(0.5);
    }

    private static Listing completeListing() {
        Listing listing = new Listing();
        listing.setImages(List.of("/uploads/a.png"));
        listing.setDescription(LONG_DESCRIPTION);
        listing.setTags(Set.of("vintage"));
        listing.setSubcategory("Furniture Parts");
        return listing;
    }
}
