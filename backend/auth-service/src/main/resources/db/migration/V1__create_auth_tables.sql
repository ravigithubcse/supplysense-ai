-- Auth Service Schema Migration V1
-- Creates users, organizations, and related tables

CREATE TABLE IF NOT EXISTS auth.organizations (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(200) NOT NULL UNIQUE,
    slug        VARCHAR(200) NOT NULL UNIQUE,
    plan        VARCHAR(50)  NOT NULL DEFAULT 'BASIC',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS auth.users (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name            VARCHAR(100) NOT NULL,
    last_name             VARCHAR(100) NOT NULL,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    enabled               BOOLEAN      NOT NULL DEFAULT TRUE,
    account_locked        BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    mfa_enabled           BOOLEAN      NOT NULL DEFAULT FALSE,
    mfa_secret            VARCHAR(255),
    org_id                UUID REFERENCES auth.organizations(id),
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS auth.user_roles (
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Indexes
CREATE INDEX idx_users_email ON auth.users(email);
CREATE INDEX idx_users_org_id ON auth.users(org_id);

-- Seed default admin user (password: Admin1234!)
INSERT INTO auth.organizations (id, name, slug, plan)
VALUES ('00000000-0000-0000-0000-000000000001', 'SupplySense Demo', 'supplysense-demo', 'ENTERPRISE')
ON CONFLICT DO NOTHING;

INSERT INTO auth.users (id, first_name, last_name, email, password_hash, org_id)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Admin', 'User',
    'admin@ss.ai',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj3bp.Vx4VEi',
    '00000000-0000-0000-0000-000000000001'
) ON CONFLICT DO NOTHING;

INSERT INTO auth.user_roles (user_id, role) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN'),
    ('00000000-0000-0000-0000-000000000001', 'MANAGER')
ON CONFLICT DO NOTHING;

-- Seed manager user (password: Manager1234!)
INSERT INTO auth.users (id, first_name, last_name, email, password_hash, org_id)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'Sarah', 'Manager',
    'manager@ss.ai',
    '$2a$12$2XcbWbMTeBLxe/HkmtIAWuHRPBDnfCBCBFflLUST1gZ.XlJPIH.rS',
    '00000000-0000-0000-0000-000000000001'
) ON CONFLICT DO NOTHING;

INSERT INTO auth.user_roles (user_id, role) VALUES
    ('00000000-0000-0000-0000-000000000002', 'MANAGER'),
    ('00000000-0000-0000-0000-000000000002', 'ANALYST')
ON CONFLICT DO NOTHING;

-- Seed analyst user (password: Analyst1234!)
INSERT INTO auth.users (id, first_name, last_name, email, password_hash, org_id)
VALUES (
    '00000000-0000-0000-0000-000000000003',
    'Alex', 'Analyst',
    'analyst@ss.ai',
    '$2a$12$iXbMmWpVWXS5JVEeF5h6e.OUEwv7UCxT4HCi9kCfIQmJdN5gy8QIW',
    '00000000-0000-0000-0000-000000000001'
) ON CONFLICT DO NOTHING;

INSERT INTO auth.user_roles (user_id, role) VALUES
    ('00000000-0000-0000-0000-000000000003', 'ANALYST')
ON CONFLICT DO NOTHING;
