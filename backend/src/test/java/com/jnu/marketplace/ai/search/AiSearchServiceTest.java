package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.ai.AiProperties;
import com.jnu.marketplace.ai.AiUnavailableException;
import com.jnu.marketplace.ai.embedding.EmbeddingProvider;
import com.jnu.marketplace.ai.vector.VectorStoreGateway;
import com.jnu.marketplace.ai.vector.VectorStoreGateway.VectorMatch;
import com.jnu.marketplace.dto.SearchRequest;
import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.repository.ListingRepository;
import com.jnu.marketplace.service.ListingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers the semantic path and each fallback. The embedding provider, vector store, MongoDB repository
 * and conventional search are mocks. The ranker is real.
 */
class AiSearchServiceTest {

    private static final float[] QUERY_VECTOR = {0.1f, 0.2f, 0.3f};

    private EmbeddingProvider provider;
    private VectorStoreGateway vectors;
    private ListingRepository listingRepository;
    private ListingService listingService;
    private AiSearchService service;

    @BeforeEach
    void setUp() {
        provider = mock(EmbeddingProvider.class);
        when(provider.isAvailable()).thenReturn(true);
        when(provider.dimension()).thenReturn(3);
        when(provider.embed(anyString())).thenReturn(QUERY_VECTOR);

        vectors = mock(VectorStoreGateway.class);
        when(vectors.isAvailable()).thenReturn(true);

        listingRepository = mock(ListingRepository.class);
        listingService = mock(ListingService.class);
        Page<Listing> keywordPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(listingService.searchListings(any(SearchRequest.class), any(Pageable.class))).thenReturn(keywordPage);

        service = new AiSearchService(provider, vectors, listingRepository, listingService,
                new HybridRanker(new AiProperties.Ranking(0.70, 0.15, 0.10, 0.05)), properties());
    }

    @Test
    void semanticQueryEmbedsSearchesAndReturnsCanonicalListings() {
        when(vectors.search(any(), anyInt())).thenReturn(List.of(
                new VectorMatch("l1", 0.90), new VectorMatch("l2", 0.80)));
        when(listingRepository.findAllById(any())).thenReturn(List.of(
                active("l1", "Mountain Bike", 100), active("l2", "Road Gear", 100)));

        AiSearchResponse response = service.search(keyword("bike for commuting"), 0, 10);

        assertThat(response.searchMode()).isEqualTo(AiSearchResponse.SEMANTIC);
        assertThat(ids(response)).containsExactly("l1", "l2");
        verify(provider).embed("bike for commuting");
        ArgumentCaptor<float[]> sent = ArgumentCaptor.forClass(float[].class);
        verify(vectors).search(sent.capture(), eq(200));
        assertThat(sent.getValue()).containsExactly(QUERY_VECTOR);
        verify(listingService, never()).searchListings(any(), any());
    }

