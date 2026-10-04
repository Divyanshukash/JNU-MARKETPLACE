package com.jnu.marketplace.security;

import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Checks required secrets once at startup, so a missing or unusable value stops the application with a
 * message that names the environment variable.
 *
 * Messages never include the secret value or any part of it. The JWT key is validated the same way
 * JwtService reads it (base64, then HMAC key bytes), so anything accepted here is accepted at runtime.
 *
 * MONGODB_URI needs no check here. application.properties references it without a default, so Spring
 * already fails with "Could not resolve placeholder 'MONGODB_URI'" when it is missing.
 */
@Component
public class RequiredSecretsValidator {

    private static final Logger log = LoggerFactory.getLogger(RequiredSecretsValidator.class);

    /** HS256 requires a key of at least 256 bits. */
    static final int MIN_JWT_KEY_BYTES = 32;

    private final String jwtSecretKey;
    private final String mailPassword;

    public RequiredSecretsValidator(
            @Value("${application.security.jwt.secret-key:}") String jwtSecretKey,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.jwtSecretKey = jwtSecretKey;
        this.mailPassword = mailPassword;
    }

    @PostConstruct
    void validate() {
        validateJwtKey(jwtSecretKey);
        if (mailPassword == null || mailPassword.isBlank()) {
            log.warn("MAIL_PASSWORD is not set. Email features (verification, notifications) will fail.");
        }
    }

    static void validateJwtKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET_KEY is not set. Provide a base64-encoded signing key of at least "
                            + MIN_JWT_KEY_BYTES + " bytes.");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(key);
        } catch (RuntimeException e) {
            // The decoder's own message is not included, in case it echoes input.
            throw new IllegalStateException("JWT_SECRET_KEY is not valid base64.");
        }
        if (bytes.length < MIN_JWT_KEY_BYTES) {
            throw new IllegalStateException("JWT_SECRET_KEY decodes to " + bytes.length
                    + " bytes, but at least " + MIN_JWT_KEY_BYTES + " are required.");
        }
    }
}
