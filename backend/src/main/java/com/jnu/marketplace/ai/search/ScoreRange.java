package com.jnu.marketplace.ai.search;

/** Keeps every ranking component inside [0, 1]. NaN maps to 0 so one bad value cannot poison the sort. */
public final class ScoreRange {

    private ScoreRange() {}

    public static double clamp01(double value) {
        if (Double.isNaN(value)) return 0.0;
        return Math.max(0.0, Math.min(1.0, value));
    }
}
