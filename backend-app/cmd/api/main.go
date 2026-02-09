package main

import (
	"log"
	"net/http"

	"groupeak/internal/config"
	"groupeak/internal/db"
	"groupeak/internal/handlers"
	"groupeak/internal/router"
	"groupeak/internal/services"

	"github.com/joho/godotenv"
)

func main() {
	// Загружаем переменные окружения из .env
	if err := godotenv.Load(); err != nil {
		log.Println("no .env file found, using OS env only")
	}

	// 1. Загружаем конфигурацию приложения
	cfg := config.Load()

	// 2. Подключаемся к PostgreSQL и накатываем миграции
	sqlDB, err := db.NewPostgres(cfg.DBDsn, "./migrations")
	if err != nil {
		log.Fatalf("connect to db / run migrations: %v", err)
	}
	defer sqlDB.Close()

	// 3. Создаём сервисы (бизнес-логика)
	authService := services.NewAuthService(sqlDB, cfg.JWTToken)
	projectService := services.NewProjectService(sqlDB)
	taskService := services.NewTaskService(sqlDB)

	// 4. Создаём HTTP-хендлеры
	authHandler := handlers.NewAuthHandler(authService)
	projectHandler := handlers.NewProjectHandler(projectService)
	taskHandler := handlers.NewTaskHandler(taskService)

	// 5. Собираем роутер
	r := router.NewRouter(
		authHandler,
		projectHandler,
		taskHandler,
		[]byte(cfg.JWTToken),
	)

	// 6. Запускаем HTTP-сервер
	addr := ":" + cfg.HTTPPort
	log.Printf("starting HTTP server on %s\n", addr)

	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatalf("ListenAndServe: %v", err)
	}
}
