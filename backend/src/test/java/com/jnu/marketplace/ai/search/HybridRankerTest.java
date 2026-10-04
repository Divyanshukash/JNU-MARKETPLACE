package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.ai.AiProperties;
import com.jnu.marketplace.ai.search.HybridRanker.RankedListing;
import com.jnu.marketplace.ai.search.HybridRanker.RankingInput;
import com.jnu.marketplace.model.Listing;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HybridRankerTest {

    private final HybridRanker ranker = new HybridRanker(new AiProperties.Ranking(0.70, 0.15, 0.10, 0.05));

    @Test
    void weightedFormulaMatchesDocumentedWeights() {
        // 0.70*0.8 + 0.15*0.4 + 0.10*0.5 + 0.05*0.6 = 0.56 + 0.06 + 0.05 + 0.03 = 0.70
        assertThat(ranker.finalScore(0.8, 0.4, 0.5, 0.6)).isCloseTo(0.70, within(1e-9));
    }

    @Test
    void finalScoreStaysWithinZeroToOne() {
        assertThat(ranker.finalScore(1, 1, 1, 1)).isCloseTo(1.0, within(1e-9));
        assertThat(ranker.finalScore(0, 0, 0, 0)).isZero();
    }

    @Test
    void componentsAreClampedBeforeWeighting() {
        assertThat(ranker.finalScore(1.7, 0, 0, 0)).isCloseTo(0.70, within(1e-9));
        assertThat(ranker.finalScore(Double.NaN, 0, 0, 0)).isZero();
        assertThat(ranker.finalScore(-0.5, 0, 0, 0)).isZero();
    }

    @Test
    void higherSemanticScoreImprovesRanking() {
        List<RankedListing> ranked = ranker.rank(List.of(
                input("weak-match", 0.5, 0, 0, 0),
                input("strong-match", 0.9, 0, 0, 0)));

        assertThat(ids(ranked)).containsExactly("strong-match", "weak-match");
    }

    @Test
    void lexicalExactMatchImprovesRanking() {
        List<RankedListing> ranked = ranker.rank(List.of(
                input("no-word-overlap", 0.6, 0.0, 0.5, 0.5),
                input("exact-title", 0.6, 1.0, 0.5, 0.5)));

        assertThat(ids(ranked)).containsExactly("exact-title", "no-word-overlap");
    }

    @Test
    void freshnessAndQualityInfluenceOrdering() {
        List<RankedListing> ranked = ranker.rank(List.of(
                input("stale-low-quality", 0.6, 0.5, 0.2, 0.1),
                input("fresh-high-quality", 0.6, 0.5, 1.0, 0.9)));

        assertThat(ids(ranked)).containsExactly("fresh-high-quality", "stale-low-quality");
    }

    @Test
    void equalScoresAreOrderedByListingIdAscending() {
        List<RankedListing> ranked = ranker.rank(List.of(
                input("c", 0.5, 0.5, 0.5, 0.5),
                input("a", 0.5, 0.5, 0.5, 0.5),
                input("b", 0.5, 0.5, 0.5, 0.5)));

        assertThat(ids(ranked)).containsExactly("a", "b", "c");
    }

    @Test
    void orderingIsIndependentOfInputOrder() {
        List<RankedListing> forward = ranker.rank(List.of(
                input("x", 0.4, 0.3, 0.2, 0.1), input("y", 0.9, 0.1, 0.1, 0.1), input("z", 0.4, 0.3, 0.2, 0.1)));
        List<RankedListing> reversed = ranker.rank(List.of(
                input("z", 0.4, 0.3, 0.2, 0.1), input("y", 0.9, 0.1, 0.1, 0.1), input("x", 0.4, 0.3, 0.2, 0.1)));

        assertThat(ids(forward)).isEqualTo(ids(reversed));
    }

    @Test
    void rankedListingKeepsItsComponentScores() {
        RankedListing ranked = ranker.rank(List.of(input("only", 0.8, 0.4, 0.5, 0.6))).get(0);

        assertThat(ranked.semantic()).isEqualTo(0.8);
        assertThat(ranked.lexical()).isEqualTo(0.4);
        assertThat(ranked.freshness()).isEqualTo(0.5);
        assertThat(ranked.quality()).isEqualTo(0.6);
        assertThat(ranked.finalScore()).isCloseTo(0.70, within(1e-9));
    }

    private static RankingInput input(String id, double semantic, double lexical, double freshness, double quality) {
        Listing listing = new Listing();
        listing.setId(id);
        return new RankingInput(listing, semantic, lexical, freshness, quality);
    }

    private static List<String> ids(List<RankedListing> ranked) {
        return ranked.stream().map(r -> r.listing().getId()).toList();
    }
}
