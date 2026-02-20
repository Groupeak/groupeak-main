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
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req models.CreateProjectRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	project, err := h.projectService.CreateProject(r.Context(), userID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNameIsRequired):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusCreated, map[string]interface{}{"project": project})
}

// GET /api/v1/projects
func (h *ProjectHandler) ListProjects(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projects, err := h.projectService.ListUserProjects(r.Context(), userID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "internal server error")
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{"projects": projects})
}

// POST /api/v1/projects/{projectID}/invites
func (h *ProjectHandler) InviteMember(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, ok := parseProjectID(r)
	if !ok {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	var req models.InviteMemberRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid json body")
		return
	}

	invite, err := h.projectService.InviteMember(r.Context(), userID, projectID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectOwner):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrAlreadyProjectMember), errors.Is(err, services.ErrInviteAlreadyPending):
			writeError(w, http.StatusConflict, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusCreated, map[string]interface{}{"invite": invite})
}

// GET /api/v1/projects/{projectID}/members
func (h *ProjectHandler) ListMembers(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, ok := parseProjectID(r)
	if !ok {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	members, err := h.projectService.ListProjectMembers(r.Context(), userID, projectID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			writeError(w, http.StatusForbidden, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{"members": members})
}

type acceptInviteRequest struct {
	Token string `json:"token"`
}

// POST /api/v1/projects/invites/accept
func (h *ProjectHandler) AcceptInvite(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req acceptInviteRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	invite, err := h.projectService.AcceptInvite(r.Context(), userID, req.Token)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInviteTokenRequired):
			writeError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrInviteNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrInviteAlreadyUsed):
			writeError(w, http.StatusConflict, err.Error())
		case errors.Is(err, services.ErrInviteEmailMismatch):
			writeError(w, http.StatusForbidden, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{"invite": invite})
}

// PATCH /api/v1/projects/{projectID}
func (h *ProjectHandler) UpdateProject(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, ok := parseProjectID(r)
	if !ok {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	var req models.PatchProjectRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	project, err := h.projectService.UpdateProject(r.Context(), userID, projectID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectOwner):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrProjectNameIsRequired),
			errors.Is(err, services.ErrNoFieldsToUpdate):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{"project": project})
}
