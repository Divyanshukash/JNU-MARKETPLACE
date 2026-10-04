package com.jnu.marketplace.ai.embedding;

import com.jnu.marketplace.model.Listing;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ListingTextAssemblerTest {

    private final ListingTextAssembler assembler = new ListingTextAssembler();

    @Test
    void assemblesAllPopulatedFieldsInFixedOrder() {
        Listing listing = new Listing();
        listing.setTitle("  Engineering   Textbook ");
        listing.setDescription("Data structures and algorithms,\nlightly used.");
        listing.setCategory(Listing.Category.BOOKS);
        listing.setSubcategory("Computer Science");
        listing.setTags(orderedTags("textbook", "algorithms"));
        listing.setBrand("Pearson");
        listing.setModel("3rd Edition");
        listing.setCondition(Listing.Condition.LIKE_NEW);
        listing.setLifeOfItem("1 year");
        listing.setDonation(false);

        assertThat(assembler.assemble(listing)).isEqualTo(String.join("\n",
                "Title: Engineering Textbook",
                "Description: Data structures and algorithms, lightly used.",
                "Category: Books",
                "Subcategory: Computer Science",
                "Tags: algorithms, textbook",
                "Brand: Pearson",
                "Model: 3rd Edition",
                "Condition: Like New",
                "Life of item: 1 year",
                "Donation: no"));
    }

    @Test
    void skipsNullAndBlankOptionalFieldsWithoutFailing() {
        Listing listing = new Listing();
        listing.setTitle("Desk Lamp");
        listing.setDescription("   ");
        listing.setCategory(null);
        listing.setSubcategory("");
        listing.setTags(new LinkedHashSet<>(Set.of("  ", "")));
        listing.setBrand(null);
        listing.setModel(null);
        listing.setCondition(null);
        listing.setLifeOfItem(null);
        listing.setDonation(true);

        assertThat(assembler.assemble(listing)).isEqualTo(String.join("\n",
                "Title: Desk Lamp",
                "Donation: yes"));
    }

    @Test
    void emptyListingUsesEntityDefaultsForConditionAndDonation() {
        // Listing defaults condition to NEW and donation to false, so the assembler reports those values.
        assertThat(assembler.assemble(new Listing())).isEqualTo("Condition: New\nDonation: no");
    }

    @Test
    void sameListingStateProducesIdenticalText() {
        Listing first = listingWithTags(orderedTags("b", "a", "c"));
        Listing second = listingWithTags(orderedTags("c", "a", "b"));

        String firstText = assembler.assemble(first);
        assertThat(assembler.assemble(first)).isEqualTo(firstText);
        // Tag iteration order must not change the output.
        assertThat(assembler.assemble(second)).isEqualTo(firstText);
    }

    @Test
    void excludesSellerAndPrivateFields() {
        Listing listing = listingWithTags(orderedTags("bike"));
        listing.setSellerId("seller-123");
        listing.setSellerName("Private Person");
        listing.setPickupLocation("Hostel 7, Room 214");
        listing.setPrice(new java.math.BigDecimal("999.00"));

        String text = assembler.assemble(listing);

        assertThat(text).doesNotContain("seller-123", "Private Person", "Hostel 7", "Room 214", "999");
        assertThat(text).doesNotContainIgnoringCase("email", "password");
    }

    private Listing listingWithTags(Set<String> tags) {
        Listing listing = new Listing();
        listing.setTitle("Mountain Bike");
        listing.setDescription("Good condition, 21 gears.");
        listing.setCategory(Listing.Category.VEHICLES);
        listing.setCondition(Listing.Condition.GOOD);
        listing.setTags(tags);
        return listing;
    }

    private static Set<String> orderedTags(String... tags) {
        return new LinkedHashSet<>(java.util.List.of(tags));
    }
}
