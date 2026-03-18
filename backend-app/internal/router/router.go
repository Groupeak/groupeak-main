package router

import (
	"net/http"

	"groupeak/internal/handlers"
	"groupeak/internal/middleware"

	"github.com/go-chi/chi/v5"
	chimw "github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
)

// NewRouter собирает HTTP-маршруты
func NewRouter(authHandler *handlers.AuthHandler, projectHandler *handlers.ProjectHandler,
	taskHandler *handlers.TaskHandler, jwtSecret []byte) http.Handler {
	r := chi.NewRouter()

	r.Use(chimw.RequestID)
	r.Use(chimw.RealIP)
	r.Use(chimw.Recoverer)
	r.Use(chimw.Logger)

	r.Use(cors.Handler(cors.Options{
		AllowedOrigins:   []string{"http://localhost:3000", "http://127.0.0.1:3000"},
		AllowedMethods:   []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"Accept", "Authorization", "Content-Type"},
		AllowCredentials: true,
		MaxAge:           300,
	}))

	r.Route("/api/v1", func(r chi.Router) {
		r.Route("/auth", func(r chi.Router) {
			r.Post("/register", authHandler.Register)
			r.Post("/login", authHandler.Login)
		})

		r.Group(func(r chi.Router) {
			r.Use(middleware.AuthMiddleware(jwtSecret))

			r.Route("/projects", func(r chi.Router) {
				r.Post("/", projectHandler.CreateProject)                   // POST /api/v1/projects
				r.Get("/", projectHandler.ListProjects)                     // GET  /api/v1/projects
				r.Post("/{projectID}/invites", projectHandler.InviteMember) // POST /api/v1/projects/{projectID}/invites
				r.Get("/{projectID}/members", projectHandler.ListMembers)   // GET  /api/v1/projects/{projectID}/members
				r.Post("/invites/accept", projectHandler.AcceptInvite)      // POST /api/v1/projects/invites/accept
				r.Patch("/{projectID}", projectHandler.UpdateProject)       // PATCH /api/v1/projects/{projectID}
				r.Delete("/{projectID}", projectHandler.DeleteProject)      // DELETE /api/v1/projects/{projectID}
				r.Get("/{projectID}", projectHandler.GetProject)            // GET /api/v1/projects/{projectID}

				r.Post("/{projectID}/tasks", taskHandler.CreateTask)                      // POST /api/v1/projects/{projectID}/tasks
				r.Get("/{projectID}/tasks", taskHandler.ListTasks)                        // GET  /api/v1/projects/{projectID}/tasks
				r.Get("/{projectID}/tasks/{taskID}", taskHandler.GetTaskByID)             // GET  /api/v1/projects/{projectID}/tasks/{taskID}
				r.Patch("/{projectID}/tasks/{taskID}", taskHandler.UpdateTask)            // PATCH /api/v1/projects/projectID/tasks/{taskID}
				r.Delete("/{projectID}/tasks/{taskID}", taskHandler.DeleteTask)           // DELETE /api/v1/projects/{projectID}/tasks/{taskID}//
				r.Post("/{projectID}/tasks/{taskID}/submit", taskHandler.SubmitForReview) // POST /api/v1/projects/{projectID}/tasks/{taskID}/submit
				r.Post("/{projectID}/tasks/{taskID}/review", taskHandler.ReviewTask)      // POST /api/v1/projects/{projectID}/tasks/{taskID}/review

			})

			r.Get("/tasks", taskHandler.GetFilteredTasks)        // GET /api/v1/tasks
			r.Get("/tasks/nearest", taskHandler.GetNearestTasks) // GET /api/v1/tasks/nearest
			r.Get("/tasks/my", taskHandler.GetMyTasks)           // GET /api/v1/tasks/my

			r.Route("/user", func(r chi.Router) {
				r.Post("/change-password", authHandler.ChangePassword) // POST /api/v1/user/change-password
				r.Post("/change-email", authHandler.ChangeEmail)       // POST /api/v1/user/change-email
				r.Get("/profile", authHandler.GetProfile)              // GET /api/v1/user/get-profile
				r.Put("/profile", authHandler.UpdateProfile)           // PUT /api/v1/user/profile
			})

		})
	})

	r.Get("/healthz", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})

	return r
}
