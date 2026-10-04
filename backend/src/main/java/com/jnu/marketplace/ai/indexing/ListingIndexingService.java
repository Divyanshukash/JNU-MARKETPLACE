package com.jnu.marketplace.ai.indexing;

import com.jnu.marketplace.ai.embedding.ContentHasher;
import com.jnu.marketplace.ai.embedding.EmbeddingProvider;
import com.jnu.marketplace.ai.embedding.ListingEmbeddingService;
import com.jnu.marketplace.ai.embedding.ListingEmbeddingService.EmbeddingResult;
import com.jnu.marketplace.ai.embedding.ListingEmbeddingState;
import com.jnu.marketplace.ai.embedding.ListingTextAssembler;
import com.jnu.marketplace.ai.vector.VectorPayload;
import com.jnu.marketplace.ai.vector.VectorPoint;
import com.jnu.marketplace.ai.vector.VectorStoreGateway;
import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.repository.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Brings one listing's vector in line with MongoDB. Repeating it is always safe.
 *
 * sync(listingId) reloads the listing from MongoDB, the source of truth, and then:
 * - listing missing, or not ACTIVE: remove its vector.
 * - listing ACTIVE and already INDEXED with the same text hash, model and payload hash: do nothing.
 * - listing ACTIVE and INDEXED with the same text and model, but a changed payload: update the payload only.
 *   No embedding is generated.
 * - anything else (new, changed text, changed model, or FAILED): embed, upsert, then mark INDEXED.
 *
 * INDEXED is set only after the vector store confirms the write. A failed step records FAILED
 * with the hash it was working on, and the next sync starts that step over.
 *
 * Provider and vector store are checked first. When AI is disabled, sync returns UNAVAILABLE
 * without any MongoDB or network work.
 */
@Service
public class ListingIndexingService {

    private static final Logger log = LoggerFactory.getLogger(ListingIndexingService.class);

    public enum Outcome {
        /** Vector embedded and confirmed in the vector store. */
        INDEXED,
        /** Vector unchanged, payload updated in place. */
        PAYLOAD_UPDATED,
        /** Already indexed with identical content and payload. No work done. */
        UNCHANGED,
        /** Vector removed, or the listing was never indexed. */
        REMOVED,
        /** AI is disabled or not configured. */
        UNAVAILABLE,
        /** A step failed. The state records FAILED and a later sync retries. */
        FAILED
    }

    /** Result of one sync. error is set only for FAILED. */
    public record IndexResult(Outcome outcome, String error) {}

    private final EmbeddingProvider embeddingProvider;
    private final VectorStoreGateway vectorStore;
    private final ListingTextAssembler textAssembler;
    private final ListingEmbeddingService embeddings;
    private final ListingRepository listingRepository;

    public ListingIndexingService(EmbeddingProvider embeddingProvider,
                                  VectorStoreGateway vectorStore,
                                  ListingTextAssembler textAssembler,
                                  ListingEmbeddingService embeddings,
                                  ListingRepository listingRepository) {
        this.embeddingProvider = embeddingProvider;
        this.vectorStore = vectorStore;
        this.textAssembler = textAssembler;
        this.embeddings = embeddings;
        this.listingRepository = listingRepository;
    }

    public IndexResult sync(String listingId) {
        if (!embeddingProvider.isAvailable() || !vectorStore.isAvailable()) {
            return new IndexResult(Outcome.UNAVAILABLE, "AI is disabled or not configured");
        }

        Optional<Listing> listing = listingRepository.findById(listingId);
        if (listing.isEmpty() || listing.get().getStatus() != Listing.ListingStatus.ACTIVE) {
            return remove(listingId);
        }
        return index(listing.get());
    }

    private IndexResult index(Listing listing) {
        String listingId = listing.getId();
        String contentHash = ContentHasher.sha256(textAssembler.assemble(listing));
        VectorPayload payload = VectorPayload.from(listing);
        String payloadHash = ContentHasher.sha256(payload.toString());

        Optional<ListingEmbeddingState> state = embeddings.findState(listingId);
        if (state.isPresent() && isIndexedWith(state.get(), contentHash)) {
            if (payloadHash.equals(state.get().getPayloadHash())) {
                return new IndexResult(Outcome.UNCHANGED, null);
            }
            return updatePayloadOnly(listingId, contentHash, payload, payloadHash, state.get().getVectorId());
        }

        EmbeddingResult embedding = embeddings.generateEmbedding(listing);
        if (embedding.outcome() == EmbeddingResult.Outcome.UNAVAILABLE) {
            return new IndexResult(Outcome.UNAVAILABLE, embedding.error());
        }
        if (embedding.outcome() != EmbeddingResult.Outcome.GENERATED) {
            // generateEmbedding has already recorded FAILED.
            log.warn("Indexing stopped for listing {} at embedding step: {}", listingId, embedding.error());
            return new IndexResult(Outcome.FAILED, embedding.error());
        }

        String vectorId;
        try {
            vectorId = vectorStore.upsert(new VectorPoint(listingId, embedding.vector(), payload));
        } catch (RuntimeException e) {
            String message = messageOf(e);
            log.warn("Vector upsert failed for listing {}: {}", listingId, message);
            embeddings.markFailed(listingId, contentHash, message);
            return new IndexResult(Outcome.FAILED, message);
        }

        embeddings.markIndexed(listingId, contentHash, vectorId, payloadHash);
        log.info("Indexed listing {}", listingId);
        return new IndexResult(Outcome.INDEXED, null);
    }

    private IndexResult updatePayloadOnly(String listingId, String contentHash, VectorPayload payload,
                                          String payloadHash, String vectorId) {
        try {
            vectorStore.updatePayload(listingId, payload);
        } catch (RuntimeException e) {
            String message = messageOf(e);
            log.warn("Payload update failed for listing {}: {}", listingId, message);
            embeddings.markFailed(listingId, contentHash, message);
            return new IndexResult(Outcome.FAILED, message);
        }
        embeddings.markIndexed(listingId, contentHash, vectorId, payloadHash);
        return new IndexResult(Outcome.PAYLOAD_UPDATED, null);
    }

    /**
     * Removes the vector. The state is marked PENDING first, so an interrupted removal is not mistaken
     * for an indexed listing later. The state document is deleted only after the vector is gone.
     */
    private IndexResult remove(String listingId) {
        try {
            embeddings.markPending(listingId);
            vectorStore.delete(listingId);
            embeddings.clearState(listingId);
            return new IndexResult(Outcome.REMOVED, null);
        } catch (RuntimeException e) {
            String message = messageOf(e);
            log.warn("Vector removal failed for listing {}: {}", listingId, message);
            return new IndexResult(Outcome.FAILED, message);
        }
    }

    private boolean isIndexedWith(ListingEmbeddingState state, String contentHash) {
        return state.getStatus() == ListingEmbeddingState.Status.INDEXED
                && contentHash.equals(state.getContentHash())
                && embeddingProvider.modelId().equals(state.getEmbeddingModel());
    }

    private static String messageOf(RuntimeException e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
