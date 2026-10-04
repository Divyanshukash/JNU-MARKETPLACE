package com.jnu.marketplace.ai.search;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Exponential time decay based on when the listing was created. Range (0, 1].
 *
 *   freshness = exp(-ageInDays / 30)
 *
 * A listing posted today scores 1.0. The score halves about every 21 days (30 * ln 2) and
 * decays smoothly, so there is no arbitrary cutoff. A createdAt in the future (clock skew)
 * counts as age 0. A missing createdAt gets the neutral value 0.5, so it is neither rewarded nor penalized.
 *
 * Uses createdAt, not updatedAt, because "newer" should mean "recently posted". Views and
 * favorites change updatedAt, which would make old listings look fresh.
 */
public final class FreshnessScorer {

    static final double TIME_CONSTANT_DAYS = 30.0;
    static final double NEUTRAL = 0.5;

    private FreshnessScorer() {}

    public static double score(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null || now == null) return NEUTRAL;
        double ageDays = Math.max(0.0, Duration.between(createdAt, now).getSeconds() / 86_400.0);
        return ScoreRange.clamp01(Math.exp(-ageDays / TIME_CONSTANT_DAYS));
    }
}
