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
	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

func main() {
	// Загружаем переменные окружения из .env
	if err := godotenv.Load(); err != nil {
		log.Println("no .env file found, using OS env only")
	}

	// 1. Загружаем конфигурацию приложения (убедись, что добавил S3 поля в config.go)
	cfg := config.Load()

	// 2. Подключаемся к PostgreSQL и накатываем миграции
	sqlDB, err := db.NewPostgres(cfg.DBDsn, "./migrations")
	if err != nil {
		log.Fatalf("connect to db / run migrations: %v", err)
	}
	defer sqlDB.Close()

	// 3. Инициализируем S3 клиент (MinIO)
	// Secure: false используется для локальной разработки без SSL (http://localhost:9000)
	minioClient, err := minio.New(cfg.S3Endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(cfg.S3AccessKey, cfg.S3SecretKey, ""),
		Secure: false,
	})
	if err != nil {
		log.Fatalf("failed to init minio client: %v", err)
	}

	// 4. Создаём сервисы (бизнес-логика)
	authService := services.NewAuthService(sqlDB, cfg.JWTToken)
	projectService := services.NewProjectService(sqlDB)
	taskService := services.NewTaskService(sqlDB)
	eventService := services.NewEventService(sqlDB)

	// 5. Создаём HTTP-хендлеры
	// Передаем клиент MinIO и имя бакета в AuthHandler
	authHandler := handlers.NewAuthHandler(authService, minioClient, cfg.S3Bucket, cfg.S3Endpoint)
	projectHandler := handlers.NewProjectHandler(projectService)
	taskHandler := handlers.NewTaskHandler(taskService)
	eventHandler := handlers.NewEventHandler(eventService)

	// 6. Собираем роутер
	r := router.NewRouter(
		authHandler,
		projectHandler,
		taskHandler,
		eventHandler,
		[]byte(cfg.JWTToken),
	)

	// 7. Запускаем HTTP-сервер
	addr := ":" + cfg.HTTPPort
	log.Printf("starting HTTP server on %s\n", addr)

	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatalf("ListenAndServe: %v", err)
	}
}
