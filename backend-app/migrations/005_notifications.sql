-- +goose Up

-- Таблица уведомлений
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    project_id        BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id           BIGINT REFERENCES users(id) ON DELETE SET NULL,
    task_id           BIGINT REFERENCES tasks(id) ON DELETE SET NULL,
    calendar_event_id BIGINT REFERENCES project_calendar(id) ON DELETE SET NULL,
    title       TEXT NOT NULL,
    message     TEXT,
    position    TEXT,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_project_id
    ON notifications(project_id);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX IF NOT EXISTS idx_notifications_task_id
    ON notifications(task_id);

CREATE INDEX IF NOT EXISTS idx_notifications_calendar_event_id
    ON notifications(calendar_event_id);

-- +goose Down

DROP TABLE IF EXISTS notifications;
