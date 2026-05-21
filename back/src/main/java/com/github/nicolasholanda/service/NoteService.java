package com.github.nicolasholanda.service;

import com.github.nicolasholanda.dto.NoteMetaResponse;
import com.github.nicolasholanda.model.Note;
import com.github.nicolasholanda.repository.NoteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class NoteService {

    @Inject
    private NoteRepository noteRepository;

    @Transactional
    public Optional<Note> findNote(String path) {
        return noteRepository.findByIdOptional(path);
    }

    @Transactional
    public Note upsertNote(String path, String content) {
        return noteRepository.upsert(path, content);
    }

    @Transactional
    public NoteMetaResponse getMeta(String path) {
        return noteRepository.findByIdOptional(path)
            .map(note -> new NoteMetaResponse(true, note.getPasswordHash() != null))
            .orElseGet(() -> new NoteMetaResponse(false, false));
    }
}
