package com.jnu.marketplace.ai.embedding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 fingerprint of the assembled listing text, as lowercase hex.
 *
 * Used only for change detection: if the hash matches the stored one, the
 * searchable content is unchanged and the embedding is not regenerated. SHA-256
 * comes with the JDK, so no extra dependency is needed. Collisions are not a
 * practical concern here, and the hash is never used for security.
 */
public final class ContentHasher {

    private ContentHasher() {}

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Every Java platform is required to provide SHA-256.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
