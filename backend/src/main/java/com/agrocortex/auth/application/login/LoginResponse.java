package com.agrocortex.auth.application.login;

import java.util.UUID;

public record LoginResponse(UUID userId, String accessToken) {
}
