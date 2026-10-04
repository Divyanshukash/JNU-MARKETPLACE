package com.jnu.marketplace.ai.embedding;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Per-listing embedding and indexing bookkeeping, stored in MongoDB collection "listing_embeddings".
 *
 * Holds metadata only. The vector itself lives in the vector store, not in MongoDB.
 *
 * Lifecycle: PENDING -> EMBEDDED -> INDEXED. FAILED can happen at either step. A listing with
 * no state document, or one set back to PENDING, is not in the vector store.
 */
@Document(collection = "listing_embeddings")
@Getter
@Setter
@NoArgsConstructor
public class ListingEmbeddingState {

    public enum Status {
        /** Not indexed. Content changed, was never embedded, or removal is in progress. */
        PENDING,
        /** Embedding generated for the stored content hash and model. Not yet confirmed in the vector store. */
        EMBEDDED,
        /** Confirmed in the vector store for the stored content hash, model and payload. */
        INDEXED,
        /** The last attempt failed. The error is in lastError and the next attempt starts over. */
        FAILED
    }

    /** Same value as the listing's MongoDB _id. */
    @Id
    private String listingId;

    @Indexed
    private Status status;

    /** SHA-256 of the text the embedding was generated from. */
    private String contentHash;

    /** Model that produced the embedding. A change here forces regeneration. */
    private String embeddingModel;

    /** SHA-256 of the payload last written to the vector store. A change here triggers a payload update only. */
    private String payloadHash;

    /** Point id in the vector store. Set only after the store confirmed the write. */
    private String vectorId;

    private LocalDateTime embeddedAt;

    private LocalDateTime indexedAt;

    /** Last failure message, cleared on success. */
    private String lastError;

    private LocalDateTime updatedAt;

    public ListingEmbeddingState(String listingId) {
        this.listingId = listingId;
    }
}
