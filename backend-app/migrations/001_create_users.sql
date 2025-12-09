-- +goose Up

-- Таблица пользователей
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

-- +goose Down

DROP TABLE IF EXISTS users;
