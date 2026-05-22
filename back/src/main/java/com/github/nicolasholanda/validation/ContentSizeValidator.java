package com.github.nicolasholanda.validation;

import java.nio.charset.StandardCharsets;

public final class ContentSizeValidator {

    public static final int MAX_BYTES = 100 * 1024;

    private ContentSizeValidator() {
    }

    public static boolean exceedsLimit(String content) {
        if (content == null) {
            return false;
        }
        return content.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES;
    }
}
