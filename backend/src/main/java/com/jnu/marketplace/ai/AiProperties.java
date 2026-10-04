package com.jnu.marketplace.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Typed view of the {@code app.ai.*} configuration block.
 *
 * Values come from application.properties, which reads environment variables
 * with empty or safe defaults. No secret is ever stored in source code.
 */
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue Embedding embedding,
        @DefaultValue Qdrant qdrant,
        @DefaultValue Ranking ranking,
        @DefaultValue Search search
) {

    /** Which embedding model turns listing text into vectors. */
    public record Embedding(
            @DefaultValue("none") String provider,
            @DefaultValue("") String model,
            @DefaultValue("") String apiKey,
            @DefaultValue("3s") Duration timeout,
            @DefaultValue("384") int dimension
    ) {
        public Embedding {
            if (dimension <= 0) {
                throw new IllegalArgumentException("app.ai.embedding.dimension must be positive");
            }
        }
    }

    /** Where the Qdrant vector database lives and which collection holds listing vectors. */
    public record Qdrant(
            @DefaultValue("") String url,
            @DefaultValue("") String apiKey,
            @DefaultValue("listings") String collection,
            @DefaultValue("5s") Duration timeout
    ) {}

    /**
     * Weights for the hybrid ranking formula. Each weight must be >= 0 and they must sum to 1.
     * Defaults: semantic 0.70, lexical 0.15, freshness 0.10, quality 0.05.
     */
    public record Ranking(
            @DefaultValue("0.70") double semantic,
            @DefaultValue("0.15") double lexical,
            @DefaultValue("0.10") double freshness,
            @DefaultValue("0.05") double quality
    ) {
        public Ranking {
            if (semantic < 0 || lexical < 0 || freshness < 0 || quality < 0) {
                throw new IllegalArgumentException("app.ai.ranking weights must not be negative");
            }
            if (Math.abs((semantic + lexical + freshness + quality) - 1.0) > 0.001) {
                throw new IllegalArgumentException("app.ai.ranking weights must sum to 1.0");
            }
        }
    }

    /**
     * Semantic search tuning.
     * candidateLimit is the minimum number of vector-store hits requested per query. It is raised
     * automatically for deep pages so that page N can always be filled.
     */
    public record Search(
            @DefaultValue("200") int candidateLimit
    ) {
        public Search {
            if (candidateLimit <= 0) {
                throw new IllegalArgumentException("app.ai.search.candidate-limit must be positive");
            }
        }
    }
}
