package com.agrocortex.auth.infrastructure.security;

import com.agrocortex.shared.application.ports.out.TokenProvider;

import java.util.UUID;

/**
 * Infrastructure adapter placeholder for the JWT implementation.
 * JWT library details must remain isolated here.
 */
public final class JwtTokenProvider implements TokenProvider {

    @Override
    public String generateAccessToken(UUID subject) {
        throw new UnsupportedOperationException("JWT adapter not implemented yet");
    }

    @Override
    public boolean isValid(String token) {
        throw new UnsupportedOperationException("JWT adapter not implemented yet");
    }

    @Override
    public UUID extractSubject(String token) {
        throw new UnsupportedOperationException("JWT adapter not implemented yet");
    }
}
