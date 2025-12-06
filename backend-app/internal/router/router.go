package router

import (
	"net/http"

	"groupeak/internal/handlers"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
)

// NewRouter собирает HTTP-маршруты.
func NewRouter(authHandler *handlers.AuthHandler) http.Handler {
	router := chi.NewRouter()

	// Базовые middlewares.
	router.Use(middleware.RequestID)
	router.Use(middleware.Logger)
	router.Use(middleware.Recoverer)

	router.Route("/api/v1", func(router chi.Router) {
		router.Route("/auth", func(router chi.Router) {
			// Регистрация
			router.Post("/register", authHandler.Register)

			// TODO (для второго разработчика):
			router.Post("/login", authHandler.Login)
		})

		// TODO: сюда потом добавить /users, /projects, /tasks и т.д.
	})

	// Healthcheck, чтобы легко проверять, что сервис жив.
	router.Get("/healthz", func(w http.ResponseWriter, router *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})

	return router
}
