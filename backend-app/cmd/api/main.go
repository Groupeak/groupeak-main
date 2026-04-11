package main

import (
	"context"
	"log"
	"log/slog"
	"net/http"
	"os"

	"groupeak/internal/config"
	"groupeak/internal/db"
	"groupeak/internal/eventbus"
	"groupeak/internal/handlers"
	"groupeak/internal/repository"
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

	// 1. Загружаем конфигурацию приложения
	cfg := config.Load()

	// 2. Подключаемся к PostgreSQL и накатываем миграции
	sqlDB, err := db.NewPostgres(cfg.DBDsn, "./migrations")
	if err != nil {
		log.Fatalf("connect to db / run migrations: %v", err)
	}
	defer sqlDB.Close()

	// 3. Инициализируем S3 клиент (MinIO)
	minioClient, err := minio.New(cfg.S3Endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(cfg.S3AccessKey, cfg.S3SecretKey, ""),
		Secure: false,
	})
	if err != nil {
		log.Fatalf("failed to init minio client: %v", err)
	}

	// Инициализируем логгер
	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))

	// 4. Инициализируем Шину Событий (EventBus)
	bus := eventbus.NewEventBus(logger)

	// 5. Создаём ВСЕ репозитории первыми (добавили notifRepo)
	taskRepo := repository.NewTaskRepository()
	projectRepo := repository.NewProjectRepository()
	userRepo := repository.NewUserRepository()
	eventRepo := repository.NewEventRepository()
	notifRepo := repository.NewNotificationRepository()

	// 6. Создаём ВСЕ сервисы
	// Важно: прокидываем bus в те сервисы, которые генерируют события
	authService := services.NewAuthService(sqlDB, userRepo, cfg.JWTToken, logger)
	taskService := services.NewTaskService(sqlDB, taskRepo, logger, bus)
	projectService := services.NewProjectService(sqlDB, projectRepo, logger, bus)
	eventService := services.NewEventService(sqlDB, eventRepo, logger, bus)

	// Создаем сервис уведомлений (он будет слушать шину)
	notifService := services.NewNotificationService(sqlDB, notifRepo, taskRepo, eventRepo, logger)

	// 7. Запускаем фоновый воркер шины событий
	bus.SetHandler(notifService.HandleEvent)
	bus.Start(context.Background())
	defer bus.Stop() // Гарантирует, что при выключении сервера все эвенты из очереди допишутся в БД

	// 8. Создаём HTTP-хендлеры (добавили notificationHandler)
	authHandler := handlers.NewAuthHandler(authService, minioClient, cfg.S3Bucket, cfg.S3Endpoint)
	projectHandler := handlers.NewProjectHandler(projectService)
	taskHandler := handlers.NewTaskHandler(taskService)
	eventHandler := handlers.NewEventHandler(eventService)
	notificationHandler := handlers.NewNotificationHandler(notifService)

	// 9. Собираем роутер (прокинули notificationHandler)
	r := router.NewRouter(
		authHandler,
		projectHandler,
		taskHandler,
		eventHandler,
		notificationHandler,
		[]byte(cfg.JWTToken),
	)

	// 10. Запускаем HTTP-сервер
	addr := ":" + cfg.HTTPPort
	log.Printf("starting HTTP server on %s\n", addr)

	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatalf("ListenAndServe: %v", err)
	}
}
