package com.agrocortex.shared.application.ports.out;

import java.util.UUID;

/**
 * Application port for issuing and validating authentication tokens.
 * The application layer does not depend on JWT or any concrete security library.
 */
public interface TokenProvider {

    String generateAccessToken(UUID subject);

    boolean isValid(String token);

    UUID extractSubject(String token);
}
