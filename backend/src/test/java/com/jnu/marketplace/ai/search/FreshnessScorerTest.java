package com.jnu.marketplace.ai.search;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class FreshnessScorerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 4, 12, 0);

    @Test
    void listingPostedNowScoresOne() {
        assertThat(FreshnessScorer.score(NOW, NOW)).isEqualTo(1.0);
    }

    @Test
    void thirtyDayOldListingDecaysByOneEFold() {
        assertThat(FreshnessScorer.score(NOW.minusDays(30), NOW)).isCloseTo(Math.exp(-1.0), within(1e-9));
    }

    @Test
    void newerListingScoresHigherThanOlder() {
        double newer = FreshnessScorer.score(NOW.minusDays(2), NOW);
        double older = FreshnessScorer.score(NOW.minusDays(60), NOW);

        assertThat(newer).isGreaterThan(older);
    }

    @Test
    void decayIsSmoothNotBinary() {
        double day10 = FreshnessScorer.score(NOW.minusDays(10), NOW);
        double day11 = FreshnessScorer.score(NOW.minusDays(11), NOW);

        // One day at a 30-day time constant moves the score by about 0.023: a gradual slope, not a step.
        assertThat(day10 - day11).isPositive().isLessThan(0.03);
    }

    @Test
    void scoreStaysWithinZeroToOne() {
        assertThat(FreshnessScorer.score(NOW.minusYears(10), NOW)).isBetween(0.0, 1.0);
        assertThat(FreshnessScorer.score(NOW.plusDays(5), NOW)).isEqualTo(1.0);
    }

    @Test
    void missingTimestampGetsNeutralValue() {
        assertThat(FreshnessScorer.score(null, NOW)).isEqualTo(0.5);
        assertThat(FreshnessScorer.score(NOW, null)).isEqualTo(0.5);
    }

    @Test
    void sameTimestampAlwaysGivesSameScore() {
        LocalDateTime created = NOW.minusDays(7).minusHours(3);

        assertThat(FreshnessScorer.score(created, NOW)).isEqualTo(FreshnessScorer.score(created, NOW));
    }
}
