package com.agrocortex.auth.infrastructure.persistence;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_roles")
public class RoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "app_role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionJpaEntity> permissions = new HashSet<>();

    protected RoleJpaEntity() {
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public boolean isActive() { return active; }
    public Set<PermissionJpaEntity> getPermissions() { return permissions; }
}
