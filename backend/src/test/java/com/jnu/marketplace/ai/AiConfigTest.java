package com.jnu.marketplace.ai;

import com.jnu.marketplace.ai.embedding.DisabledEmbeddingProvider;
import com.jnu.marketplace.ai.embedding.EmbeddingProvider;
import com.jnu.marketplace.ai.vector.DisabledVectorStoreGateway;
import com.jnu.marketplace.ai.vector.QdrantVectorStoreGateway;
import com.jnu.marketplace.ai.vector.VectorStoreGateway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checks the AI configuration layer in isolation. It uses a bare application
 * context, so no MongoDB, Qdrant or embedding API is needed.
 */
class AiConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AiConfig.class);

    @Test
    void aiIsDisabledByDefaultAndBothBeansFallBackToDisabledImplementations() {
        runner.run(context -> {
            AiProperties properties = context.getBean(AiProperties.class);
            assertThat(properties.enabled()).isFalse();
            assertThat(properties.ranking().semantic()).isEqualTo(0.70);
            assertThat(properties.search().candidateLimit()).isEqualTo(200);

            EmbeddingProvider embeddings = context.getBean(EmbeddingProvider.class);
            VectorStoreGateway vectors = context.getBean(VectorStoreGateway.class);
            assertThat(embeddings).isInstanceOf(DisabledEmbeddingProvider.class);
            assertThat(vectors).isInstanceOf(DisabledVectorStoreGateway.class);
            assertThat(embeddings.isAvailable()).isFalse();
            assertThat(vectors.isAvailable()).isFalse();

            assertThatThrownBy(() -> embeddings.embed("vintage bicycle"))
                    .isInstanceOf(AiUnavailableException.class);
            assertThatThrownBy(() -> vectors.search(new float[]{0.1f}, 5))
                    .isInstanceOf(AiUnavailableException.class);
        });
    }

    @Test
    void bindsEnvironmentDrivenSettings() {
        runner.withPropertyValues(
                        "app.ai.enabled=true",
                        "app.ai.embedding.provider=openai",
                        "app.ai.embedding.model=text-embedding-3-small",
                        "app.ai.embedding.api-key=test-key",
                        "app.ai.embedding.timeout=5s",
                        "app.ai.embedding.dimension=1536",
                        "app.ai.qdrant.url=http://qdrant:6333",
                        "app.ai.qdrant.collection=listings_v2",
                        "app.ai.ranking.semantic=0.4",
                        "app.ai.ranking.lexical=0.3",
                        "app.ai.ranking.freshness=0.2",
                        "app.ai.ranking.quality=0.1")
                .run(context -> {
                    AiProperties properties = context.getBean(AiProperties.class);
                    assertThat(properties.enabled()).isTrue();
                    assertThat(properties.embedding().provider()).isEqualTo("openai");
                    assertThat(properties.embedding().model()).isEqualTo("text-embedding-3-small");
                    assertThat(properties.embedding().apiKey()).isEqualTo("test-key");
                    assertThat(properties.embedding().timeout()).isEqualTo(Duration.ofSeconds(5));
                    assertThat(properties.embedding().dimension()).isEqualTo(1536);
                    assertThat(properties.qdrant().url()).isEqualTo("http://qdrant:6333");
                    assertThat(properties.qdrant().collection()).isEqualTo("listings_v2");
                    assertThat(properties.ranking().semantic()).isEqualTo(0.4);
                });
    }

    @Test
    void enabledQdrantDoesNotConnectAtStartup() {
        // 127.0.0.1:1 refuses connections. The context must still start, which proves no eager connection is made.
        runner.withPropertyValues(
                        "app.ai.enabled=true",
                        "app.ai.qdrant.url=http://127.0.0.1:1")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(VectorStoreGateway.class))
                            .isInstanceOf(QdrantVectorStoreGateway.class);
                    assertThat(context.getBean(VectorStoreGateway.class).isAvailable()).isTrue();
                });
    }

    @Test
    void enabledOpenAiWithoutKeyFailsStartupWithoutLeakingAnything() {
        runner.withPropertyValues("app.ai.enabled=true", "app.ai.embedding.provider=openai",
                        "app.ai.qdrant.url=http://qdrant.test")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure().getMessage()).contains("AI_EMBEDDING_API_KEY");
                });
    }

    @Test
    void enabledWithoutQdrantUrlFailsStartup() {
        runner.withPropertyValues("app.ai.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure().getMessage()).contains("QDRANT_URL");
                });
    }

    @Test
    void unknownEmbeddingProviderFailsStartup() {
        runner.withPropertyValues("app.ai.enabled=true", "app.ai.embedding.provider=unknown-vendor",
                        "app.ai.qdrant.url=http://qdrant.test")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void enabledOpenAiWithKeySelectsTheRealProvider() {
        // The runner does not load application.properties, so the model is set explicitly here.
        runner.withPropertyValues("app.ai.enabled=true", "app.ai.embedding.provider=openai",
                        "app.ai.embedding.api-key=placeholder", "app.ai.embedding.model=text-embedding-3-small",
                        "app.ai.qdrant.url=http://qdrant.test")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(EmbeddingProvider.class))
                            .isInstanceOf(com.jnu.marketplace.ai.embedding.OpenAiEmbeddingProvider.class);
                });
    }

    @Test
    void rejectedRankingWeightsFailStartup() {
        runner.withPropertyValues(
                        "app.ai.ranking.semantic=0.9",
                        "app.ai.ranking.lexical=0.9",
                        "app.ai.ranking.freshness=0.15",
                        "app.ai.ranking.quality=0.15")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void nonPositiveDimensionFailsStartup() {
        runner.withPropertyValues("app.ai.embedding.dimension=0")
                .run(context -> assertThat(context).hasFailed());
    }
}
