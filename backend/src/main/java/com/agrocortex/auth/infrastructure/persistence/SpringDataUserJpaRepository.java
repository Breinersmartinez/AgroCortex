package com.agrocortex.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findDetailedById(UUID id);
}
