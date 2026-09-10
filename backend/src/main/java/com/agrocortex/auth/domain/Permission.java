package com.agrocortex.auth.domain;

import java.util.Objects;

/**
 * Application permission used for fine-grained authorization.
 * Example: CONSULTATIONS_READ, DIAGNOSES_CREATE.
 */
public record Permission(String code, boolean active) {

    public Permission {
        Objects.requireNonNull(code, "Permission code must not be null");
        if (code.isBlank()) {
            throw new IllegalArgumentException("Permission code must not be blank");
        }
    }
}
