package com.jnu.marketplace.ai;

/**
 * Thrown when an AI component is asked to do work while it is disabled,
 * unconfigured, or failing. Callers catch this (or check isAvailable() first)
 * and fall back to conventional search.
 */
public class AiUnavailableException extends RuntimeException {

    public AiUnavailableException(String message) {
        super(message);
    }
}
