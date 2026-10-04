package com.jnu.marketplace.ai.indexing;

import com.jnu.marketplace.event.ListingChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Runs vector indexing after a listing change. Indexing does not block or fail the marketplace request.
 *
 * - @Async: runs on the task executor, so Qdrant latency and outages never reach the HTTP request.
 * - AFTER_COMMIT with fallbackExecution: inside a transaction (SaleService), it runs only after commit,
 *   so it reads committed data. Outside a transaction (ListingService), it runs immediately.
 *
 * Errors are logged, not rethrown. The listing state records any failure, and the next change retries.
 */
@Component
public class ListingIndexingListener {

    private static final Logger log = LoggerFactory.getLogger(ListingIndexingListener.class);

    private final ListingIndexingService indexingService;

    public ListingIndexingListener(ListingIndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onListingChanged(ListingChangedEvent event) {
        try {
            ListingIndexingService.IndexResult result = indexingService.sync(event.listingId());
            if (result.outcome() == ListingIndexingService.Outcome.FAILED) {
                log.warn("Vector sync for listing {} failed: {}", event.listingId(), result.error());
            }
        } catch (RuntimeException e) {
            log.warn("Vector sync for listing {} crashed: {}", event.listingId(), e.getMessage());
        }
    }
}
