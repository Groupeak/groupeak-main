-- +goose Up

-- Приглашения в проект по email
CREATE TABLE IF NOT EXISTS project_invites (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,

    email TEXT NOT NULL,
    token TEXT NOT NULL UNIQUE,

    status TEXT NOT NULL DEFAULT 'pending'
    CHECK (status IN ('pending', 'accepted', 'cancelled', 'expired')),

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_project_invites_project_id
    ON project_invites(project_id);

CREATE INDEX IF NOT EXISTS idx_project_invites_email
    ON project_invites(email);

-- +goose Down

DROP TABLE IF EXISTS project_invites;
