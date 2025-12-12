-- +goose Up

ALTER TABLE users
    ADD COLUMN birth_date DATE,
    ADD COLUMN about TEXT;

-- +goose Down

ALTER TABLE users
    DROP COLUMN IF EXISTS birth_date,
    DROP COLUMN IF EXISTS about;
