package com.jnu.marketplace.ai.embedding;

import org.springframework.data.mongodb.repository.MongoRepository;

/** MongoDB access for embedding bookkeeping. Keyed by listing id. */
public interface ListingEmbeddingStateRepository extends MongoRepository<ListingEmbeddingState, String> {
}
