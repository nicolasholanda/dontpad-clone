package com.github.nicolasholanda.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nicolasholanda.repository.NoteRepository;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class NoteWebSocketTest {

    @TestHTTPResource("/ws/syncpath")
    URI httpUri;

    @Inject
    NoteRepository noteRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    @Transactional
    void clean() {
        noteRepository.deleteAll();
    }

    @Test
    void updateFromOneClientReachesOtherWithin500ms() throws Exception {
        URI wsUri = URI.create(httpUri.toString().replaceFirst("^http", "ws"));

        BlockingQueue<String> clientAMessages = new LinkedBlockingQueue<>();
        BlockingQueue<String> clientBMessages = new LinkedBlockingQueue<>();

        WebSocket clientA = openWebSocket(wsUri, clientAMessages);
        WebSocket clientB = openWebSocket(wsUri, clientBMessages);

        assertType(pollOrFail(clientAMessages, 2000), "init");
        assertType(pollOrFail(clientBMessages, 2000), "init");

        clientA.sendText("{\"type\":\"update\",\"content\":\"hello\",\"baseVersion\":0}", true);

        String received = clientBMessages.poll(500, TimeUnit.MILLISECONDS);
        assertNotNull(received, "client B did not receive update within 500ms");

        JsonNode node = mapper.readTree(received);
        assertEquals("update", node.get("type").asText());
        assertEquals("hello", node.get("content").asText());
        assertEquals(1, node.get("version").asInt());

        clientA.sendClose(WebSocket.NORMAL_CLOSURE, "");
        clientB.sendClose(WebSocket.NORMAL_CLOSURE, "");
    }

    private WebSocket openWebSocket(URI uri, BlockingQueue<String> messages) throws Exception {
        return HttpClient.newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(uri, new WebSocket.Listener() {
                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    messages.offer(data.toString());
                    webSocket.request(1);
                    return null;
                }
            })
            .get(2, TimeUnit.SECONDS);
    }

    private String pollOrFail(BlockingQueue<String> queue, long ms) throws Exception {
        String value = queue.poll(ms, TimeUnit.MILLISECONDS);
        assertNotNull(value, "expected a message within " + ms + "ms");
        return value;
    }

    private void assertType(String json, String expected) throws Exception {
        JsonNode node = mapper.readTree(json);
        assertEquals(expected, node.get("type").asText());
    }
}
