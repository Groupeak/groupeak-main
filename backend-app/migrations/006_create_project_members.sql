-- +goose Up

-- Связка проектов и участников (owner + invited members)
CREATE TABLE IF NOT EXISTS project_members (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role       TEXT NOT NULL CHECK (role IN ('owner', 'member')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT project_members_unique_member UNIQUE (project_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_project_members_project_id
    ON project_members(project_id);

CREATE INDEX IF NOT EXISTS idx_project_members_user_id
    ON project_members(user_id);

-- +goose Down

DROP TABLE IF EXISTS project_members;
