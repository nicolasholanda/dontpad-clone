package com.github.nicolasholanda.controller;

import com.github.nicolasholanda.dto.NoteContentRequest;
import com.github.nicolasholanda.dto.NoteResponse;
import com.github.nicolasholanda.model.Note;
import com.github.nicolasholanda.service.NoteService;
import com.github.nicolasholanda.validation.ContentSizeValidator;
import com.github.nicolasholanda.validation.PathValidator;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/notes")
public class NoteController {

    @Inject
    private NoteService noteService;

    @GET
    @Path("/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNote(@PathParam("path") String path) {
        if (!PathValidator.isValid(path)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return noteService.findNote(path)
            .map(note -> Response.ok(new NoteResponse(note.getContent(), note.getVersion())).build())
            .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{path}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upsertNote(@PathParam("path") String path, NoteContentRequest request) {
        if (!PathValidator.isValid(path)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (request == null || request.content() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (ContentSizeValidator.exceedsLimit(request.content())) {
            return Response.status(413).build();
        }
        Note note = noteService.upsertNote(path, request.content());
        return Response.ok(new NoteResponse(note.getContent(), note.getVersion())).build();
    }

    @GET
    @Path("/{path}/meta")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMeta(@PathParam("path") String path) {
        if (!PathValidator.isValid(path)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(noteService.getMeta(path)).build();
    }
}
