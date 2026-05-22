package com.github.nicolasholanda.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentSizeValidatorTest {

    @Test
    void nullDoesNotExceed() {
        assertFalse(ContentSizeValidator.exceedsLimit(null));
    }

    @Test
    void emptyDoesNotExceed() {
        assertFalse(ContentSizeValidator.exceedsLimit(""));
    }

    @Test
    void exactlyMaxBytesDoesNotExceed() {
        String content = "a".repeat(ContentSizeValidator.MAX_BYTES);
        assertFalse(ContentSizeValidator.exceedsLimit(content));
    }

    @Test
    void oneByteOverMaxExceeds() {
        String content = "a".repeat(ContentSizeValidator.MAX_BYTES + 1);
        assertTrue(ContentSizeValidator.exceedsLimit(content));
    }

    @Test
    void multibyteUtf8CharsCountedAsBytes() {
        int charsAtTwoBytesEach = ContentSizeValidator.MAX_BYTES / 2 + 1;
        String content = "é".repeat(charsAtTwoBytesEach);
        assertTrue(ContentSizeValidator.exceedsLimit(content));
    }
}
