package com.github.nicolasholanda.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nicolasholanda.dto.ws.ClientUpdate;
import com.github.nicolasholanda.dto.ws.ErrorCode;
import com.github.nicolasholanda.dto.ws.ErrorMessage;
import com.github.nicolasholanda.dto.ws.InitMessage;
import com.github.nicolasholanda.dto.ws.UpdateBroadcast;
import com.github.nicolasholanda.model.LiveNote;
import com.github.nicolasholanda.model.Note;
import com.github.nicolasholanda.repository.NoteRepository;
import com.github.nicolasholanda.service.LiveNoteCache;
import com.github.nicolasholanda.service.NotePubSubBridge;
import com.github.nicolasholanda.service.PersistScheduler;
import com.github.nicolasholanda.validation.PathValidator;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

import java.util.Optional;

@WebSocket(path = "/ws/{path}")
public class NoteWebSocket {

    private static final String PATH_PARAM = "path";

    @Inject
    WebSocketConnection connection;

    @Inject
    LiveNoteCache liveCache;

    @Inject
    NoteRepository noteRepository;

    @Inject
    NotePubSubBridge bridge;

    @Inject
    PersistScheduler persistScheduler;

    @Inject
    ObjectMapper mapper;

    @OnOpen
    public void onOpen() {
        String path = connection.pathParam(PATH_PARAM);
        if (!PathValidator.isValid(path)) {
            send(new ErrorMessage(ErrorCode.INVALID_PATH));
            connection.closeAndAwait();
            return;
        }
        send(buildInit(path));
        bridge.register(path, connection.id(), this::send);
    }

    @OnTextMessage
    public void onMessage(ClientUpdate msg) {
        String path = connection.pathParam(PATH_PARAM);
        int newVersion = msg.baseVersion() + 1;
        liveCache.set(path, msg.content(), newVersion);
        bridge.publish(path, new UpdateBroadcast(msg.content(), newVersion, connection.id()));
        persistScheduler.schedule(path);
    }

    @OnClose
    public void onClose() {
        bridge.unregister(connection.pathParam(PATH_PARAM), connection.id());
    }

    private InitMessage buildInit(String path) {
        Optional<LiveNote> live = liveCache.get(path);
        if (live.isPresent()) {
            return new InitMessage(live.get().content(), live.get().version());
        }
        Optional<Note> fromDb = QuarkusTransaction.requiringNew().call(() ->
            noteRepository.findByIdOptional(path)
        );
        if (fromDb.isPresent()) {
            Note n = fromDb.get();
            liveCache.set(path, n.getContent(), n.getVersion());
            return new InitMessage(n.getContent(), n.getVersion());
        }
        return new InitMessage("", 0);
    }

    private void send(Object message) {
        try {
            connection.sendTextAndAwait(mapper.writeValueAsString(message));
        } catch (JsonProcessingException | RuntimeException ignored) {
        }
    }
}
