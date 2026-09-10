package com.agrocortex.auth.application.ports.out;

import com.agrocortex.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

/** Persistence port owned by the application/domain boundary. */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);
}
