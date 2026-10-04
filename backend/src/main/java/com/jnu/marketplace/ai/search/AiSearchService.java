package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.ai.AiProperties;
import com.jnu.marketplace.ai.embedding.EmbeddingProvider;
import com.jnu.marketplace.ai.search.HybridRanker.RankedListing;
import com.jnu.marketplace.ai.search.HybridRanker.RankingInput;
import com.jnu.marketplace.ai.vector.VectorStoreGateway;
import com.jnu.marketplace.ai.vector.VectorStoreGateway.VectorMatch;
import com.jnu.marketplace.dto.SearchRequest;
import com.jnu.marketplace.model.Listing;
import com.jnu.marketplace.repository.ListingRepository;
import com.jnu.marketplace.service.ListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Semantic search with hybrid ranking. Falls back to the conventional marketplace search on any problem.
 *
 * Flow for a keyword query, when AI is available:
 *   query -> EmbeddingProvider -> query vector (dimension checked)
 *         -> VectorStoreGateway.search -> candidate listing ids + similarity scores
 *         -> ListingRepository.findAllById -> canonical MongoDB listings
 *         -> eligibility: status ACTIVE and the UI's filters, checked against MongoDB data
 *         -> lexical, freshness and quality scores -> HybridRanker -> page slice
 *
 * MongoDB is the source of truth. Qdrant contributes only ids and similarity. An id that is
 * missing in MongoDB, not ACTIVE, or filtered out is dropped, so stale vectors never reach users.
 *
 * Fallback to ListingService.searchListings happens when:
 * - the query is empty, or the request uses a field the semantic path does not support
 * - AI is disabled or not configured (checked before any network call)
 * - embedding or Qdrant fails, the vector dimension mismatches, or MongoDB lookup fails
 * - Qdrant returns no candidates, or none of them are eligible
 *
 * Errors are logged with detail. The response never contains them.
 */
@Service
public class AiSearchService {

    private static final Logger log = LoggerFactory.getLogger(AiSearchService.class);

    /** Upper bound on vector-store candidates per query. Keeps ranking cost bounded. */
    static final int MAX_CANDIDATES = 1000;

    private final EmbeddingProvider embeddingProvider;
    private final VectorStoreGateway vectorStore;
    private final ListingRepository listingRepository;
    private final ListingService listingService;
    private final HybridRanker ranker;
    private final AiProperties properties;

    public AiSearchService(EmbeddingProvider embeddingProvider,
                           VectorStoreGateway vectorStore,
                           ListingRepository listingRepository,
                           ListingService listingService,
                           HybridRanker ranker,
                           AiProperties properties) {
        this.embeddingProvider = embeddingProvider;
        this.vectorStore = vectorStore;
        this.listingRepository = listingRepository;
        this.listingService = listingService;
        this.ranker = ranker;
        this.properties = properties;
    }

    public AiSearchResponse search(SearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (!request.hasKeyword()) {
            return keywordSearch(request, pageable);
        }
        Optional<Filters> filters = Filters.from(request);
        if (filters.isEmpty()) {
            log.debug("Semantic path skipped: request uses a field it does not support");
            return keywordSearch(request, pageable);
        }
        if (!embeddingProvider.isAvailable() || !vectorStore.isAvailable()) {
            log.debug("Semantic search unavailable (AI disabled or not configured)");
            return keywordSearch(request, pageable);
        }

        try {
            Optional<AiSearchResponse> semantic = semanticSearch(request.getKeyword().trim(), filters.get(), page, size);
            if (semantic.isPresent()) {
                return semantic.get();
            }
            log.debug("Semantic search produced no eligible results, using keyword search");
            return keywordSearch(request, pageable);
        } catch (RuntimeException e) {
            log.warn("Semantic search failed, using keyword search: {}", e.getMessage());
            return keywordSearch(request, pageable);
        }
    }

