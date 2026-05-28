package com.github.nicolasholanda.dto.ws;

public record UpdateBroadcast(String type, String content, int version, String originSessionId) {
}
