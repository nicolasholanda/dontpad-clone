package com.github.nicolasholanda.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.Duration;


@Data
@Builder
@Entity
@EqualsAndHashCode(callSuper=false)
@Table(name = "note")
public class Note extends PanacheEntityBase {
    @Id
    private String path;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
    private int version;
    private String passwordHash;

    public static Note from(String path, Duration expiration) {
        return Note.builder()
            .path(path)
            .content("")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .expiresAt(Instant.now().plus(expiration))
            .version(0)
            .passwordHash("")
            .build();
    }
}