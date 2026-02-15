-- +goose Up

-- 1) Сносим старый CHECK, если есть
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_position_check;

-- 2) Делаем position nullable (раз у тебя теперь pointer и ты хочешь null)
ALTER TABLE users
    ALTER COLUMN position DROP NOT NULL;

-- 3) Нормализуем существующие данные под новый строгий список
--    Всё неизвестное -> NULL (чтобы не ломать CHECK)
UPDATE users
SET position = CASE LOWER(TRIM(position))
    WHEN 'frontend' THEN 'Frontend'
    WHEN 'backend' THEN 'Backend'
    WHEN 'uiux' THEN 'Designer'
    WHEN 'designer' THEN 'Designer'
    WHEN 'qa' THEN 'QA'
    WHEN 'qa automation' THEN 'QA'
    WHEN 'devops' THEN 'DevOps'
    WHEN 'android' THEN 'Android'
    WHEN 'ios' THEN 'iOS'
    WHEN 'data analyst' THEN 'Data Analyst'
    WHEN 'product' THEN 'Product'
    WHEN 'pm' THEN 'Product'
    WHEN 'hr' THEN 'HR'
    ELSE NULL
END;

-- 4) Добавляем строгий CHECK (теперь данные уже приведены)
ALTER TABLE users
    ADD CONSTRAINT users_position_check
    CHECK (
        position IS NULL OR position IN (
            'Frontend',
            'Backend',
            'Designer',
            'QA',
            'DevOps',
            'Android',
            'iOS',
            'Data Analyst',
            'Product',
            'HR'
        )
    );

-- +goose Down

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_position_check;

-- обратно NOT NULL НЕ возвращаю, потому что по логике проекта position может быть NULL
