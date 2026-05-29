package com.github.nicolasholanda.service;

import com.github.nicolasholanda.model.LiveNote;
import com.github.nicolasholanda.repository.NoteRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class PersistScheduler {

    private static final long DEBOUNCE_MS = Duration.ofSeconds(2).toMillis();

    @Inject
    LiveNoteCache liveCache;

    @Inject
    NoteRepository noteRepository;

    private ScheduledExecutorService scheduler;

    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "note-persist-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }

    public void schedule(String path) {
        ScheduledFuture<?> next = scheduler.schedule(() -> flush(path), DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        ScheduledFuture<?> previous = pending.put(path, next);
        if (previous != null) {
            previous.cancel(false);
        }
    }

    private void flush(String path) {
        pending.remove(path);
        Optional<LiveNote> live = liveCache.get(path);
        if (live.isEmpty()) {
            return;
        }
        LiveNote snapshot = live.get();
        QuarkusTransaction.requiringNew().run(() ->
            noteRepository.flushFromCache(path, snapshot.content(), snapshot.version())
        );
    }
}
