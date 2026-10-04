package com.jnu.marketplace.ai.embedding;

import com.jnu.marketplace.model.Listing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Generates listing embeddings and owns the embedding state in MongoDB.
 *
 * listing -> ListingTextAssembler -> SHA-256 -> EmbeddingProvider -> result, recorded in ListingEmbeddingState.
 *
 * Rules:
 * - Provider disabled: return UNAVAILABLE and write nothing to MongoDB.
 * - embedListing skips the provider when the stored state already matches (EMBEDDED or INDEXED, same hash and model).
 * - generateEmbedding always calls the provider. The indexing flow uses it, because it needs the vector.
 * - Provider and state-store failures never propagate. Provider failures become FAILED results.
 *   State-write failures are logged, because the outcome is still returned to the caller.
 *
 * Write methods come in two kinds. Tolerant ones (updateState) log MongoDB errors. Strict ones
 * (markPending, clearState) throw, because the vector store must not diverge from the recorded state.
 */
@Service
public class ListingEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(ListingEmbeddingService.class);

    private final EmbeddingProvider embeddingProvider;
    private final ListingTextAssembler textAssembler;
    private final ListingEmbeddingStateRepository stateRepository;

    public ListingEmbeddingService(EmbeddingProvider embeddingProvider,
                                   ListingTextAssembler textAssembler,
                                   ListingEmbeddingStateRepository stateRepository) {
        this.embeddingProvider = embeddingProvider;
        this.textAssembler = textAssembler;
        this.stateRepository = stateRepository;
    }

    /** Generates an embedding only when the stored state does not already match the current content and model. */
    public EmbeddingResult embedListing(Listing listing) {
        if (!embeddingProvider.isAvailable()) {
            return EmbeddingResult.unavailable();
        }
        if (listing == null || listing.getId() == null) {
            return EmbeddingResult.failed(null, "Listing must be saved before it can be embedded");
        }
        String contentHash = ContentHasher.sha256(textAssembler.assemble(listing));
        if (isUpToDate(listing.getId(), contentHash)) {
            return EmbeddingResult.unchanged(contentHash);
        }
        return generateEmbedding(listing);
    }

    /** Always calls the provider, then records EMBEDDED on success or FAILED on error. */
    public EmbeddingResult generateEmbedding(Listing listing) {
        if (!embeddingProvider.isAvailable()) {
            return EmbeddingResult.unavailable();
        }
        if (listing == null || listing.getId() == null) {
            return EmbeddingResult.failed(null, "Listing must be saved before it can be embedded");
        }

        String listingId = listing.getId();
        String text = textAssembler.assemble(listing);
        String contentHash = ContentHasher.sha256(text);

        try {
            float[] vector = embeddingProvider.embed(text);
            if (vector == null || vector.length != embeddingProvider.dimension()) {
                throw new IllegalStateException("Embedding dimension mismatch: expected "
                        + embeddingProvider.dimension() + ", got " + (vector == null ? "null" : vector.length));
            }
            updateState(listingId, state -> {
                state.setStatus(ListingEmbeddingState.Status.EMBEDDED);
                state.setContentHash(contentHash);
                state.setEmbeddingModel(embeddingProvider.modelId());
                state.setEmbeddedAt(LocalDateTime.now());
                state.setLastError(null);
            });
            return EmbeddingResult.generated(contentHash, vector);
        } catch (RuntimeException e) {
            String message = messageOf(e);
            log.warn("Embedding failed for listing {}: {}", listingId, message);
            markFailed(listingId, contentHash, message);
            return EmbeddingResult.failed(contentHash, message);
        }
    }

    /** Records that the listing's vector is confirmed in the vector store. Called only after the store confirmed the write. */
    public void markIndexed(String listingId, String contentHash, String vectorId, String payloadHash) {
        updateState(listingId, state -> {
            state.setStatus(ListingEmbeddingState.Status.INDEXED);
            state.setContentHash(contentHash);
            state.setEmbeddingModel(embeddingProvider.modelId());
            state.setVectorId(vectorId);
            state.setPayloadHash(payloadHash);
            state.setIndexedAt(LocalDateTime.now());
            state.setLastError(null);
        });
    }

    /** Records a failed indexing step. The stored hash is kept, so the next attempt knows what it is retrying. */
    public void markFailed(String listingId, String contentHash, String error) {
        updateState(listingId, state -> {
            state.setStatus(ListingEmbeddingState.Status.FAILED);
            state.setContentHash(contentHash);
            state.setEmbeddingModel(embeddingProvider.modelId());
            state.setLastError(error);
        });
    }

    /** Records a new payload hash after a payload-only update succeeded. The vector is unchanged. */
    public void markPayloadSynced(String listingId, String payloadHash) {
        updateState(listingId, state -> state.setPayloadHash(payloadHash));
    }

    /**
     * Strict. Marks the listing PENDING before its vector is removed. If this write fails, the caller must not
     * delete the vector, because the recorded state would then disagree with the store.
     */
    public void markPending(String listingId) {
        ListingEmbeddingState state = stateRepository.findById(listingId)
                .orElseGet(() -> new ListingEmbeddingState(listingId));
        state.setStatus(ListingEmbeddingState.Status.PENDING);
        state.setUpdatedAt(LocalDateTime.now());
        stateRepository.save(state);
    }

    /** Strict. Removes the state document once the vector is gone, so the listing counts as not indexed. */
    public void clearState(String listingId) {
        stateRepository.deleteById(listingId);
    }

    /** Tolerant read. A failed read returns empty, so the caller does the full path and does not skip work. */
    public Optional<ListingEmbeddingState> findState(String listingId) {
        try {
            return stateRepository.findById(listingId);
        } catch (RuntimeException e) {
            log.warn("Could not read embedding state for listing {}: {}", listingId, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isUpToDate(String listingId, String contentHash) {
        return findState(listingId)
                .filter(state -> state.getStatus() == ListingEmbeddingState.Status.EMBEDDED
                        || state.getStatus() == ListingEmbeddingState.Status.INDEXED)
                .filter(state -> contentHash.equals(state.getContentHash()))
                .filter(state -> embeddingProvider.modelId().equals(state.getEmbeddingModel()))
                .isPresent();
    }

    /** Tolerant write. A MongoDB failure is logged, never thrown. */
    private void updateState(String listingId, Consumer<ListingEmbeddingState> change) {
        try {
            ListingEmbeddingState state = stateRepository.findById(listingId)
                    .orElseGet(() -> new ListingEmbeddingState(listingId));
            change.accept(state);
            state.setUpdatedAt(LocalDateTime.now());
            stateRepository.save(state);
        } catch (RuntimeException e) {
            log.warn("Could not save embedding state for listing {}: {}", listingId, e.getMessage());
        }
    }

    private static String messageOf(RuntimeException e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }

    /**
     * Outcome of one embedding attempt.
     *
     * @param outcome     what happened
     * @param contentHash hash of the assembled text, null when UNAVAILABLE
     * @param vector      the new embedding, only when GENERATED
     * @param error       failure message, only when FAILED
     */
    public record EmbeddingResult(Outcome outcome, String contentHash, float[] vector, String error) {

        public enum Outcome { GENERATED, UNCHANGED, UNAVAILABLE, FAILED }

        static EmbeddingResult generated(String hash, float[] vector) {
            return new EmbeddingResult(Outcome.GENERATED, hash, vector, null);
        }

        static EmbeddingResult unchanged(String hash) {
            return new EmbeddingResult(Outcome.UNCHANGED, hash, null, null);
        }

        static EmbeddingResult unavailable() {
            return new EmbeddingResult(Outcome.UNAVAILABLE, null, null, "AI is disabled or not configured");
        }

        static EmbeddingResult failed(String hash, String error) {
            return new EmbeddingResult(Outcome.FAILED, hash, null, error);
        }
    }
}
