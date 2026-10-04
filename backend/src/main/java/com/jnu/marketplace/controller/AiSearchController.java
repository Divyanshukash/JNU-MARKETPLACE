package com.jnu.marketplace.controller;

import com.jnu.marketplace.ai.search.AiSearchResponse;
import com.jnu.marketplace.ai.search.AiSearchService;
import com.jnu.marketplace.dto.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Semantic search endpoint. It takes the same body and paging parameters as POST /api/listings/search,
 * so the frontend can switch endpoints without changing its request.
 *
 * Public, like /api/listings/search. Listing data is already public there, and this endpoint does not
 * expose more.
 */
@RestController
@RequestMapping("/api/ai")
public class AiSearchController {

    private static final Logger log = LoggerFactory.getLogger(AiSearchController.class);

    private final AiSearchService aiSearchService;

    public AiSearchController(AiSearchService aiSearchService) {
        this.aiSearchService = aiSearchService;
    }

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponse> search(
            @RequestBody SearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = (size < 1 || size > 100) ? 20 : size;
        return ResponseEntity.ok(aiSearchService.search(request, safePage, safeSize));
    }

    /** User-input problems get a short message. Anything else gets a generic response, and the details stay in the server log. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidInput(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Invalid search filter"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(RuntimeException e) {
        log.error("Unexpected error in AI search", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Search is temporarily unavailable. Please try again."));
    }
}
