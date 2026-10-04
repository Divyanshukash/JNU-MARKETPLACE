package com.jnu.marketplace.ai.indexing;

import com.jnu.marketplace.ai.indexing.ListingIndexingService.IndexResult;
import com.jnu.marketplace.ai.indexing.ListingIndexingService.Outcome;
import com.jnu.marketplace.event.ListingChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListingIndexingListenerTest {

    @Test
    void delegatesChangeEventToIndexingSync() {
        ListingIndexingService indexing = mock(ListingIndexingService.class);
        when(indexing.sync("listing-1")).thenReturn(new IndexResult(Outcome.INDEXED, null));

        new ListingIndexingListener(indexing).onListingChanged(new ListingChangedEvent("listing-1"));

        verify(indexing).sync("listing-1");
    }

    @Test
    void indexingFailureIsContainedAndNeverRethrown() {
        ListingIndexingService indexing = mock(ListingIndexingService.class);
        when(indexing.sync("listing-1")).thenThrow(new RuntimeException("Qdrant exploded"));
        ListingIndexingListener listener = new ListingIndexingListener(indexing);

        assertThatCode(() -> listener.onListingChanged(new ListingChangedEvent("listing-1")))
                .doesNotThrowAnyException();
    }

    @Test
    void failedResultIsLoggedNotThrown() {
        ListingIndexingService indexing = mock(ListingIndexingService.class);
        when(indexing.sync("listing-1")).thenReturn(new IndexResult(Outcome.FAILED, "timeout"));

        assertThatCode(() -> new ListingIndexingListener(indexing)
                .onListingChanged(new ListingChangedEvent("listing-1")))
                .doesNotThrowAnyException();
    }

    @Test
    void listenerRunsAsyncAndAfterCommitWhenInsideATransaction() throws NoSuchMethodException {
        Method method = ListingIndexingListener.class.getMethod("onListingChanged", ListingChangedEvent.class);

        assertThat(method.isAnnotationPresent(Async.class)).isTrue();

        TransactionalEventListener tx = method.getAnnotation(TransactionalEventListener.class);
        assertThat(tx).isNotNull();
        assertThat(tx.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
        // Without this, the listener would not fire for non-transactional callers such as ListingService.
        assertThat(tx.fallbackExecution()).isTrue();
    }
}
