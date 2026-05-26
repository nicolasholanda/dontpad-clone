package com.github.nicolasholanda.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Path("/{any:.+}")
@ApplicationScoped
public class SpaController {

    private String indexHtml;

    @PostConstruct
    void load() throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("META-INF/resources/index.html")) {
            if (in == null) {
                throw new IllegalStateException("index.html not found on classpath");
            }
            indexHtml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response serve() {
        return Response.ok(indexHtml).build();
    }
}
