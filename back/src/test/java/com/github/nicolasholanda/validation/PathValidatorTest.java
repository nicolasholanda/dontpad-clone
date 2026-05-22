package com.github.nicolasholanda.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathValidatorTest {

    @Test
    void acceptsSimpleAlphanumeric() {
        assertTrue(PathValidator.isValid("test"));
        assertTrue(PathValidator.isValid("abc123"));
        assertTrue(PathValidator.isValid("a"));
    }

    @Test
    void acceptsDashesUnderscoresAndSlashes() {
        assertTrue(PathValidator.isValid("my-note"));
        assertTrue(PathValidator.isValid("my_note"));
        assertTrue(PathValidator.isValid("team/standup"));
        assertTrue(PathValidator.isValid("a/b/c"));
    }

    @Test
    void acceptsMaxLength() {
        assertTrue(PathValidator.isValid("a".repeat(200)));
    }

    @Test
    void rejectsNull() {
        assertFalse(PathValidator.isValid(null));
    }

    @Test
    void rejectsEmpty() {
        assertFalse(PathValidator.isValid(""));
    }

    @Test
    void rejectsSpaces() {
        assertFalse(PathValidator.isValid("my note"));
    }

    @Test
    void rejectsDisallowedSpecialChars() {
        assertFalse(PathValidator.isValid("test!"));
        assertFalse(PathValidator.isValid("test?"));
        assertFalse(PathValidator.isValid("test.txt"));
        assertFalse(PathValidator.isValid("hello@world"));
        assertFalse(PathValidator.isValid("a#b"));
    }

    @Test
    void rejectsOver200Chars() {
        assertFalse(PathValidator.isValid("a".repeat(201)));
    }
}
