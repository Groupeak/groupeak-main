-- +goose Up

-- Расширяем список допустимых ролей пользователей
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_position_check;

ALTER TABLE users
    ADD CONSTRAINT users_position_check
    CHECK (position IN (
        -- старые роли
        'backend',
        'frontend',
        'uiux',
        'pm',

        -- новые роли
        'fullstack',
        'mobile',
        'devops',
        'software architect',
        'tech lead',
        'qa automation',
        'data engineer',
        'machine learning engineer',
        'embedded',
        'game',
        'blockchain',
        'systems programmer'
    ));

-- +goose Down

-- Возвращаем исходный набор ролей
ALTER TABLE users
    DROP CONSTRAINT IF EXISTS users_position_check;

ALTER TABLE users
    ADD CONSTRAINT users_position_check
    CHECK (position IN (
        'backend',
        'frontend',
        'uiux',
        'pm'
    ));
