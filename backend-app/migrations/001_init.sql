-- 1. Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name      TEXT NOT NULL,
    avatar_url     TEXT,
    email          TEXT NOT NULL UNIQUE,
    password_hash  TEXT NOT NULL,
    position       TEXT NOT NULL CHECK (position IN ('backend', 'frontend', 'uiux', 'pm')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Таблица проектов
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    owner_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    name       TEXT NOT NULL,
    description TEXT,
    deadline_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Таблица задач
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title       TEXT NOT NULL,
    description TEXT,
    comments    TEXT,
    status      TEXT NOT NULL DEFAULT 'todo'
        CHECK (status IN ('todo', 'in_progress', 'done', 'postponed')),
    priority    TEXT NOT NULL DEFAULT 'medium'
        CHECK (priority IN ('low', 'medium', 'high')),
    assignee_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    due_at      TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Таблица календаря проекта
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

-- 5. Таблица уведомлений
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

-- Индексы для ускорения выборок
CREATE INDEX IF NOT EXISTS idx_tasks_project_id
    ON tasks(project_id);

CREATE INDEX IF NOT EXISTS idx_tasks_assignee_id
    ON tasks(assignee_id);

CREATE INDEX IF NOT EXISTS idx_calendar_project_id
    ON project_calendar(project_id);

CREATE INDEX IF NOT EXISTS idx_notifications_project_id
    ON notifications(project_id);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX IF NOT EXISTS idx_notifications_task_id
    ON notifications(task_id);

CREATE INDEX IF NOT EXISTS idx_notifications_calendar_event_id
    ON notifications(calendar_event_id);
