package com.jnu.marketplace.ai.embedding;

/**
 * Turns text into a dense vector. Implementations wrap an external embedding
 * model (for example a hosted API). Callers never depend on a specific vendor.
 */
public interface EmbeddingProvider {

    /** True when this provider is enabled and configured to produce embeddings. */
    boolean isAvailable();

    /** Length of every vector this provider returns. Must match the vector store collection. */
    int dimension();

    /** Identifier of the model that produced the vectors. Stored with each embedding so a model change forces regeneration. */
    String modelId();

    /**
     * Embeds the given text.
     *
     * @throws com.jnu.marketplace.ai.AiUnavailableException if the provider is disabled or fails
     */
    float[] embed(String text);
}
