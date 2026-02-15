-- +goose Up
ALTER TABLE users
    ALTER COLUMN position DROP NOT NULL;

-- +goose Down
ALTER TABLE users
    ALTER COLUMN position SET NOT NULL;
