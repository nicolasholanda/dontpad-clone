package com.github.nicolasholanda.validation;

import java.util.regex.Pattern;

public final class PathValidator {

    private static final Pattern VALID_PATH = Pattern.compile("^[a-zA-Z0-9/_-]{1,200}$");

    private PathValidator() {
    }

    public static boolean isValid(String path) {
        return path != null && VALID_PATH.matcher(path).matches();
    }
}
