package com.jnu.marketplace.ai.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.jnu.marketplace.ai.AiProperties;
import com.jnu.marketplace.ai.AiUnavailableException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * EmbeddingProvider backed by an OpenAI-compatible POST /v1/embeddings endpoint.
 *
 * Configuration (all from environment variables, see application.properties):
 * - AI_EMBEDDING_API_KEY: bearer token. Sent only in the Authorization header, never logged or put in an exception message.
 * - AI_EMBEDDING_MODEL: must support the "dimensions" request parameter, for example text-embedding-3-small or text-embedding-3-large.
 * - AI_EMBEDDING_DIMENSION: requested and verified vector length. It must match the Qdrant collection.
 * - AI_EMBEDDING_TIMEOUT: connect and read timeout for each request.
 *
 * Failure mapping: every failure (HTTP error, timeout, connection failure, malformed response, wrong length)
 * becomes an AiUnavailableException with a short description. No provider types leave this class, and the
 * caller treats the failure as an ordinary AI fallback.
 */
public class OpenAiEmbeddingProvider implements EmbeddingProvider {

    static final String BASE_URL = "https://api.openai.com";

    private final RestClient client;
    private final String model;
    private final int dimension;

    public OpenAiEmbeddingProvider(RestClient client, String model, int dimension) {
        this.client = client;
        this.model = model;
        this.dimension = dimension;
    }

    /** Builds the HTTP client. Building it does not connect. */
    public static RestClient buildClient(AiProperties.Embedding config) {
        return clientBuilder(config).build();
    }

    /** The configured builder, exposed so tests can attach a mock server to the same settings production uses. */
    static RestClient.Builder clientBuilder(AiProperties.Embedding config) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(config.timeout());
        requestFactory.setReadTimeout(config.timeout());
        return RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + config.apiKey())
                .defaultHeader("Content-Type", "application/json");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public String modelId() {
        return model;
    }

    @Override
    public float[] embed(String text) {
        Map<String, Object> body = Map.of(
                "model", model,
                "input", text,
                "dimensions", dimension,
                "encoding_format", "float");

        JsonNode response;
        try {
            response = client.post().uri("/v1/embeddings")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new AiUnavailableException("Embedding request failed with HTTP " + e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new AiUnavailableException("Embedding provider unreachable or timed out");
        }

        JsonNode values = response == null ? null : response.path("data").path(0).path("embedding");
        if (values == null || !values.isArray()) {
            throw new AiUnavailableException("Embedding response did not contain a vector");
        }
        if (values.size() != dimension) {
            throw new AiUnavailableException("Embedding dimension mismatch: configured " + dimension
                    + ", provider returned " + values.size());
        }
        float[] vector = new float[values.size()];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) values.get(i).asDouble();
        }
        return vector;
    }
}