    @Test
    void qdrantScoresAreUsedWhileMongoListingsAreReturned() {
        Listing canonical = active("l1", "Mountain Bike", 100);
        canonical.setDescription("canonical description from MongoDB");
        when(vectors.search(any(), anyInt())).thenReturn(List.of(new VectorMatch("l1", 0.9)));
        when(listingRepository.findAllById(any())).thenReturn(List.of(canonical));

        AiSearchResponse response = service.search(keyword("bike"), 0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0)).isSameAs(canonical);
    }

    @Test
    void vectorDimensionMismatchFallsBackToKeywordSearch() {
        when(provider.embed(anyString())).thenReturn(new float[]{0.1f, 0.2f});

        AiSearchResponse response = service.search(keyword("bike"), 0, 10);

        assertThat(response.searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(vectors, never()).search(any(), anyInt());
        verify(listingService).searchListings(any(), any());
    }

    @Test
    void inactiveDeletedAndMissingListingsAreRemoved() {
        when(vectors.search(any(), anyInt())).thenReturn(List.of(
                new VectorMatch("l1", 0.9),
                new VectorMatch("sold", 0.95),
                new VectorMatch("gone", 0.85)));
        Listing sold = active("sold", "Mountain Bike", 100);
        sold.setStatus(Listing.ListingStatus.SOLD);
        when(listingRepository.findAllById(any())).thenReturn(List.of(active("l1", "Mountain Bike", 100), sold));

        AiSearchResponse response = service.search(keyword("bike"), 0, 10);

        assertThat(response.searchMode()).isEqualTo(AiSearchResponse.SEMANTIC);
        assertThat(ids(response)).containsExactly("l1");
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void filtersAreAppliedFromCanonicalMongoData() {
        SearchRequest request = keyword("bike");
        request.setCategory("VEHICLES");
        request.setMinPrice(new BigDecimal("50"));
        request.setMaxPrice(new BigDecimal("150"));
        when(vectors.search(any(), anyInt())).thenReturn(List.of(
                new VectorMatch("in-range", 0.9),
                new VectorMatch("wrong-category", 0.8),
                new VectorMatch("too-expensive", 0.7)));
        Listing wrongCategory = active("wrong-category", "Bike Helmet", 100);
        wrongCategory.setCategory(Listing.Category.CLOTHING);
        when(listingRepository.findAllById(any())).thenReturn(List.of(
                active("in-range", "Mountain Bike", 100), wrongCategory, active("too-expensive", "Bike Pro", 500)));

        AiSearchResponse response = service.search(request, 0, 10);

        assertThat(ids(response)).containsExactly("in-range");
    }

    @Test
    void conditionFilterIsApplied() {
        SearchRequest request = keyword("bike");
        request.setCondition(Listing.Condition.NEW);
        when(vectors.search(any(), anyInt())).thenReturn(List.of(new VectorMatch("used", 0.9), new VectorMatch("new", 0.8)));
        Listing used = active("used", "Mountain Bike", 100);
        used.setCondition(Listing.Condition.GOOD);
        Listing fresh = active("new", "Mountain Bike", 100);
        fresh.setCondition(Listing.Condition.NEW);
        when(listingRepository.findAllById(any())).thenReturn(List.of(used, fresh));

        assertThat(ids(service.search(request, 0, 10))).containsExactly("new");
    }

    @Test
    void whenEveryCandidateIsIneligibleTheKeywordSearchAnswers() {
        when(vectors.search(any(), anyInt())).thenReturn(List.of(new VectorMatch("sold", 0.9)));
        Listing sold = active("sold", "Mountain Bike", 100);
        sold.setStatus(Listing.ListingStatus.SOLD);
        when(listingRepository.findAllById(any())).thenReturn(List.of(sold));

        AiSearchResponse response = service.search(keyword("bike"), 0, 10);

        assertThat(response.searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(listingService).searchListings(any(), any());
    }

    @Test
    void aiDisabledGoesStraightToKeywordWithoutQdrantOrEmbedding() {
        when(provider.isAvailable()).thenReturn(false);

        AiSearchResponse response = service.search(keyword("bike"), 0, 10);

        assertThat(response.searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(provider, never()).embed(anyString());
        verifyNoInteractions(vectors, listingRepository);
    }

    @Test
    void embeddingUnavailableFallsBackToKeyword() {
        when(provider.embed(anyString())).thenThrow(new AiUnavailableException("timeout"));

        assertThat(service.search(keyword("bike"), 0, 10).searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(listingService).searchListings(any(), any());
    }

    @Test
    void qdrantUnavailableFallsBackToKeyword() {
        when(vectors.search(any(), anyInt())).thenThrow(new AiUnavailableException("Qdrant down"));

        assertThat(service.search(keyword("bike"), 0, 10).searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(listingService).searchListings(any(), any());
    }

    @Test
    void noSemanticCandidatesFallsBackToKeyword() {
        when(vectors.search(any(), anyInt())).thenReturn(List.of());

        assertThat(service.search(keyword("bike"), 0, 10).searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(listingService).searchListings(any(), any());
    }

    @Test
    void mongoLookupFailureFallsBackToKeyword() {
        when(vectors.search(any(), anyInt())).thenReturn(List.of(new VectorMatch("l1", 0.9)));
        when(listingRepository.findAllById(any())).thenThrow(new RuntimeException("mongo unreachable"));

        assertThat(service.search(keyword("bike"), 0, 10).searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
    }

    @Test
    void emptyQueryUsesKeywordSearchWithoutEmbedding() {
        AiSearchResponse response = service.search(new SearchRequest(), 0, 10);

        assertThat(response.searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(provider, never()).embed(anyString());
        verifyNoInteractions(vectors);
    }

    @Test
    void unsupportedFieldDelegatesToKeywordSearch() {
        SearchRequest request = keyword("bike");
        request.setSubCategory("Books");

        assertThat(service.search(request, 0, 10).searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(provider, never()).embed(anyString());
    }

    @Test
    void unknownCategoryDelegatesToKeywordSearchSoItsErrorIsUnchanged() {
        SearchRequest request = keyword("bike");
        request.setCategory("Not A Real Category");

        assertThat(service.search(request, 0, 10).searchMode()).isEqualTo(AiSearchResponse.KEYWORD);
        verify(provider, never()).embed(anyString());
    }

    @Test
    void paginationSlicesTheRankedList() {
        when(vectors.search(any(), anyInt())).thenReturn(List.of(
                new VectorMatch("a", 0.9), new VectorMatch("b", 0.8), new VectorMatch("c", 0.7)));
        when(listingRepository.findAllById(any())).thenReturn(List.of(
                active("a", "x", 1), active("b", "x", 1), active("c", "x", 1)));

        AiSearchResponse second = service.search(keyword("x"), 1, 2);

        assertThat(second.content()).hasSize(1);
        assertThat(second.totalElements()).isEqualTo(3);
        assertThat(second.totalPages()).isEqualTo(2);
        assertThat(second.page()).isEqualTo(1);
    }

    @Test
    void candidateLimitCoversTheRequestedPageAndIsCapped() {
        assertThat(service.candidateLimit(0, 20)).isEqualTo(200);
        assertThat(service.candidateLimit(2, 40)).isEqualTo(200);
        assertThat(service.candidateLimit(4, 100)).isEqualTo(500);
        assertThat(service.candidateLimit(10, 100)).isEqualTo(AiSearchService.MAX_CANDIDATES);
    }

    private static SearchRequest keyword(String text) {
        return new SearchRequest(text);
    }

    private static Listing active(String id, String title, int price) {
        Listing listing = new Listing();
        listing.setId(id);
        listing.setTitle(title);
        listing.setDescription("Listing description long enough to be ordinary text.");
        listing.setCategory(Listing.Category.VEHICLES);
        listing.setCondition(Listing.Condition.GOOD);
        listing.setPrice(BigDecimal.valueOf(price));
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setCreatedAt(LocalDateTime.now().minusDays(1));
        return listing;
    }

    private static List<String> ids(AiSearchResponse response) {
        return response.content().stream().map(Listing::getId).toList();
    }

    private static AiProperties properties() {
        return new AiProperties(
                true,
                new AiProperties.Embedding("test", "test-model", "key", Duration.ofSeconds(3), 3),
                new AiProperties.Qdrant("http://qdrant.test", "", "listings", Duration.ofSeconds(5)),
                new AiProperties.Ranking(0.70, 0.15, 0.10, 0.05),
                new AiProperties.Search(200));
    }
}
