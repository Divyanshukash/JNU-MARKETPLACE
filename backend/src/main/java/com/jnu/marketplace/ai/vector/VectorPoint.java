package com.jnu.marketplace.ai.vector;

/**
 * One listing's vector plus its filter metadata, ready to be written to the store.
 * The store point id is derived from the listing id inside the gateway implementation.
 */
public record VectorPoint(String listingId, float[] vector, VectorPayload payload) {}
