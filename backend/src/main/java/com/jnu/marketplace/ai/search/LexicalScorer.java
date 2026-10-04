package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.model.Listing;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Deterministic keyword relevance of a listing for a query. Range [0, 1].
 *
 * Text is lowercased, Unicode-normalized, and split on anything that is not a letter or digit.
 *
 * 1. Exact phrase: if the whole query equals the whole title (after normalization), the score is 1.0.
 * 2. Otherwise, each distinct query token earns points for each field that contains it:
 *      title        3.0
 *      tags         2.0
 *      category     1.5  (category display name and subcategory)
 *      description  1.0
 *    The maximum for one token is 7.5, when all four fields contain it.
 * 3. score = sum(points over tokens) / (number of query tokens * 7.5)
 *
 * Title outranks description because it is the strongest signal of what a seller is offering.
 * Dividing by the query length keeps longer queries from scoring higher just for being longer.
 */
public final class LexicalScorer {

    static final double TITLE_WEIGHT = 3.0;
    static final double TAG_WEIGHT = 2.0;
    static final double CATEGORY_WEIGHT = 1.5;
    static final double DESCRIPTION_WEIGHT = 1.0;
    static final double MAX_TOKEN_SCORE = TITLE_WEIGHT + TAG_WEIGHT + CATEGORY_WEIGHT + DESCRIPTION_WEIGHT;

    private LexicalScorer() {}

    public static double score(String query, Listing listing) {
        List<String> queryTokens = tokens(query);
        if (queryTokens.isEmpty() || listing == null) return 0.0;

        if (!queryTokens.isEmpty() && String.join(" ", queryTokens).equals(phrase(listing.getTitle()))) {
            return 1.0;
        }

        Set<String> title = new HashSet<>(tokens(listing.getTitle()));
        Set<String> tags = new HashSet<>();
        if (listing.getTags() != null) {
            for (String tag : listing.getTags()) tags.addAll(tokens(tag));
        }
        Set<String> category = new HashSet<>(tokens(listing.getCategory() == null ? null : listing.getCategory().getDisplayName()));
        category.addAll(tokens(listing.getSubcategory()));
        Set<String> description = new HashSet<>(tokens(listing.getDescription()));

        Set<String> distinctQueryTokens = new LinkedHashSet<>(queryTokens);
        double points = 0.0;
        for (String token : distinctQueryTokens) {
            if (title.contains(token)) points += TITLE_WEIGHT;
            if (tags.contains(token)) points += TAG_WEIGHT;
            if (category.contains(token)) points += CATEGORY_WEIGHT;
            if (description.contains(token)) points += DESCRIPTION_WEIGHT;
        }
        return ScoreRange.clamp01(points / (distinctQueryTokens.size() * MAX_TOKEN_SCORE));
    }

    /** Tokens in order, duplicates kept. Used for exact-phrase comparison. */
    static List<String> tokens(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null) return tokens;
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
        for (String part : normalized.split("[^\\p{L}\\p{N}]+")) {
            if (!part.isEmpty()) tokens.add(part);
        }
        return tokens;
    }

    private static String phrase(String text) {
        return String.join(" ", tokens(text));
    }
}
