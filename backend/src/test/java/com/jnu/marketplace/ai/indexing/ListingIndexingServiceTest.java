package com.jnu.marketplace.ai.indexing;

import com.jnu.marketplace.ai.AiUnavailableException;
import com.jnu.marketplace.ai.embedding.DisabledEmbeddingProvider;
import com.jnu.marketplace.ai.embedding.EmbeddingProvider;
import com.jnu.marketplace.ai.embedding.ListingEmbeddingService;
import com.jnu.marketplace.ai.embedding.ListingEmbeddingState;
import com.jnu.marketplace.ai.embedding.ListingEmbeddingStateRepository;
import com.jnu.marketplace.ai.embedding.ListingTextAssembler;
import com.jnu.marketplace.ai.indexing.ListingIndexingService.IndexResult;
import com.jnu.marketplace.ai.indexing.ListingIndexingService.Outcome;
import com.jnu.marketplace.ai.vector.VectorPoint;
import com.jnu.marketplace.ai.vector.VectorStoreGateway;
import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * State-machine tests for indexing. Mocks stand in for the embedding provider, the vector store and
 * the listing repository. The embedding state is kept in an in-memory map, so tests can inspect the
 * state after each step. No MongoDB, Qdrant or API key is involved.
 */
class ListingIndexingServiceTest {

    private static final float[] VECTOR = {0.1f, 0.2f, 0.3f};

    private EmbeddingProvider provider;
    private VectorStoreGateway vectors;
    private ListingRepository listings;
    private Map<String, ListingEmbeddingState> store;
    private ListingIndexingService service;
    private Listing listing;

    @BeforeEach
    void setUp() {
        provider = mock(EmbeddingProvider.class);
        when(provider.isAvailable()).thenReturn(true);
        when(provider.dimension()).thenReturn(3);
        when(provider.modelId()).thenReturn("test-model");
        when(provider.embed(anyString())).thenReturn(VECTOR);

        vectors = mock(VectorStoreGateway.class);
        when(vectors.isAvailable()).thenReturn(true);
        // Fake store: the point id equals the listing id. The real gateway returns a deterministic UUID.
        when(vectors.upsert(any())).thenAnswer(inv -> ((VectorPoint) inv.getArgument(0)).listingId());

        listings = mock(ListingRepository.class);
        store = new HashMap<>();
        ListingEmbeddingStateRepository stateRepository = fakeStateRepository();

        service = new ListingIndexingService(provider, vectors, new ListingTextAssembler(),
                new ListingEmbeddingService(provider, new ListingTextAssembler(), stateRepository), listings);

        listing = activeListing();
        when(listings.findById("listing-1")).thenReturn(Optional.of(listing));
    }

    @Test
    void successfulSyncEmbedsUpsertsAndMarksIndexed() {
        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.INDEXED);
        ListingEmbeddingState state = store.get("listing-1");
        assertThat(state.getStatus()).isEqualTo(ListingEmbeddingState.Status.INDEXED);
        assertThat(state.getVectorId()).isEqualTo("listing-1");
        assertThat(state.getEmbeddingModel()).isEqualTo("test-model");
        assertThat(state.getContentHash()).isNotBlank();
        assertThat(state.getPayloadHash()).isNotBlank();
        assertThat(state.getIndexedAt()).isNotNull();
        assertThat(state.getLastError()).isNull();

