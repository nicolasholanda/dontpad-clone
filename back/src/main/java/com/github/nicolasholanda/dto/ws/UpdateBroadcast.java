package com.github.nicolasholanda.dto.ws;

public record UpdateBroadcast(MessageType type, String content, int version, String originSessionId) {

    public UpdateBroadcast {
        type = MessageType.UPDATE;
    }

    public UpdateBroadcast(String content, int version, String originSessionId) {
        this(MessageType.UPDATE, content, version, originSessionId);
    }
}
