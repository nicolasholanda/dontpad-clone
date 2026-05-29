package com.github.nicolasholanda.service;

import com.github.nicolasholanda.dto.ws.UpdateBroadcast;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.quarkus.redis.datasource.pubsub.PubSubCommands.RedisSubscriber;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@ApplicationScoped
public class NotePubSubBridge {

    @Inject
    RedisDataSource redis;

    private PubSubCommands<UpdateBroadcast> pubsub;

    private final Map<String, Map<String, Consumer<UpdateBroadcast>>> sessionsByPath = new ConcurrentHashMap<>();
    private final Map<String, RedisSubscriber> subscribersByPath = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        pubsub = redis.pubsub(UpdateBroadcast.class);
    }

    public void register(String path, String sessionId, Consumer<UpdateBroadcast> handler) {
        sessionsByPath.compute(path, (p, sessions) -> {
            if (sessions == null) {
                sessions = new ConcurrentHashMap<>();
                subscribeChannel(p);
            }
            sessions.put(sessionId, handler);
            return sessions;
        });
    }

    public void unregister(String path, String sessionId) {
        sessionsByPath.computeIfPresent(path, (p, sessions) -> {
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
        Map<String, Consumer<UpdateBroadcast>> sessions = sessionsByPath.get(path);
        if (sessions == null) {
            return;
        }
        sessions.forEach((sessionId, handler) -> {
            if (!sessionId.equals(message.originSessionId())) {
                try {
                    handler.accept(message);
                } catch (Exception ignored) {
                }
            }
        });
    }

    private String channel(String path) {
        return "note:channel:" + path;
    }
}
