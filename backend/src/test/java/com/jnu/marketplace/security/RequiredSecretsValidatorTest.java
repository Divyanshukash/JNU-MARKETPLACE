package com.jnu.marketplace.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequiredSecretsValidatorTest {

    @Test
    void missingKeyIsReportedByVariableNameOnly() {
        assertThatThrownBy(() -> RequiredSecretsValidator.validateJwtKey(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET_KEY is not set");
    }

    @Test
    void notBase64IsRejectedWithoutEchoingInput() {
        String bad = "not*base64*value!";

        assertThatThrownBy(() -> RequiredSecretsValidator.validateJwtKey(bad))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET_KEY is not valid base64.")
                .satisfies(e -> org.assertj.core.api.Assertions.assertThat(e.getMessage()).doesNotContain(bad));
    }

    @Test
    void tooShortKeyReportsLengthNotValue() {
        // "short" in base64 decodes to 5 bytes, below the 32-byte HS256 minimum.
        String tooShort = Base64.getEncoder().encodeToString("short".getBytes());

        assertThatThrownBy(() -> RequiredSecretsValidator.validateJwtKey(tooShort))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("decodes to 5 bytes")
                .satisfies(e -> org.assertj.core.api.Assertions.assertThat(e.getMessage()).doesNotContain(tooShort));
    }

    @Test
    void adequateKeyPassesValidation() {
        // Test-only key material, 32 zero bytes. It is not a real signing key.
        String keyForTest = Base64.getEncoder().encodeToString(new byte[32]);

        assertThatCode(() -> RequiredSecretsValidator.validateJwtKey(keyForTest)).doesNotThrowAnyException();
    }
}
