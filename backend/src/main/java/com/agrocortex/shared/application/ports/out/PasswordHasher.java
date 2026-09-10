package com.agrocortex.shared.application.ports.out;

/**
 * Application port for password hashing and verification.
 * Concrete algorithms (BCrypt, Argon2, etc.) remain infrastructure concerns.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
