package router

import (
	"net/http"

	"groupeak/internal/handlers"

	"github.com/go-chi/chi/v5"
	chimw "github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
)

// NewRouter собирает HTTP-маршруты
func NewRouter(authHandler *handlers.AuthHandler) http.Handler {
	router := chi.NewRouter()

	// Базовые middlewares.
	router.Use(chimw.RequestID)
	router.Use(chimw.Logger)
	router.Use(chimw.Recoverer)

	// CORS Настройки для работы с фронтом
	router.Use(cors.Handler(cors.Options{
		AllowedOrigins: []string{
			"http://localhost:5173", 
			"http://localhost:3000", 
			"https://api-groupeak.netlify.app/",
		},

		AllowedMethods: []string{
			"GET",
			"POST",
			"PUT",
			"PATCH",
			"DELETE",
			"OPTIONS",
		},

		AllowedHeaders: []string{
			"Accept",
			"Authorization",
			"Content-Type",
			"X-CSRF-Token",
		},

		ExposedHeaders: []string{
			"Link",
		},

		AllowCredentials: true,
		MaxAge: 300,
	}))

	// Маршруты API
	router.Route("/api/v1", func(router chi.Router) {
		router.Route("/auth", func(router chi.Router) {
			router.Post("/register", authHandler.Register)
			router.Post("/login", authHandler.Login)
		})
	})

	router.Get("/healthz", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})

	return router
}
