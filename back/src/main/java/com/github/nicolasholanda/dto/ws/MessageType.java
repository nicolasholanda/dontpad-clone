package com.github.nicolasholanda.dto.ws;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    INIT("init"),
    UPDATE("update"),
    ERROR("error");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static MessageType from(String value) {
        for (MessageType t : values()) {
            if (t.value.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("unknown message type: " + value);
    }
}
