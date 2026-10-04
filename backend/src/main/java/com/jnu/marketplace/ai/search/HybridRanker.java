package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.ai.AiProperties;
import com.jnu.marketplace.model.Listing;

import java.util.Comparator;
import java.util.List;

/**
 * Combines the four ranking components into one score and sorts by it.
 *
 *   finalScore = w.semantic  * semantic
 *              + w.lexical   * lexical
 *              + w.freshness * freshness
 *              + w.quality   * quality
 *
 * Default weights: semantic 0.70, lexical 0.15, freshness 0.10, quality 0.05. They sum to 1,
 * so finalScore is in [0, 1]. Each component is clamped to [0, 1] before weighting.
 *
 * Why these weights:
 * - semantic (0.70): the vector match is what lets a query find items that use different words.
 *   It must dominate.
 * - lexical (0.15): exact words are a strong, cheap signal, and they fix cases where the
 *   embedding is vague.
 * - freshness (0.10): newer listings should surface first among equally relevant results.
 * - quality (0.05): a light tie-breaker. It is kept small because quality data is sparse
 *   early in a listing's life.
 *
 * Ties are broken by listing id, ascending, so identical inputs always give the same order.
 *
 * This is a fixed, hand-tuned formula. There is no learned ranking.
 */
public class HybridRanker {

    private static final Comparator<RankedListing> ORDER = Comparator
            .comparingDouble(RankedListing::finalScore).reversed()
            .thenComparing(ranked -> ranked.listing().getId(), Comparator.nullsLast(Comparator.naturalOrder()));

    private final AiProperties.Ranking weights;

    public HybridRanker(AiProperties.Ranking weights) {
        this.weights = weights;
    }

    public double finalScore(double semantic, double lexical, double freshness, double quality) {
        return weights.semantic() * ScoreRange.clamp01(semantic)
                + weights.lexical() * ScoreRange.clamp01(lexical)
                + weights.freshness() * ScoreRange.clamp01(freshness)
                + weights.quality() * ScoreRange.clamp01(quality);
    }

    /** Scores every input and returns them sorted by finalScore, highest first. */
    public List<RankedListing> rank(List<RankingInput> inputs) {
        return inputs.stream()
                .map(in -> new RankedListing(
                        in.listing(),
                        in.semantic(),
                        in.lexical(),
                        in.freshness(),
                        in.quality(),
                        finalScore(in.semantic(), in.lexical(), in.freshness(), in.quality())))
                .sorted(ORDER)
                .toList();
    }

    /** Raw component scores for one listing, before weighting. */
    public record RankingInput(Listing listing, double semantic, double lexical, double freshness, double quality) {}

    /** A listing with its components and final score. Components are kept for debugging and tests. */
    public record RankedListing(Listing listing, double semantic, double lexical, double freshness, double quality, double finalScore) {}
}
