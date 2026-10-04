package com.jnu.marketplace.ai.vector;

import com.jnu.marketplace.ai.AiUnavailableException;

import java.util.List;

/**
 * Default gateway used when AI is disabled. It never contacts Qdrant,
 * so the application runs without a vector database.
 */
public class DisabledVectorStoreGateway implements VectorStoreGateway {

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String upsert(VectorPoint point) {
        throw new AiUnavailableException("Vector store is disabled");
    }

    @Override
    public void updatePayload(String listingId, VectorPayload payload) {
        throw new AiUnavailableException("Vector store is disabled");
    }

    @Override
    public void delete(String listingId) {
        throw new AiUnavailableException("Vector store is disabled");
    }

    @Override
    public List<VectorMatch> search(float[] queryVector, int limit) {
        throw new AiUnavailableException("Vector store is disabled");
    }
}
