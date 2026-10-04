package com.jnu.marketplace.ai.embedding;

import com.jnu.marketplace.ai.AiProperties;
import com.jnu.marketplace.ai.AiUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

/**
 * Exercises the OpenAI-compatible embedding call against a mock server. No network and no real key are used.
 * The API key below is a placeholder that the tests check never reaches an error message.
 */
class OpenAiEmbeddingProviderTest {

    private static final String URL = "https://api.openai.com/v1/embeddings";
    private static final String KEY = "sk-test-placeholder-key";
    private static final String MODEL = "text-embedding-3-small";

    private MockRestServiceServer server;
    private OpenAiEmbeddingProvider provider;

    @BeforeEach
    void setUp() {
        AiProperties.Embedding config = new AiProperties.Embedding("openai", MODEL, KEY, Duration.ofSeconds(3), 3);
        RestClient.Builder builder = OpenAiEmbeddingProvider.clientBuilder(config);
        server = MockRestServiceServer.bindTo(builder).build();
        provider = new OpenAiEmbeddingProvider(builder.build(), MODEL, 3);
    }

    @Test
    void sendsModelDimensionAndBearerKeyAndReturnsTheVector() {
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + KEY))
                .andExpect(jsonPath("$.model").value(MODEL))
                .andExpect(jsonPath("$.input").value("vintage bicycle"))
                .andExpect(jsonPath("$.dimensions").value(3))
                .andRespond(withSuccess(
                        "{\"data\":[{\"index\":0,\"embedding\":[0.25,-0.5,1.0]}]}",
                        MediaType.APPLICATION_JSON));

        float[] vector = provider.embed("vintage bicycle");

        assertThat(vector).containsExactly(0.25f, -0.5f, 1.0f);
        assertThat(provider.modelId()).isEqualTo(MODEL);
        assertThat(provider.dimension()).isEqualTo(3);
        server.verify();
    }

    @Test
    void providerReturningAWrongLengthVectorIsRejected() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"data\":[{\"embedding\":[0.1,0.2]}]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.embed("x"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("dimension mismatch");
    }

    @Test
    void malformedResponseIsRejected() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.embed("x"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("did not contain a vector");
    }

    @Test
    void httpErrorsBecomeUnavailableWithStatusOnlyAndNoKey() {
        server.expect(requestTo(URL)).andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> provider.embed("x"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessage("Embedding request failed with HTTP 401")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain(KEY));
    }

    @Test
    void serverErrorBecomesUnavailable() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> provider.embed("x"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessage("Embedding request failed with HTTP 500");
    }

    @Test
    void timeoutBecomesUnavailableWithoutInfrastructureDetail() {
        server.expect(requestTo(URL))
                .andRespond(withException(new SocketTimeoutException("read timed out after 3000ms against api.openai.com")));

        assertThatThrownBy(() -> provider.embed("x"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessage("Embedding provider unreachable or timed out")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("api.openai.com").doesNotContain(KEY));
    }
}
