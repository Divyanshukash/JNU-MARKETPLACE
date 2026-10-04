package com.jnu.marketplace.ai.vector;

import com.jnu.marketplace.ai.AiUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests the Qdrant REST integration against a mock HTTP server. No real Qdrant is needed.
 */
class QdrantVectorStoreGatewayTest {

    private static final String BASE = "http://qdrant.test";
    private static final String COLLECTION = BASE + "/collections/listings";
    private static final String POINTS = COLLECTION + "/points?wait=true";
    private static final String DELETE = COLLECTION + "/points/delete?wait=true";
    private static final String PAYLOAD = COLLECTION + "/points/payload?wait=true";

    /** Collection info as Qdrant returns it for a 3-dimensional cosine collection. */
    private static final String COLLECTION_INFO_3D_COSINE =
            "{\"result\":{\"config\":{\"params\":{\"vectors\":{\"size\":3,\"distance\":\"Cosine\"}}}},\"status\":\"ok\"}";

    private MockRestServiceServer server;
    private QdrantVectorStoreGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.baseUrl(BASE).build();
        gateway = new QdrantVectorStoreGateway(client, "listings", 3);
    }

    @Test
    void createsMissingCollectionWithCosineDistanceBeforeUpserting() {
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.vectors.size").value(3))
                .andExpect(jsonPath("$.vectors.distance").value("Cosine"))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        expectUpsert("listing-1");

        gateway.upsert(point("listing-1"));

        server.verify();
    }

    @Test
    void reusesMatchingCollectionAndVerifiesItOnlyOnce() {
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        expectUpsert("listing-1");
        expectUpsert("listing-2");

        gateway.upsert(point("listing-1"));
        gateway.upsert(point("listing-2"));

        // The second upsert must not repeat the collection lookup. Any extra request fails server.verify().
        server.verify();
    }

    @Test
    void rejectsCollectionWithWrongDimensionAndWritesNothing() {
        String wrongSize = "{\"result\":{\"config\":{\"params\":{\"vectors\":{\"size\":768,\"distance\":\"Cosine\"}}}}}";
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(wrongSize, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> gateway.upsert(point("listing-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("768-dimensional")
                .hasMessageContaining("Refusing to use it");
        server.verify();
    }

    @Test
    void rejectsCollectionWithWrongDistanceMetric() {
        String euclid = "{\"result\":{\"config\":{\"params\":{\"vectors\":{\"size\":3,\"distance\":\"Euclid\"}}}}}";
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(euclid, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> gateway.upsert(point("listing-1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Euclid");
    }

    @Test
    void rejectsVectorWithWrongDimensionBeforeCallingQdrant() {
        VectorPoint bad = new VectorPoint("listing-1", new float[]{0.1f}, payload("listing-1"));

        assertThatThrownBy(() -> gateway.upsert(bad)).isInstanceOf(IllegalArgumentException.class);
        // No expectations were set, so any request would already have failed the test.
    }

    @Test
    void upsertFailureIsSurfacedAsUnavailable() {
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        server.expect(requestTo(POINTS)).andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError());

        assertThatThrownBy(() -> gateway.upsert(point("listing-1")))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("upsert")
                .hasMessageContaining("listing-1");
    }

    @Test
    void deleteFailureIsSurfacedAsUnavailable() {
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        server.expect(requestTo(DELETE)).andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThatThrownBy(() -> gateway.delete("listing-1"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("delete");
    }

    @Test
    void deleteTargetsDeterministicPointId() {
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        server.expect(requestTo(DELETE)).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.points[0]").value(QdrantVectorStoreGateway.pointIdFor("listing-9")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        gateway.delete("listing-9");

        server.verify();
    }

    @Test
    void payloadUpdateSendsPayloadWithoutTheVector() {
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        server.expect(requestTo(PAYLOAD)).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.points[0]").value(QdrantVectorStoreGateway.pointIdFor("listing-1")))
                .andExpect(jsonPath("$.payload.price").value(30.0))
                .andExpect(jsonPath("$.vector").doesNotExist())
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        gateway.updatePayload("listing-1", payload("listing-1", 30.0));

        server.verify();
    }

    @Test
    void upsertPayloadContainsOnlyApprovedMetadata() {
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        server.expect(requestTo(POINTS)).andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.points[0].payload.listingId").value("listing-1"))
                .andExpect(jsonPath("$.points[0].payload.status").value("ACTIVE"))
                .andExpect(jsonPath("$.points[0].payload.category").value("BOOKS"))
                .andExpect(jsonPath("$.points[0].payload.subcategory").value("Computer Science"))
                .andExpect(jsonPath("$.points[0].payload.price").value(20.5))
                .andExpect(jsonPath("$.points[0].payload.donation").value(false))
                .andExpect(jsonPath("$.points[0].payload.sellerId").value("seller-1"))
                .andExpect(jsonPath("$.points[0].payload.updatedAt").value("2026-10-04T10:00"))
                .andExpect(jsonPath("$.points[0].payload.title").doesNotExist())
                .andExpect(jsonPath("$.points[0].payload.description").doesNotExist())
                .andExpect(jsonPath("$.points[0].payload.sellerName").doesNotExist())
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        gateway.upsert(point("listing-1"));

        server.verify();
    }

    @Test
    void pointIdIsDeterministicValidUuidAndDistinctPerListing() {
        String first = QdrantVectorStoreGateway.pointIdFor("65a1f0c2e4b0a1b2c3d4e5f6");

        assertThat(QdrantVectorStoreGateway.pointIdFor("65a1f0c2e4b0a1b2c3d4e5f6")).isEqualTo(first);
        assertThat(QdrantVectorStoreGateway.pointIdFor("another-listing")).isNotEqualTo(first);
        assertThat(UUID.fromString(first)).isNotNull();
    }

    @Test
    void searchReturnsListingIdsAndScoresAndSkipsPointsWithoutAListingId() {
        String queryUrl = COLLECTION + "/points/query";
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        server.expect(requestTo(queryUrl)).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.limit").value(5))
                .andExpect(jsonPath("$.query.length()").value(3))
                .andExpect(jsonPath("$.with_payload[0]").value("listingId"))
                .andRespond(withSuccess(
                        "{\"result\":{\"points\":["
                                + "{\"id\":\"u1\",\"score\":0.91,\"payload\":{\"listingId\":\"l1\"}},"
                                + "{\"id\":\"u2\",\"score\":0.5,\"payload\":{}}"
                                + "]}}",
                        MediaType.APPLICATION_JSON));

        List<VectorStoreGateway.VectorMatch> matches = gateway.search(new float[]{0.1f, 0.2f, 0.3f}, 5);

        assertThat(matches).containsExactly(new VectorStoreGateway.VectorMatch("l1", 0.91));
        server.verify();
    }

    @Test
    void searchRejectsQueryVectorOfWrongDimension() {
        assertThatThrownBy(() -> gateway.search(new float[]{0.1f}, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchFailureIsSurfacedAsUnavailable() {
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COLLECTION_INFO_3D_COSINE, MediaType.APPLICATION_JSON));
        server.expect(requestTo(COLLECTION + "/points/query")).andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThatThrownBy(() -> gateway.search(new float[]{0.1f, 0.2f, 0.3f}, 5))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("search");
    }

    @Test
    void searchChecksCollectionDimensionBeforeQuerying() {
        String wrongSize = "{\"result\":{\"config\":{\"params\":{\"vectors\":{\"size\":768,\"distance\":\"Cosine\"}}}}}";
        server.expect(requestTo(COLLECTION)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(wrongSize, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> gateway.search(new float[]{0.1f, 0.2f, 0.3f}, 5))
                .isInstanceOf(IllegalStateException.class);
        server.verify();
    }

    private void expectUpsert(String listingId) {
        server.expect(requestTo(POINTS)).andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.points[0].id").value(QdrantVectorStoreGateway.pointIdFor(listingId)))
                .andExpect(jsonPath("$.points[0].vector.length()").value(3))
                .andExpect(jsonPath("$.points[0].payload.listingId").value(listingId))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    }

    private static VectorPoint point(String listingId) {
        return new VectorPoint(listingId, new float[]{0.1f, 0.2f, 0.3f}, payload(listingId));
    }

    private static VectorPayload payload(String listingId) {
        return payload(listingId, 20.5);
    }

    private static VectorPayload payload(String listingId, double price) {
        return new VectorPayload(listingId, "ACTIVE", "BOOKS", "Computer Science", price,
                false, "seller-1", "2026-10-04T10:00");
    }
}
