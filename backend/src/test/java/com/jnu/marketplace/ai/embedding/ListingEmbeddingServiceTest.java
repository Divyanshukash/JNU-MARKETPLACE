package com.jnu.marketplace.ai.embedding;

import com.jnu.marketplace.ai.AiUnavailableException;
import com.jnu.marketplace.ai.embedding.ListingEmbeddingService.EmbeddingResult;
import com.jnu.marketplace.model.Listing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Service behavior with mocked provider and repository. No MongoDB, Qdrant or API key is involved.
 */
class ListingEmbeddingServiceTest {

    private static final float[] VECTOR = {0.1f, 0.2f, 0.3f};

    private EmbeddingProvider provider;
    private ListingEmbeddingStateRepository repository;
    private ListingEmbeddingService service;
    private Listing listing;

    @BeforeEach
    void setUp() {
        provider = mock(EmbeddingProvider.class);
        repository = mock(ListingEmbeddingStateRepository.class);
        when(provider.isAvailable()).thenReturn(true);
        when(provider.dimension()).thenReturn(3);
        when(provider.modelId()).thenReturn("test-model");
        service = new ListingEmbeddingService(provider, new ListingTextAssembler(), repository);

        listing = new Listing();
        listing.setId("listing-1");
        listing.setTitle("Mountain Bike");
        listing.setDescription("Good condition, 21 gears.");
        listing.setCategory(Listing.Category.VEHICLES);
    }

    @Test
    void disabledProviderReturnsUnavailableAndTouchesNothing() {
        ListingEmbeddingService disabled = new ListingEmbeddingService(
                new DisabledEmbeddingProvider(), new ListingTextAssembler(), repository);

        EmbeddingResult result = disabled.embedListing(listing);

        assertThat(result.outcome()).isEqualTo(EmbeddingResult.Outcome.UNAVAILABLE);
        assertThat(result.vector()).isNull();
        verifyNoInteractions(repository);
    }

    @Test
    void disabledProviderFailsControlledWhenCalledDirectly() {
        assertThatThrownBy(() -> new DisabledEmbeddingProvider().embed("anything"))
                .isInstanceOf(AiUnavailableException.class);
    }

    @Test
    void generatesEmbeddingAndRecordsEmbeddedState() {
        when(provider.isAvailable()).thenReturn(true);
        when(provider.embed(anyString())).thenReturn(VECTOR);
        when(repository.findById("listing-1")).thenReturn(Optional.empty());

        EmbeddingResult result = service.embedListing(listing);

        assertThat(result.outcome()).isEqualTo(EmbeddingResult.Outcome.GENERATED);
        assertThat(result.vector()).containsExactly(VECTOR);
        assertThat(result.contentHash()).isNotBlank();

        ListingEmbeddingState saved = capturedState();
        assertThat(saved.getListingId()).isEqualTo("listing-1");
        assertThat(saved.getStatus()).isEqualTo(ListingEmbeddingState.Status.EMBEDDED);
        assertThat(saved.getContentHash()).isEqualTo(result.contentHash());
        assertThat(saved.getEmbeddingModel()).isEqualTo("test-model");
        assertThat(saved.getLastError()).isNull();
        assertThat(saved.getEmbeddedAt()).isNotNull();
    }

    @Test
    void skipsProviderWhenContentAndModelAreUnchanged() {
        String hash = currentHash();
        ListingEmbeddingState existing = new ListingEmbeddingState("listing-1");
        existing.setStatus(ListingEmbeddingState.Status.EMBEDDED);
        existing.setContentHash(hash);
        existing.setEmbeddingModel("test-model");
        when(repository.findById("listing-1")).thenReturn(Optional.of(existing));

        EmbeddingResult result = service.embedListing(listing);

        assertThat(result.outcome()).isEqualTo(EmbeddingResult.Outcome.UNCHANGED);
        assertThat(result.vector()).isNull();
        verify(provider, never()).embed(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void regeneratesWhenSearchableContentChanged() {
        ListingEmbeddingState existing = new ListingEmbeddingState("listing-1");
        existing.setStatus(ListingEmbeddingState.Status.EMBEDDED);
        existing.setContentHash("stale-hash");
        existing.setEmbeddingModel("test-model");
        when(repository.findById("listing-1")).thenReturn(Optional.of(existing));
        when(provider.embed(anyString())).thenReturn(VECTOR);

        EmbeddingResult result = service.embedListing(listing);

        assertThat(result.outcome()).isEqualTo(EmbeddingResult.Outcome.GENERATED);
        verify(provider).embed(anyString());
    }

    @Test
    void regeneratesWhenEmbeddingModelChanged() {
        String hash = currentHash();
        ListingEmbeddingState existing = new ListingEmbeddingState("listing-1");
        existing.setStatus(ListingEmbeddingState.Status.EMBEDDED);
        existing.setContentHash(hash);
        existing.setEmbeddingModel("old-model");
        when(repository.findById("listing-1")).thenReturn(Optional.of(existing));
        when(provider.embed(anyString())).thenReturn(VECTOR);

        assertThat(service.embedListing(listing).outcome()).isEqualTo(EmbeddingResult.Outcome.GENERATED);
    }

    @Test
    void providerFailureIsRecordedAndNeverThrown() {
        when(repository.findById("listing-1")).thenReturn(Optional.empty());
        when(provider.embed(anyString())).thenThrow(new AiUnavailableException("timeout after 3s"));

        EmbeddingResult result = service.embedListing(listing);

        assertThat(result.outcome()).isEqualTo(EmbeddingResult.Outcome.FAILED);
        assertThat(result.error()).isEqualTo("timeout after 3s");
        ListingEmbeddingState saved = capturedState();
        assertThat(saved.getStatus()).isEqualTo(ListingEmbeddingState.Status.FAILED);
        assertThat(saved.getLastError()).isEqualTo("timeout after 3s");
    }

    @Test
    void dimensionMismatchIsTreatedAsFailure() {
        when(repository.findById("listing-1")).thenReturn(Optional.empty());
        when(provider.embed(anyString())).thenReturn(new float[]{0.1f});

        EmbeddingResult result = service.embedListing(listing);

        assertThat(result.outcome()).isEqualTo(EmbeddingResult.Outcome.FAILED);
        assertThat(capturedState().getStatus()).isEqualTo(ListingEmbeddingState.Status.FAILED);
    }

    @Test
    void stateStoreFailureDoesNotBreakEmbeddingResult() {
        when(repository.findById("listing-1")).thenThrow(new RuntimeException("mongo down"));
        when(repository.save(any())).thenThrow(new RuntimeException("mongo down"));
        when(provider.embed(anyString())).thenReturn(VECTOR);

        EmbeddingResult result = service.embedListing(listing);

        assertThat(result.outcome()).isEqualTo(EmbeddingResult.Outcome.GENERATED);
    }

    @Test
    void unsavedListingFailsWithoutCallingProvider() {
        listing.setId(null);

        assertThat(service.embedListing(listing).outcome()).isEqualTo(EmbeddingResult.Outcome.FAILED);
        verify(provider, never()).embed(anyString());
    }

    private String currentHash() {
        return ContentHasher.sha256(new ListingTextAssembler().assemble(listing));
    }

    private ListingEmbeddingState capturedState() {
        ArgumentCaptor<ListingEmbeddingState> captor = ArgumentCaptor.forClass(ListingEmbeddingState.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }
}
