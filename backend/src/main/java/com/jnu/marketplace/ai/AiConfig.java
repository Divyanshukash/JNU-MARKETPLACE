package com.jnu.marketplace.ai;

import com.jnu.marketplace.ai.embedding.DisabledEmbeddingProvider;
import com.jnu.marketplace.ai.embedding.EmbeddingProvider;
import com.jnu.marketplace.ai.embedding.OpenAiEmbeddingProvider;
import com.jnu.marketplace.ai.search.HybridRanker;
import com.jnu.marketplace.ai.vector.DisabledVectorStoreGateway;
import com.jnu.marketplace.ai.vector.QdrantVectorStoreGateway;
import com.jnu.marketplace.ai.vector.VectorStoreGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

/**
 * Wires the AI beans.
 *
 * When app.ai.enabled is false (the default), both beans are the disabled implementations and nothing
 * contacts an external service. When it is true, misconfiguration fails at startup with a message that
 * names the environment variable. The message never contains a secret value.
 *
 * Nothing in the marketplace depends on these beans directly, so the application behaves the same way
 * whether AI is on or off.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    public EmbeddingProvider embeddingProvider(AiProperties properties) {
        if (!properties.enabled()) {
            return new DisabledEmbeddingProvider();
        }
        AiProperties.Embedding embedding = properties.embedding();
        String provider = embedding.provider() == null ? "none" : embedding.provider().trim().toLowerCase(Locale.ROOT);
        switch (provider) {
            case "openai" -> {
                if (isBlank(embedding.apiKey())) {
                    throw new IllegalStateException(
                            "AI is enabled with provider 'openai' but AI_EMBEDDING_API_KEY is not set");
                }
                if (isBlank(embedding.model())) {
                    throw new IllegalStateException(
                            "AI is enabled with provider 'openai' but AI_EMBEDDING_MODEL is not set");
                }
                log.info("Embedding provider: openai, model {}, dimension {}", embedding.model(), embedding.dimension());
                return new OpenAiEmbeddingProvider(
                        OpenAiEmbeddingProvider.buildClient(embedding),
                        embedding.model(),
                        embedding.dimension());
            }
            case "none" -> {
                log.warn("AI is enabled but AI_EMBEDDING_PROVIDER is 'none'. Search uses conventional matching.");
                return new DisabledEmbeddingProvider();
            }
            default -> throw new IllegalStateException(
                    "Unsupported AI_EMBEDDING_PROVIDER '" + provider + "'. Supported values: openai, none");
        }
    }

    @Bean
    public HybridRanker hybridRanker(AiProperties properties) {
        return new HybridRanker(properties.ranking());
    }

    /**
     * Qdrant when AI is enabled, otherwise the disabled gateway. Building the client does not connect.
     * The first real call does, and it validates the collection.
     */
    @Bean
    public VectorStoreGateway vectorStoreGateway(AiProperties properties) {
        if (!properties.enabled()) {
            return new DisabledVectorStoreGateway();
        }
        AiProperties.Qdrant qdrant = properties.qdrant();
        if (isBlank(qdrant.url())) {
            throw new IllegalStateException("AI is enabled but QDRANT_URL is not set");
        }
        return new QdrantVectorStoreGateway(
                QdrantVectorStoreGateway.buildClient(qdrant),
                qdrant.collection(),
                properties.embedding().dimension());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
