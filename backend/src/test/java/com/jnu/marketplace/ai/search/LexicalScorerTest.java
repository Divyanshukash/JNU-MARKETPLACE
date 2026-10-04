package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.model.Listing;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class LexicalScorerTest {

    @Test
    void exactTitleMatchScoresOne() {
        assertThat(LexicalScorer.score("mountain bike", listing("Mountain Bike", "desc", null, null)))
                .isEqualTo(1.0);
    }

    @Test
    void exactMatchIgnoresCaseAndPunctuation() {
        assertThat(LexicalScorer.score("MOUNTAIN, bike!", listing("mountain bike", "desc", null, null)))
                .isEqualTo(1.0);
    }

    @Test
    void titleTokenMatchOutscoresDescriptionOnlyMatch() {
        double titleMatch = LexicalScorer.score("bike", listing("Mountain Bike", "Nothing relevant", null, null));
        double descriptionMatch = LexicalScorer.score("bike", listing("Road Gear", "bike for sale", null, null));

        assertThat(titleMatch).isGreaterThan(descriptionMatch);
        assertThat(titleMatch).isCloseTo(3.0 / 7.5, within(1e-9));
        assertThat(descriptionMatch).isCloseTo(1.0 / 7.5, within(1e-9));
    }

    @Test
    void tagsContributeTheirWeight() {
        Listing tagged = listing("Gear", "desc", null, null);
        tagged.setTags(Set.of("bike"));

        assertThat(LexicalScorer.score("bike", tagged)).isCloseTo(2.0 / 7.5, within(1e-9));
    }

    @Test
    void categoryContributesItsWeight() {
        Listing vehicle = listing("Gear", "desc", Listing.Category.VEHICLES, null);

        assertThat(LexicalScorer.score("vehicles", vehicle)).isCloseTo(1.5 / 7.5, within(1e-9));
    }

    @Test
    void subcategoryCountsAsCategory() {
        Listing listing = listing("Gear", "desc", null, "bike rack");

        assertThat(LexicalScorer.score("rack", listing)).isCloseTo(1.5 / 7.5, within(1e-9));
    }

    @Test
    void scoreIsAveragedOverQueryTokens() {
        // "bike" matches the title (3.0). "gear" matches nothing. Two tokens give 3.0 / (2 * 7.5).
        assertThat(LexicalScorer.score("bike gear", listing("Bike", "desc", null, null)))
                .isCloseTo(3.0 / 15.0, within(1e-9));
    }

    @Test
    void emptyOrBlankQueryScoresZero() {
        Listing listing = listing("Mountain Bike", "desc", null, null);

        assertThat(LexicalScorer.score("", listing)).isZero();
        assertThat(LexicalScorer.score("   ", listing)).isZero();
        assertThat(LexicalScorer.score(null, listing)).isZero();
        assertThat(LexicalScorer.score("!!!", listing)).isZero();
    }

    @Test
    void missingListingFieldsAreHandled() {
        Listing empty = new Listing();

        assertThat(LexicalScorer.score("bike", empty)).isZero();
    }

    @Test
    void scoreStaysWithinZeroToOne() {
        Listing everything = listing("bike bike", "bike bike bike", Listing.Category.VEHICLES, "bike");
        everything.setTags(Set.of("bike", "bicycle"));

        for (String query : List.of("bike", "bike bike", "bike vehicles", "x y z")) {
            double score = LexicalScorer.score(query, everything);
            assertThat(score).isBetween(0.0, 1.0);
        }
    }

    @Test
    void scoreIsDeterministic() {
        Listing listing = listing("Mountain Bike", "Good condition", null, null);

        assertThat(LexicalScorer.score("bike good", listing))
                .isEqualTo(LexicalScorer.score("bike good", listing));
    }

    private static Listing listing(String title, String description, Listing.Category category, String subcategory) {
        Listing listing = new Listing();
        listing.setTitle(title);
        listing.setDescription(description);
        listing.setCategory(category);
        listing.setSubcategory(subcategory);
        return listing;
    }
}
