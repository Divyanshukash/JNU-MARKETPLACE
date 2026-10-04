package com.jnu.marketplace.ai.search;

import com.jnu.marketplace.model.Listing;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Response of POST /api/ai/search. Its shape matches the Spring Page used by POST /api/listings/search
 * (content, totalElements, totalPages, page, size), so the existing frontend keeps working.
 *
 * searchMode is "SEMANTIC" when the hybrid path produced the results, or "KEYWORD" when the
 * conventional search did. The ranking scores and fallback reasons are kept in server logs
 * and are not exposed here.
 */
public record AiSearchResponse(
        List<Listing> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String searchMode
) {

    public static final String SEMANTIC = "SEMANTIC";
    public static final String KEYWORD = "KEYWORD";

    public static AiSearchResponse of(Page<Listing> page, String searchMode) {
        return new AiSearchResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                searchMode);
    }
}
