package com.github.nicolasholanda.dto.ws;

public enum ErrorCode {
    INVALID_PATH("path is not valid");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
