package com.jnu.marketplace.ai.vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.jnu.marketplace.ai.AiProperties;
import com.jnu.marketplace.ai.AiUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Qdrant implementation of VectorStoreGateway, using Qdrant's REST API through Spring's RestClient.
 * No Qdrant SDK is added, so there are no extra dependencies. Qdrant types stay in this class.
 *
 * Point ids: Qdrant accepts only unsigned integers or UUIDs, and Mongo ObjectIds are neither.
 * So each listing maps to a name-based (version 3) UUID derived from its listing id. The same
 * listing always maps to the same point, which makes upserts idempotent. The listing id is
 * also stored in the payload, so the mapping can be checked directly.
 *
 * Collection: created on first use if missing, with one unnamed vector of the configured
 * dimension and Cosine distance. If it already exists with a different size or distance, the
 * gateway refuses to use it and fails with a clear message. It never recreates or deletes it.
 * The check runs once per process and is retried after a failure.
 *
 * Writes use wait=true, so Qdrant only confirms after the change is applied.
 */
public class QdrantVectorStoreGateway implements VectorStoreGateway {

    private static final Logger log = LoggerFactory.getLogger(QdrantVectorStoreGateway.class);
    private static final String DISTANCE = "Cosine";

    private final RestClient client;
    private final String collection;
    private final int dimension;
    private volatile boolean collectionVerified = false;

    public QdrantVectorStoreGateway(RestClient client, String collection, int dimension) {
        this.client = client;
        this.collection = collection;
        this.dimension = dimension;
    }

    /**
     * Builds the HTTP client from configuration. Building it does not connect to Qdrant.
     * The api-key header is set only when a key is configured.
     */
    public static RestClient buildClient(AiProperties.Qdrant config) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(config.timeout());
        requestFactory.setReadTimeout(config.timeout());

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(config.url())
                .requestFactory(requestFactory);
        if (config.apiKey() != null && !config.apiKey().isBlank()) {
            builder.defaultHeader("api-key", config.apiKey());
        }
        return builder.build();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String upsert(VectorPoint point) {
        requireDimension(point.vector());
        ensureCollection();

        String pointId = pointIdFor(point.listingId());
        Map<String, Object> body = Map.of("points", List.of(Map.of(
                "id", pointId,
                "vector", toList(point.vector()),
                "payload", point.payload().toMap())));

        call("upsert", point.listingId(), () ->
                client.put().uri("/collections/{c}/points?wait=true", collection)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity());
        return pointId;
    }

    @Override
    public void updatePayload(String listingId, VectorPayload payload) {
        ensureCollection();

        Map<String, Object> body = Map.of(
                "payload", payload.toMap(),
                "points", List.of(pointIdFor(listingId)));

        call("payload update", listingId, () ->
                client.post().uri("/collections/{c}/points/payload?wait=true", collection)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity());
    }

    @Override
    public void delete(String listingId) {
        ensureCollection();

        Map<String, Object> body = Map.of("points", List.of(pointIdFor(listingId)));

        call("delete", listingId, () ->
                client.post().uri("/collections/{c}/points/delete?wait=true", collection)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity());
    }

    /**
     * Nearest points to the query vector, best first. Only the listingId payload field is returned.
     * Points without that field are skipped. The caller re-checks every id against MongoDB, so a stale
     * point cannot produce a result on its own.
     *
     * Uses POST /collections/{name}/points/query, the Qdrant Query API. The older /points/search endpoint is
     * deprecated in Qdrant's documentation, so it is not used. Requires a Qdrant version that provides the
     * Query API (1.10 or later). Response: {"result": {"points": [{id, score, payload}]}}.
     */
    @Override
    public List<VectorMatch> search(float[] queryVector, int limit) {
        requireDimension(queryVector);
        ensureCollection();

        Map<String, Object> body = Map.of(
                "query", toList(queryVector),
                "limit", limit,
                "with_payload", List.of("listingId"));

        JsonNode response;
        try {
            response = client.post().uri("/collections/{c}/points/query", collection)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new AiUnavailableException("Qdrant search failed: " + e.getMessage());
        }

        List<VectorMatch> matches = new ArrayList<>();
        if (response == null) return matches;
        for (JsonNode hit : response.path("result").path("points")) {
            JsonNode listingId = hit.path("payload").path("listingId");
            if (!listingId.isTextual() || listingId.asText().isBlank()) continue;
            matches.add(new VectorMatch(listingId.asText(), hit.path("score").asDouble(0.0)));
        }
        return matches;
    }

    /** Deterministic point id for a listing. Exposed so tests and tooling can check the mapping. */
    public static String pointIdFor(String listingId) {
        return UUID.nameUUIDFromBytes(("listing:" + listingId).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private void ensureCollection() {
        if (collectionVerified) return;
        synchronized (this) {
            if (collectionVerified) return;

            JsonNode existing = fetchCollection();
            if (existing == null) {
                createCollection();
                existing = fetchCollection();
            }
            if (existing == null) {
                throw new AiUnavailableException("Qdrant collection '" + collection + "' could not be created");
            }
            verifyCollection(existing);
            collectionVerified = true;
            log.info("Qdrant collection '{}' verified: {}-dimensional, {}", collection, dimension, DISTANCE);
        }
    }

    /** Returns null when the collection does not exist yet. */
    private JsonNode fetchCollection() {
        try {
            return client.get().uri("/collections/{c}", collection)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (RestClientException e) {
            throw new AiUnavailableException("Qdrant collection lookup failed: " + e.getMessage());
        }
    }

    private void createCollection() {
        Map<String, Object> body = Map.of("vectors", Map.of("size", dimension, "distance", DISTANCE));
        try {
            client.put().uri("/collections/{c}", collection)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Created Qdrant collection '{}' ({}-dimensional, {})", collection, dimension, DISTANCE);
        } catch (HttpClientErrorException.Conflict e) {
            // Another instance created it first. The verification step that follows still checks it.
            log.info("Qdrant collection '{}' was created concurrently", collection);
        } catch (RestClientException e) {
            throw new AiUnavailableException("Qdrant collection creation failed: " + e.getMessage());
        }
    }

    private void verifyCollection(JsonNode info) {
        JsonNode vectors = info.path("result").path("config").path("params").path("vectors");
        int size = vectors.path("size").asInt(-1);
        String distance = vectors.path("distance").asText("");

        if (size != dimension) {
            throw new IllegalStateException(String.format(
                    "Qdrant collection '%s' stores %d-dimensional vectors but app.ai.embedding.dimension is %d. "
                            + "Refusing to use it. Use a new collection name or fix the configuration.",
                    collection, size, dimension));
        }
        if (!DISTANCE.equalsIgnoreCase(distance)) {
            throw new IllegalStateException(String.format(
                    "Qdrant collection '%s' uses distance '%s' but '%s' is required. Refusing to use it.",
                    collection, distance, DISTANCE));
        }
    }

    private void call(String operation, String listingId, Runnable request) {
        try {
            request.run();
        } catch (RestClientException e) {
            throw new AiUnavailableException("Qdrant " + operation + " failed for listing " + listingId + ": " + e.getMessage());
        }
    }

    private void requireDimension(float[] vector) {
        if (vector == null || vector.length != dimension) {
            throw new IllegalArgumentException("Vector has " + (vector == null ? 0 : vector.length)
                    + " dimensions but the collection expects " + dimension);
        }
    }

    private static List<Float> toList(float[] vector) {
        List<Float> values = new ArrayList<>(vector.length);
        for (float value : vector) values.add(value);
        return values;
    }
}
