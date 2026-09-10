package com.agrocortex.auth.domain;

import com.agrocortex.shared.domain.AggregateRoot;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Authentication aggregate. Persistence and Spring Security concerns stay outside the domain.
 */
public final class User implements AggregateRoot {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final Set<Role> roles;

    public User(UUID id, String email, String passwordHash) {
        this(id, email, passwordHash, true, Set.of());
    }

    public User(UUID id, String email, String passwordHash, boolean active, Set<Role> roles) {
        this.id = Objects.requireNonNull(id, "User id must not be null");
        this.email = Objects.requireNonNull(email, "User email must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash must not be null");
        this.active = active;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
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

    public boolean active() {
        return active;
    }

    public Set<Role> roles() {
        return Collections.unmodifiableSet(roles);
    }
}
