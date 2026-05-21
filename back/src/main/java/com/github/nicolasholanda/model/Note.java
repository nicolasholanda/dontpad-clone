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

    public static final Duration DEFAULT_TTL = Duration.ofDays(90);

    public static Note from(String path) {
        Instant now = Instant.now();
        return Note.builder()
            .path(path)
            .content("")
            .createdAt(now)
            .updatedAt(now)
            .expiresAt(now.plus(DEFAULT_TTL))
            .version(1)
            .passwordHash(null)
            .build();
    }
}