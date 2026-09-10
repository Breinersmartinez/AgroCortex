CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_roles (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_app_user_roles_user
        FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_user_roles_role
        FOREIGN KEY (role_id) REFERENCES app_roles(id) ON DELETE CASCADE
);

CREATE TABLE app_role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_app_role_permissions_role
        FOREIGN KEY (role_id) REFERENCES app_roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_role_permissions_permission
        FOREIGN KEY (permission_id) REFERENCES app_permissions(id) ON DELETE CASCADE
);

CREATE INDEX idx_app_users_email ON app_users (email);
CREATE INDEX idx_app_user_roles_role ON app_user_roles (role_id);
CREATE INDEX idx_app_role_permissions_permission ON app_role_permissions (permission_id);
