package com.github.nicolasholanda.service;

import com.github.nicolasholanda.repository.NoteRepository;
import jakarta.inject.Inject;
import java.time.Duration;
import com.github.nicolasholanda.model.Note;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;


@ApplicationScoped
public class NoteService {

    @Inject
    private NoteRepository noteRepository;

    public Note getNoteByPath(String path) {
        return noteRepository.findByIdOptional(path)
            .orElse(createNote(path));
    }

    @Transactional
    public Note createNote(String path) {
        Note note = Note.from(path, Duration.ofDays(30));
        return noteRepository.create(note);
    }
}