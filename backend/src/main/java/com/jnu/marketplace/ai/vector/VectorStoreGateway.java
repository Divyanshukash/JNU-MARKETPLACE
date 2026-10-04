package com.jnu.marketplace.ai.vector;

import java.util.List;

/**
 * Gateway to the vector database (Qdrant in production).
 *
 * This is the only boundary the application uses. Vector-store types never leave
 * the implementation. MongoDB remains the source of truth. The store holds vectors
 * plus small filter metadata, and search returns listing ids, which the caller then
 * loads from MongoDB.
 *
 * Write methods throw AiUnavailableException when the store is disabled or the call
 * fails. Callers record that outcome and never treat it as a marketplace error.
 */
public interface VectorStoreGateway {

    /** True when vector operations are enabled. This does not prove the store is reachable. */
    boolean isAvailable();

    /**
     * Inserts or replaces the point for a listing. Returns the vector id only after the
     * store has confirmed the write. Repeating the call with the same listing id replaces
     * the point and never creates a duplicate.
     */
    String upsert(VectorPoint point);

    /** Replaces only the payload of an existing listing point. The vector is left unchanged. */
    void updatePayload(String listingId, VectorPayload payload);

    /** Removes the listing's point. Removing a point that does not exist is not an error. */
    void delete(String listingId);

    /** Vector search. Not implemented yet, reserved for the search phase. */
    List<VectorMatch> search(float[] queryVector, int limit);

    /** One search hit: the MongoDB listing id and its similarity score. */
    record VectorMatch(String listingId, double score) {}
}
