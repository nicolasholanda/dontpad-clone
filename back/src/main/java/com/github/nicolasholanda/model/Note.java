package com.github.nicolasholanda.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;

@Data
@Entity
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
}