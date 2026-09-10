package com.agrocortex.auth.application.login;

public record LoginCommand(
        String email,
        String password
) {
}
