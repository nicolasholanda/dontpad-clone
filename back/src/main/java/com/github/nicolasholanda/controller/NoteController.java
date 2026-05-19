package com.github.nicolasholanda.controller;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.inject.Inject;
import com.github.nicolasholanda.service.NoteService;
import jakarta.ws.rs.PathParam;

@Path("/api/notes")
public class NoteController {

    @Inject
    private NoteService noteService;

    @GET
    @Path("/{path}")
    public String getNote(@PathParam("path") String path) {
        return noteService.getNoteByPath(path).getContent();
    }
}