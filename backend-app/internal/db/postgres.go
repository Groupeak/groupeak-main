package db

import (
	"database/sql"
	"fmt"

	_ "github.com/jackc/pgx/v5/stdlib"
	"github.com/pressly/goose/v3"
)

// NewPostgres создаёт подключение к БД и накатывает миграции из migrationsDir.
func NewPostgres(dsn string, migrationsDir string) (*sql.DB, error) {
	db, err := sql.Open("pgx", dsn)
	if err != nil {
		return nil, fmt.Errorf("sql.Open: %w", err)
	}

	if err := db.Ping(); err != nil {
		_ = db.Close()
		return nil, fmt.Errorf("db.Ping: %w", err)
	}

	if err := goose.SetDialect("postgres"); err != nil {
		_ = db.Close()
		return nil, fmt.Errorf("goose.SetDialect: %w", err)
	}

	if err := goose.Up(db, migrationsDir); err != nil {
		_ = db.Close()
		return nil, fmt.Errorf("goose.Up: %w", err)
	}

	return db, nil
}
