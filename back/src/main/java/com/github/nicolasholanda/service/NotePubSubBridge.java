package com.github.nicolasholanda.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nicolasholanda.dto.ws.UpdateBroadcast;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.redis.datasource.pubsub.PubSubCommands.RedisSubscriber;
import io.quarkus.websockets.next.OpenConnections;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class NotePubSubBridge {

    @Inject
    RedisDataSource redis;

    @Inject
    OpenConnections openConnections;

    @Inject
    ObjectMapper mapper;

    @Inject
    MeterRegistry meterRegistry;

    private PubSubCommands<UpdateBroadcast> pubsub;

    private final Map<String, Set<String>> sessionIdsByPath = new ConcurrentHashMap<>();
    private final Map<String, RedisSubscriber> subscribersByPath = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        pubsub = redis.pubsub(UpdateBroadcast.class);
        meterRegistry.gauge("dontpad_active_websocket_connections", this, NotePubSubBridge::totalSessions);
    }

    public int totalSessions() {
        int total = 0;
        for (Set<String> sessions : sessionIdsByPath.values()) {
            total += sessions.size();
        }
        return total;
    }

    public void register(String path, String sessionId) {
        sessionIdsByPath.compute(path, (p, sessions) -> {
            if (sessions == null) {
                sessions = ConcurrentHashMap.newKeySet();
                subscribeChannel(p);
            }
            sessions.add(sessionId);
            return sessions;
        });
    }

    public void unregister(String path, String sessionId) {
        sessionIdsByPath.computeIfPresent(path, (p, sessions) -> {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                unsubscribeChannel(p);
                return null;
            }
            return sessions;
        });
    }

    public void publish(String path, UpdateBroadcast message) {
        pubsub.publish(channel(path), message);
    }

    private void subscribeChannel(String path) {
        RedisSubscriber subscriber = pubsub.subscribe(channel(path), message -> fanOut(path, message));
        subscribersByPath.put(path, subscriber);
    }

    private void unsubscribeChannel(String path) {
        RedisSubscriber subscriber = subscribersByPath.remove(path);
        if (subscriber != null) {
            subscriber.unsubscribe(channel(path));
        }
    }

    private void fanOut(String path, UpdateBroadcast message) {
        Set<String> sessionIds = sessionIdsByPath.get(path);
        if (sessionIds == null) {
            return;
        }
        String json;
        try {
            json = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return;
        }
        for (String sessionId : sessionIds) {
            if (sessionId.equals(message.originSessionId())) {
                continue;
            }
            openConnections.findByConnectionId(sessionId).ifPresent(conn -> {
                try {
                    conn.sendTextAndAwait(json);
                } catch (Exception ignored) {
                }
            });
        }
    }

    private String channel(String path) {
        return "note:channel:" + path;
    }
}
