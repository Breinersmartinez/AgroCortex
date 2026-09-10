package com.agrocortex.auth.application.login;

import java.util.UUID;

public record LoginResult(
        UUID userId,
        String accessToken
) {
}
