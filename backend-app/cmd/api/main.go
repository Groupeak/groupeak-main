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
	if err := godotenv.Load(); err != nil {
		log.Println("no .env file found, using OS env only")
	}

	// 1. Загружаем конфиг.
	cfg := config.Load()

	// 2. Подключаемся к Postgres.
	pgDB, err := db.NewPostgres(cfg.DBDsn)
	if err != nil {
		log.Fatalf("cannot connect to postgres: %v", err)
	}
	defer pgDB.Close()

	// 3. Создаём сервисы.
	authService := services.NewAuthService(pgDB, cfg.JWTToken)

	// 4. Создаём хендлеры.
	authHandler := handlers.NewAuthHandler(authService)

	// 5. Собираем роутер.
	r := router.NewRouter(authHandler)

	// 6. Запускаем сервер.
	addr := ":" + cfg.HTTPPort
	log.Printf("starting HTTP server on %s\n", addr)

	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatalf("ListenAndServe: %v", err)
	}
}
