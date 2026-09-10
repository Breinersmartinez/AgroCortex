package com.agrocortex.auth.infrastructure;

import com.agrocortex.auth.application.login.LoginHandler;
import com.agrocortex.auth.application.login.LoginUseCase;
import com.agrocortex.auth.application.ports.out.UserRepository;
import com.agrocortex.shared.application.ports.out.PasswordHasher;
import com.agrocortex.shared.application.ports.out.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    @Bean
    LoginUseCase loginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider
    ) {
        return new LoginHandler(userRepository, passwordHasher, tokenProvider);
    }
}
