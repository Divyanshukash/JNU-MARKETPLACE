package com.jnu.marketplace.ai.embedding;

import com.jnu.marketplace.ai.AiUnavailableException;

/**
 * Default provider used when AI is disabled or no real provider is configured.
 * It never produces vectors, so semantic features report unavailable and the
 * marketplace keeps using its conventional search.
 */
public class DisabledEmbeddingProvider implements EmbeddingProvider {

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public int dimension() {
        return 0;
    }

    @Override
    public String modelId() {
        return "none";
    }

    @Override
    public float[] embed(String text) {
        throw new AiUnavailableException("Embedding provider is disabled");
    }
}
