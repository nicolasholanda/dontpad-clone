package com.github.nicolasholanda.dto.ws;

public record ErrorMessage(MessageType type, ErrorCode code, String message) {

    public ErrorMessage {
        type = MessageType.ERROR;
    }

    public ErrorMessage(ErrorCode code) {
        this(MessageType.ERROR, code, code.defaultMessage());
    }

    public ErrorMessage(ErrorCode code, String message) {
        this(MessageType.ERROR, code, message);
    }
}
