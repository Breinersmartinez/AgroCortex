package com.agrocortex.auth.infrastructure.persistence;

import com.agrocortex.auth.application.ports.out.UserRepository;
import com.agrocortex.auth.domain.Permission;
import com.agrocortex.auth.domain.Role;
import com.agrocortex.auth.domain.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public final class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserJpaRepository repository;

    public UserRepositoryAdapter(SpringDataUserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    private User toDomain(UserJpaEntity entity) {
        Set<Role> roles = entity.getRoles().stream()
                .filter(RoleJpaEntity::isActive)
                .map(role -> new Role(
                        role.getCode(),
                        true,
                        role.getPermissions().stream()
                                .filter(PermissionJpaEntity::isActive)
                                .map(permission -> new Permission(permission.getCode(), true))
                                .collect(Collectors.toUnmodifiableSet())
                ))
                .collect(Collectors.toUnmodifiableSet());

        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.isActive(),
                roles
        );
    }
}
