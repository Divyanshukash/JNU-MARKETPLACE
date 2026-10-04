package com.jnu.marketplace.service;

import com.jnu.marketplace.dto.ListingRequest;
import com.jnu.marketplace.event.ListingChangedEvent;
import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.model.User;
import com.jnu.marketplace.repository.ListingRepository;
import com.jnu.marketplace.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Checks that marketplace writes publish a ListingChangedEvent, and that the saved listing id is used.
 * The indexing reaction itself is covered by the indexing tests.
 */
class ListingServiceEventTest {

    private static final String SELLER_EMAIL = "seller@jnu.ac.in";

    private ListingRepository listingRepository;
    private UserRepository userRepository;
    private ApplicationEventPublisher eventPublisher;
    private ListingService service;

    @BeforeEach
    void setUp() {
        listingRepository = mock(ListingRepository.class);
        userRepository = mock(UserRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new ListingService(listingRepository, userRepository, mock(MongoTemplate.class), eventPublisher);

        User seller = mock(User.class);
        when(seller.getId()).thenReturn("seller-1");
        when(userRepository.findByEmail(SELLER_EMAIL)).thenReturn(Optional.of(seller));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(SELLER_EMAIL, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPublishesEventWithTheSavedListingId() {
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> {
            Listing saved = inv.getArgument(0);
            saved.setId("new-listing");
            return saved;
        });
        ListingRequest request = new ListingRequest("Mountain Bike", "Good condition, 21 gears.",
                new BigDecimal("500"), Listing.Category.VEHICLES);

        Listing created = service.createListing(request);

        assertThat(created.getId()).isEqualTo("new-listing");
        verify(eventPublisher).publishEvent(new ListingChangedEvent("new-listing"));
    }

    @Test
    void updatePublishesEvent() {
        Listing existing = ownedListing();
        when(listingRepository.findById("listing-1")).thenReturn(Optional.of(existing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));
        ListingRequest request = new ListingRequest("Mountain Bike Pro", "Upgraded, 24 gears now.",
                new BigDecimal("650"), Listing.Category.VEHICLES);

        service.updateListing("listing-1", request);

        verify(eventPublisher).publishEvent(new ListingChangedEvent("listing-1"));
    }

    @Test
    void statusChangePublishesEvent() {
        when(listingRepository.findById("listing-1")).thenReturn(Optional.of(ownedListing()));

        service.updateListingStatus("listing-1", Listing.ListingStatus.SOLD);

        verify(eventPublisher).publishEvent(new ListingChangedEvent("listing-1"));
    }

    @Test
    void deletePublishesEvent() {
        when(listingRepository.findById("listing-1")).thenReturn(Optional.of(ownedListing()));

        service.deleteListing("listing-1");

        verify(eventPublisher).publishEvent(new ListingChangedEvent("listing-1"));
    }

    private Listing ownedListing() {
        Listing listing = new Listing();
        listing.setId("listing-1");
        listing.setSellerId("seller-1");
        listing.setTitle("Mountain Bike");
        listing.setDescription("Good condition, 21 gears.");
        listing.setCategory(Listing.Category.VEHICLES);
        listing.setPrice(new BigDecimal("500"));
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        return listing;
    }
}
