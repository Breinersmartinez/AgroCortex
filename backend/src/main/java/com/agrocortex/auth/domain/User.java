package com.agrocortex.auth.domain;

import com.agrocortex.shared.domain.AggregateRoot;

import java.util.UUID;

/**
 * Authentication aggregate. Persistence and Spring Security concerns are kept outside the domain.
 */
public final class User implements AggregateRoot {

    private final UUID id;
    private final String email;
    private final String passwordHash;

    public User(UUID id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }
}
