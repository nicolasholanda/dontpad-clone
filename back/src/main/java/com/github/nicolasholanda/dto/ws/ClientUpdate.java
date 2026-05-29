package com.github.nicolasholanda.dto.ws;

public record ClientUpdate(MessageType type, String content, int baseVersion) {

    public ClientUpdate {
        type = MessageType.UPDATE;
    }
}
