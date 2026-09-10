package com.agrocortex.auth.domain;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Role aggregates the permissions granted to a user.
 */
public record Role(String code, boolean active, Set<Permission> permissions) {

    public Role {
        Objects.requireNonNull(code, "Role code must not be null");
        if (code.isBlank()) {
            throw new IllegalArgumentException("Role code must not be blank");
        }
        permissions = permissions == null
                ? Set.of()
                : Set.copyOf(permissions);
    }

    public Set<Permission> permissions() {
        return Collections.unmodifiableSet(permissions);
    }
}
