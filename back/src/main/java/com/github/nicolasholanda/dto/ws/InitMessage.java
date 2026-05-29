package com.github.nicolasholanda.dto.ws;

public record InitMessage(MessageType type, String content, int version) {

    public InitMessage {
        type = MessageType.INIT;
    }

    public InitMessage(String content, int version) {
        this(MessageType.INIT, content, version);
    }
}
