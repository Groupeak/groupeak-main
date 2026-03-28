package handlers

import (
	"errors"
	"net/http"
	"strconv"

	"groupeak/internal/models"
	"groupeak/internal/services"

	"github.com/go-chi/chi/v5"
)

type ProjectHandler struct {
	projectService *services.ProjectService
}

func NewProjectHandler(projectService *services.ProjectService) *ProjectHandler {
	return &ProjectHandler{
		projectService: projectService,
	}
}

func parseProjectID(r *http.Request) (int64, bool) {
	projectIDStr := chi.URLParam(r, "projectID")
	projectID, err := strconv.ParseInt(projectIDStr, 10, 64)
	if err != nil || projectID <= 0 {
		return 0, false
	}
	return projectID, true
}

// POST /api/v1/projects
func (h *ProjectHandler) CreateProject(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req models.CreateProjectRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	project, err := h.projectService.CreateProject(r.Context(), userID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNameIsRequired):
			WriteError(w, http.StatusBadRequest, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	WriteJSON(w, http.StatusCreated, map[string]interface{}{"project": project})
}

// GET /api/v1/projects
func (h *ProjectHandler) ListProjects(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projects, err := h.projectService.ListUserProjects(r.Context(), userID)
	if err != nil {
		WriteError(w, http.StatusInternalServerError, "internal server error")
		return
	}

	WriteJSON(w, http.StatusOK, map[string]interface{}{"projects": projects})
}

// PATCH /api/v1/projects/{projectID}
func (h *ProjectHandler) UpdateProject(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, ok := parseProjectID(r)
	if !ok {
		WriteError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	var req models.PatchProjectRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	project, err := h.projectService.UpdateProject(r.Context(), userID, projectID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			WriteError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectOwner):
			WriteError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrProjectNameIsRequired),
			errors.Is(err, services.ErrNoFieldsToUpdate):
			WriteError(w, http.StatusBadRequest, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	WriteJSON(w, http.StatusOK, map[string]interface{}{"project": project})
}

// DELETE /api/v1/projects/{projectID}
func (h *ProjectHandler) DeleteProject(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, ok := parseProjectID(r)
	if !ok {
		WriteError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	err := h.projectService.DeleteProject(r.Context(), userID, projectID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			WriteError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectOwner):
			WriteError(w, http.StatusForbidden, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// GET /api/v1/projects/{projectID}
func (h *ProjectHandler) GetProject(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, ok := parseProjectID(r)
	if !ok {
		WriteError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	project, err := h.projectService.GetProjectDetails(r.Context(), userID, projectID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			WriteError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			WriteError(w, http.StatusForbidden, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	WriteJSON(w, http.StatusOK, map[string]interface{}{"project": project})
}
