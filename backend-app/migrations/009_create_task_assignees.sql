-- +goose Up
-- Связь many-to-many: одна задача может иметь много исполнителей, один пользователь — много задач.

CREATE TABLE IF NOT EXISTS task_assignees (
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (task_id, user_id)
);

-- Ускоряет выборку всех задач конкретного пользователя
CREATE INDEX IF NOT EXISTS idx_task_assignees_user_id ON task_assignees(user_id);

-- +goose Down
DROP TABLE IF EXISTS task_assignees;
