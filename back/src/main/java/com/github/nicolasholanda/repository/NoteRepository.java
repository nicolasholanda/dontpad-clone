package com.github.nicolasholanda.repository;

import com.github.nicolasholanda.model.Note;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class NoteRepository implements PanacheRepository<Note> {

    public Note create(Note note) {
        persist(note);
        return note;
    }

}