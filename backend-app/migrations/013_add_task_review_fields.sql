-- +goose Up

ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS status_before_review TEXT,
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS reviewed_by BIGINT,
    ADD COLUMN IF NOT EXISTS review_comment TEXT;

-- +goose StatementBegin
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.constraint_name = 'tasks_reviewed_by_fkey'
          AND tc.table_name = 'tasks'
    ) THEN
        ALTER TABLE tasks
            ADD CONSTRAINT tasks_reviewed_by_fkey
            FOREIGN KEY (reviewed_by) REFERENCES users(id)
            ON DELETE SET NULL;
    END IF;
END $$;
-- +goose StatementEnd

-- +goose StatementBegin
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'tasks'
          AND constraint_type = 'CHECK'
          AND constraint_name = 'tasks_status_check'
    ) THEN
        ALTER TABLE tasks DROP CONSTRAINT tasks_status_check;
    END IF;

    ALTER TABLE tasks
        ADD CONSTRAINT tasks_status_check
        CHECK (status IN ('todo', 'in_progress', 'in_review', 'done', 'postponed'));
END $$;
-- +goose StatementEnd


-- +goose Down

-- +goose StatementBegin
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.constraint_name = 'tasks_reviewed_by_fkey'
          AND tc.table_name = 'tasks'
    ) THEN
        ALTER TABLE tasks DROP CONSTRAINT tasks_reviewed_by_fkey;
    END IF;
END $$;
-- +goose StatementEnd

-- +goose StatementBegin
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'tasks'
          AND constraint_type = 'CHECK'
          AND constraint_name = 'tasks_status_check'
    ) THEN
        ALTER TABLE tasks DROP CONSTRAINT tasks_status_check;
    END IF;

    ALTER TABLE tasks
        ADD CONSTRAINT tasks_status_check
        CHECK (status IN ('todo', 'in_progress', 'done', 'postponed'));
END $$;
-- +goose StatementEnd

ALTER TABLE tasks
    DROP COLUMN IF EXISTS status_before_review,
    DROP COLUMN IF EXISTS submitted_at,
    DROP COLUMN IF EXISTS reviewed_at,
    DROP COLUMN IF EXISTS reviewed_by,
    DROP COLUMN IF EXISTS review_comment;