package com.github.nicolasholanda.service;

import com.github.nicolasholanda.model.LiveNote;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class LiveNoteCache {

    private static final long LIVE_TTL_SECONDS = Duration.ofHours(1).toSeconds();

    @Inject
    RedisDataSource redis;

    private ValueCommands<String, LiveNote> commands;

    @PostConstruct
    void init() {
        commands = redis.value(String.class, LiveNote.class);
    }

    public Optional<LiveNote> get(String path) {
        return Optional.ofNullable(commands.get(key(path)));
    }

    public void set(String path, String content, int version) {
        LiveNote live = new LiveNote(content, version, Instant.now().toEpochMilli());
        commands.setex(key(path), LIVE_TTL_SECONDS, live);
    }

    private String key(String path) {
        return "note:live:" + path;
    }
}
