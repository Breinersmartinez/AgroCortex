package com.agrocortex.auth.infrastructure.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "app_permissions")
public class PermissionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false)
    private boolean active = true;

    protected PermissionJpaEntity() {
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public boolean isActive() { return active; }
}
