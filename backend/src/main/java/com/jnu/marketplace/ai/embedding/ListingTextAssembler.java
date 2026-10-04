package com.jnu.marketplace.ai.embedding;

import com.jnu.marketplace.model.Listing;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Builds the text that gets embedded for a listing.
 *
 * Only searchable, non-personal listing content is included. Seller identity,
 * seller id, pickup location, prices and timestamps are deliberately left out.
 *
 * Output is deterministic: the same listing content always produces the same
 * string. Each present field becomes one "Label: value" line in a fixed order.
 * Null or blank fields are skipped. Tags are sorted, so set iteration order
 * never changes the result.
 */
@Component
public class ListingTextAssembler {

    public String assemble(Listing listing) {
        Objects.requireNonNull(listing, "listing must not be null");

        List<String> lines = new ArrayList<>();
        addLine(lines, "Title", listing.getTitle());
        addLine(lines, "Description", listing.getDescription());
        addLine(lines, "Category", listing.getCategory() == null ? null : listing.getCategory().getDisplayName());
        addLine(lines, "Subcategory", listing.getSubcategory());
        addLine(lines, "Tags", joinTags(listing.getTags()));
        addLine(lines, "Brand", listing.getBrand());
        addLine(lines, "Model", listing.getModel());
        addLine(lines, "Condition", listing.getCondition() == null ? null : listing.getCondition().getDisplayName());
        addLine(lines, "Life of item", listing.getLifeOfItem());
        // Always present, so donation and non-donation listings never produce identical text.
        lines.add("Donation: " + (listing.isDonation() ? "yes" : "no"));

        return String.join("\n", lines);
    }

    private void addLine(List<String> lines, String label, String value) {
        String normalized = normalize(value);
        if (normalized != null) {
            lines.add(label + ": " + normalized);
        }
    }

    private String joinTags(Iterable<String> tags) {
        if (tags == null) return null;
        TreeSet<String> sorted = new TreeSet<>();
        for (String tag : tags) {
            String normalized = normalize(tag);
            if (normalized != null) sorted.add(normalized);
        }
        return sorted.isEmpty() ? null : String.join(", ", sorted);
    }

    /**
     * Unicode NFC, trimmed, with internal whitespace runs collapsed to one space.
     * Returns null for null or blank input so callers can skip the field.
     */
    private String normalize(String value) {
        if (value == null) return null;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFC)
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
