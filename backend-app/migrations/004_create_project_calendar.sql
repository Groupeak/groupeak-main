-- +goose Up

-- Таблица календаря проекта
CREATE TABLE IF NOT EXISTS project_calendar (
    id BIGSERIAL PRIMARY KEY,
    project_id   BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    role         TEXT NOT NULL DEFAULT 'all'
        CHECK (role IN ('owner', 'member', 'all')),
    title        TEXT NOT NULL,
    description  TEXT,
    meeting_link TEXT,
    event_at     TIMESTAMPTZ NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_calendar_project_id
    ON project_calendar(project_id);

-- +goose Down

DROP TABLE IF EXISTS project_calendar;
