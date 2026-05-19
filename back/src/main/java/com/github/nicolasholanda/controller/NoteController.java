package com.github.nicolasholanda.controller;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;

@Path("/api/notes")
public class NoteController {

    @GET
    public String getNote() {
        return "Hello, World!";
    }
}