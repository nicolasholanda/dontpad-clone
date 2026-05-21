package com.github.nicolasholanda.repository;

import com.github.nicolasholanda.model.Note;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class NoteRepository implements PanacheRepositoryBase<Note, String> {

    public Note create(Note note) {
        persist(note);
        return note;
    }

    public Note upsert(String path, String content) {
        Instant now = Instant.now();
        Optional<Note> existing = findByIdOptional(path);
        Note note;
        if (existing.isPresent()) {
            note = existing.get();
            note.setVersion(note.getVersion() + 1);
        } else {
            note = Note.from(path);
        }
        note.setContent(content);
        note.setUpdatedAt(now);
        note.setExpiresAt(now.plus(Note.DEFAULT_TTL));
        persist(note);
        return note;
    }

}