    private Optional<AiSearchResponse> semanticSearch(String query, Filters filters, int page, int size) {
        float[] queryVector = embeddingProvider.embed(query);
        if (queryVector == null || queryVector.length != embeddingProvider.dimension()) {
            log.warn("Query embedding dimension mismatch: expected {}, got {}",
                    embeddingProvider.dimension(), queryVector == null ? "null" : queryVector.length);
            return Optional.empty();
        }

        List<VectorMatch> matches = vectorStore.search(queryVector, candidateLimit(page, size));
        if (matches.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Double> semanticById = bestScoreById(matches);
        List<Listing> canonical = listingRepository.findAllById(semanticById.keySet());
        List<Listing> eligible = canonical.stream()
                .filter(listing -> listing.getStatus() == Listing.ListingStatus.ACTIVE)
                .filter(filters::matches)
                .toList();

        int dropped = semanticById.size() - eligible.size();
        if (dropped > 0) {
            log.debug("Dropped {} semantic candidates that were missing, inactive, or filtered out", dropped);
        }
        if (eligible.isEmpty()) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        List<RankingInput> inputs = eligible.stream()
                .map(listing -> new RankingInput(
                        listing,
                        ScoreRange.clamp01(semanticById.get(listing.getId())),
                        LexicalScorer.score(query, listing),
                        FreshnessScorer.score(listing.getCreatedAt(), now),
                        QualityScorer.score(listing)))
                .toList();

        return Optional.of(toPage(ranker.rank(inputs), page, size));
    }

    /**
     * Number of vector-store hits to request. It must cover the deepest page asked for,
     * otherwise that page could never be filled. Pages are capped at MAX_CANDIDATES.
     */
    int candidateLimit(int page, int size) {
        long needed = (long) (page + 1) * size;
        long limit = Math.max(properties.search().candidateLimit(), needed);
        return (int) Math.min(MAX_CANDIDATES, limit);
    }

    /** Qdrant can return the same listing more than once. Keep its best score. */
    private static Map<String, Double> bestScoreById(List<VectorMatch> matches) {
        Map<String, Double> best = new LinkedHashMap<>();
        for (VectorMatch match : matches) {
            best.merge(match.listingId(), match.score(), Math::max);
        }
        return best;
    }

    private static AiSearchResponse toPage(List<RankedListing> ranked, int page, int size) {
        int total = ranked.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<Listing> content = ranked.subList(from, to).stream().map(RankedListing::listing).toList();
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new AiSearchResponse(content, page, size, total, totalPages, AiSearchResponse.SEMANTIC);
    }

    private AiSearchResponse keywordSearch(SearchRequest request, Pageable pageable) {
        Page<Listing> page = listingService.searchListings(request, pageable);
        return AiSearchResponse.of(page, AiSearchResponse.KEYWORD);
    }

    /**
     * The filters the semantic path applies in memory. Each one matches the conventional search:
     * category and condition are exact matches, and price is an inclusive range.
     * Anything the frontend does not send is not supported here, so the request goes to the conventional search.
     */
    record Filters(Listing.Category category, Listing.Condition condition, BigDecimal minPrice, BigDecimal maxPrice) {

        /** Empty when the request uses a field this path does not handle, or a category value is not recognized. */
        static Optional<Filters> from(SearchRequest request) {
            if (request.getSubCategory() != null || request.getLocation() != null
                    || request.getHostelBlock() != null || request.getSellerId() != null
                    || request.isNegotiable() || request.isFeatured() || request.isUrgent()
                    || (request.getTags() != null && !request.getTags().isEmpty())) {
                return Optional.empty();
            }
            if (!"createdAt".equals(request.getSortBy()) || !"desc".equals(request.getSortOrder())) {
                return Optional.empty();
            }
            Listing.Category category = null;
            if (request.getCategory() != null && !request.getCategory().isBlank()) {
                category = parseCategory(request.getCategory());
                if (category == null) return Optional.empty();
            }
            return Optional.of(new Filters(category, request.getCondition(),
                    request.getMinPrice(), request.getMaxPrice()));
        }

        boolean matches(Listing listing) {
            if (category != null && listing.getCategory() != category) return false;
            if (condition != null && listing.getCondition() != condition) return false;
            if (minPrice != null && (listing.getPrice() == null || listing.getPrice().compareTo(minPrice) < 0)) return false;
            if (maxPrice != null && (listing.getPrice() == null || listing.getPrice().compareTo(maxPrice) > 0)) return false;
            return true;
        }

        /** Same rule as ListingService.parseCategory: enum name or display name, ignoring case. */
        private static Listing.Category parseCategory(String value) {
            String trimmed = value.trim();
            for (Listing.Category candidate : Listing.Category.values()) {
                if (candidate.name().equalsIgnoreCase(trimmed) || candidate.getDisplayName().equalsIgnoreCase(trimmed)) {
                    return candidate;
                }
            }
            return null;
        }
    }
}
