package com.agrocortex.auth.infrastructure.security;

import com.agrocortex.shared.application.ports.out.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public final class JwtTokenProvider implements TokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiration-seconds:900}") long accessTokenExpirationSeconds,
            @Value("${security.jwt.issuer:agrocortex-api}") String issuer
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("security.jwt.secret must contain at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.issuer = issuer;
    }

    @Override
    public String generateAccessToken(UUID subject) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(subject.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @Override
    public UUID extractSubject(String token) {
        return UUID.fromString(parse(token).getSubject());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
