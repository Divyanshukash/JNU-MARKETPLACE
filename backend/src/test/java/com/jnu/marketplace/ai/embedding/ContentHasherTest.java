package com.jnu.marketplace.ai.embedding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentHasherTest {

    @Test
    void identicalTextProducesIdenticalHash() {
        String text = "Title: Desk Lamp\nDonation: no";

        assertThat(ContentHasher.sha256(text)).isEqualTo(ContentHasher.sha256(text));
    }

    @Test
    void changedTextProducesDifferentHash() {
        assertThat(ContentHasher.sha256("Title: Desk Lamp"))
                .isNotEqualTo(ContentHasher.sha256("Title: Desk Lamps"));
    }

    @Test
    void hashIsLowercaseSha256Hex() {
        // SHA-256 of the empty string, a known test vector.
        assertThat(ContentHasher.sha256(""))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }
}
