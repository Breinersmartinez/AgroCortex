package com.agrocortex.auth.application.login;

import com.agrocortex.auth.application.ports.out.UserRepository;
import com.agrocortex.auth.domain.User;
import com.agrocortex.shared.application.ports.out.PasswordHasher;
import com.agrocortex.shared.application.ports.out.TokenProvider;

/**
 * Orchestrates the login use case without depending on Spring, JPA, JWT or HTTP.
 */
public final class LoginHandler implements LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public LoginHandler(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public LoginResult execute(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return new LoginResult(
                user.id(),
                tokenProvider.generateAccessToken(user.id())
        );
    }
}