        ArgumentCaptor<VectorPoint> sent = ArgumentCaptor.forClass(VectorPoint.class);
        verify(vectors).upsert(sent.capture());
        assertThat(sent.getValue().vector()).containsExactly(VECTOR);
        assertThat(sent.getValue().payload().category()).isEqualTo("VEHICLES");
    }

    @Test
    void embeddingFailureRecordsFailedAndNeverTouchesVectorStore() {
        when(provider.embed(anyString())).thenThrow(new AiUnavailableException("timeout after 3s"));

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.FAILED);
        assertThat(store.get("listing-1").getStatus()).isEqualTo(ListingEmbeddingState.Status.FAILED);
        assertThat(store.get("listing-1").getLastError()).isEqualTo("timeout after 3s");
        verify(vectors, never()).upsert(any());
    }

    @Test
    void qdrantFailureRecordsFailedAndNeverMarksIndexed() {
        doThrow(new AiUnavailableException("Qdrant upsert failed")).when(vectors).upsert(any());

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.FAILED);
        ListingEmbeddingState state = store.get("listing-1");
        assertThat(state.getStatus()).isEqualTo(ListingEmbeddingState.Status.FAILED);
        assertThat(state.getContentHash()).isNotBlank();
        assertThat(state.getVectorId()).isNull();
        assertThat(state.getLastError()).contains("Qdrant upsert failed");
    }

    @Test
    void dimensionMismatchRecordsFailedAndNeverUpserts() {
        when(provider.embed(anyString())).thenReturn(new float[]{0.1f});

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.FAILED);
        assertThat(store.get("listing-1").getStatus()).isEqualTo(ListingEmbeddingState.Status.FAILED);
        verify(vectors, never()).upsert(any());
    }

    @Test
    void unchangedAlreadyIndexedListingDoesNoEmbeddingOrVectorWork() {
        service.sync("listing-1");
        clearInvocations(provider, vectors);

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.UNCHANGED);
        verify(provider, never()).embed(anyString());
        verify(vectors, never()).upsert(any());
        verify(vectors, never()).updatePayload(anyString(), any());
    }

    @Test
    void changedContentRegeneratesAndReindexesTheSamePoint() {
        service.sync("listing-1");
        listing.setDescription("Completely new description with different searchable words.");
        clearInvocations(provider, vectors);

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.INDEXED);
        verify(provider).embed(anyString());
        ArgumentCaptor<VectorPoint> sent = ArgumentCaptor.forClass(VectorPoint.class);
        verify(vectors).upsert(sent.capture());
        assertThat(sent.getValue().listingId()).isEqualTo("listing-1");
    }

    @Test
    void changedEmbeddingModelRegeneratesAndReindexes() {
        service.sync("listing-1");
        when(provider.modelId()).thenReturn("test-model-v2");
        clearInvocations(provider, vectors);

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.INDEXED);
        verify(provider).embed(anyString());
        verify(vectors).upsert(any());
        assertThat(store.get("listing-1").getEmbeddingModel()).isEqualTo("test-model-v2");
    }

    @Test
    void payloadOnlyChangeUpdatesPayloadWithoutEmbedding() {
        service.sync("listing-1");
        listing.setPrice(new BigDecimal("25.00"));
        clearInvocations(provider, vectors);

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.PAYLOAD_UPDATED);
        verify(provider, never()).embed(anyString());
        verify(vectors, never()).upsert(any());
        verify(vectors).updatePayload(eq("listing-1"), any());
        assertThat(store.get("listing-1").getStatus()).isEqualTo(ListingEmbeddingState.Status.INDEXED);
    }

    @Test
    void retryingAFailedListingRecoversIt() {
        doThrow(new AiUnavailableException("Qdrant down"))
                .doAnswer(inv -> ((VectorPoint) inv.getArgument(0)).listingId())
                .when(vectors).upsert(any());

        assertThat(service.sync("listing-1").outcome()).isEqualTo(Outcome.FAILED);
        assertThat(store.get("listing-1").getStatus()).isEqualTo(ListingEmbeddingState.Status.FAILED);

        IndexResult retry = service.sync("listing-1");

        assertThat(retry.outcome()).isEqualTo(Outcome.INDEXED);
        assertThat(store.get("listing-1").getStatus()).isEqualTo(ListingEmbeddingState.Status.INDEXED);
        assertThat(store.get("listing-1").getLastError()).isNull();
        verify(provider, times(2)).embed(anyString());
    }

    @Test
    void deactivatedListingRemovesVectorAndClearsState() {
        service.sync("listing-1");
        listing.setStatus(Listing.ListingStatus.SOLD);

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.REMOVED);
        verify(vectors).delete("listing-1");
        assertThat(store).doesNotContainKey("listing-1");
    }

    @Test
    void deletedListingRemovesItsVector() {
        when(listings.findById("gone")).thenReturn(Optional.empty());
        ListingEmbeddingState indexed = new ListingEmbeddingState("gone");
        indexed.setStatus(ListingEmbeddingState.Status.INDEXED);
        store.put("gone", indexed);

        IndexResult result = service.sync("gone");

        assertThat(result.outcome()).isEqualTo(Outcome.REMOVED);
        verify(vectors).delete("gone");
        assertThat(store).doesNotContainKey("gone");
    }

    @Test
    void failedRemovalKeepsStatePendingSoItIsNotMistakenForIndexed() {
        service.sync("listing-1");
        listing.setStatus(Listing.ListingStatus.SOLD);
        doThrow(new AiUnavailableException("Qdrant down")).when(vectors).delete("listing-1");

        IndexResult result = service.sync("listing-1");

        assertThat(result.outcome()).isEqualTo(Outcome.FAILED);
        assertThat(store.get("listing-1").getStatus()).isEqualTo(ListingEmbeddingState.Status.PENDING);
    }

    @Test
    void disabledEmbeddingProviderReturnsUnavailableWithoutDatabaseOrVectorWork() {
        ListingIndexingService disabled = new ListingIndexingService(
                new DisabledEmbeddingProvider(), vectors, new ListingTextAssembler(),
                new ListingEmbeddingService(new DisabledEmbeddingProvider(), new ListingTextAssembler(),
                        fakeStateRepository()),
                listings);

        assertThat(disabled.sync("listing-1").outcome()).isEqualTo(Outcome.UNAVAILABLE);
        verifyNoInteractions(listings, vectors);
        assertThat(store).isEmpty();
    }

    @Test
    void disabledVectorStoreReturnsUnavailableWithoutEmbedding() {
        when(vectors.isAvailable()).thenReturn(false);

        assertThat(service.sync("listing-1").outcome()).isEqualTo(Outcome.UNAVAILABLE);
        verify(provider, never()).embed(anyString());
        verifyNoInteractions(listings);
    }

    private ListingEmbeddingStateRepository fakeStateRepository() {
        ListingEmbeddingStateRepository repository = mock(ListingEmbeddingStateRepository.class);
        when(repository.findById(anyString()))
                .thenAnswer(inv -> Optional.ofNullable(store.get(inv.<String>getArgument(0))));
        when(repository.save(any())).thenAnswer(inv -> {
            ListingEmbeddingState state = inv.getArgument(0);
            store.put(state.getListingId(), state);
            return state;
        });
        doAnswer(inv -> {
            store.remove(inv.<String>getArgument(0));
            return null;
        }).when(repository).deleteById(anyString());
        return repository;
    }

    private static Listing activeListing() {
        Listing listing = new Listing();
        listing.setId("listing-1");
        listing.setTitle("Mountain Bike");
        listing.setDescription("Good condition, 21 gears.");
        listing.setCategory(Listing.Category.VEHICLES);
        listing.setCondition(Listing.Condition.GOOD);
        listing.setPrice(new BigDecimal("20.00"));
        listing.setSellerId("seller-1");
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        return listing;
    }
}
