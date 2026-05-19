package com.github.nicolasholanda.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/healthz")
public class HealthController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> check() {
        return Map.of("status", "up");
    }
}
