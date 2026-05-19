package com.github.nicolasholanda.repository;

import com.github.nicolasholanda.model.Note;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NoteRepository implements PanacheRepositoryBase<Note, String> {

    public Note create(Note note) {
        persist(note);
        return note;
    }

}