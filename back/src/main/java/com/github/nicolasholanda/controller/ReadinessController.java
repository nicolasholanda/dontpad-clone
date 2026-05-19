package com.github.nicolasholanda.controller;

import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/readyz")
public class ReadinessController {

    @Inject
    DataSource dataSource;

    @Inject
    RedisDataSource redis;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response check() {
        Map<String, String> body = new LinkedHashMap<>();
        boolean postgresOk = checkPostgres(body);
        boolean redisOk = checkRedis(body);
        boolean allOk = postgresOk && redisOk;
        body.put("status", allOk ? "up" : "down");
        return Response.status(allOk ? 200 : 503).entity(body).build();
    }

    private boolean checkPostgres(Map<String, String> body) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            body.put("postgres", "up");
            return true;
        } catch (Exception e) {
            body.put("postgres", "down: " + e.getMessage());
            return false;
        }
    }

    private boolean checkRedis(Map<String, String> body) {
        try {
            var resp = redis.execute("PING");
            boolean ok = resp != null && "PONG".equalsIgnoreCase(resp.toString());
            body.put("redis", ok ? "up" : "down");
            return ok;
        } catch (Exception e) {
            body.put("redis", "down: " + e.getMessage());
            return false;
        }
    }
}
