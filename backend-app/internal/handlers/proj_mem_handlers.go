package handlers

import (
	"errors"
	"groupeak/internal/models"
	"groupeak/internal/services"
	"net/http"
)

// POST /api/v1/projects/{projectID}/invites
func (h *ProjectHandler) InviteMember(w http.ResponseWriter, r *http.Request) {
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

	var req models.InviteMemberRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid json body")
		return
	}

	invite, err := h.projectService.InviteMember(r.Context(), userID, projectID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			WriteError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectOwner):
			WriteError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrAlreadyProjectMember), errors.Is(err, services.ErrInviteAlreadyPending):
			WriteError(w, http.StatusConflict, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	WriteJSON(w, http.StatusCreated, map[string]interface{}{"invite": invite})
}

// GET /api/v1/projects/{projectID}/members
func (h *ProjectHandler) ListMembers(w http.ResponseWriter, r *http.Request) {
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

	members, err := h.projectService.ListProjectMembers(r.Context(), userID, projectID)
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

	WriteJSON(w, http.StatusOK, map[string]interface{}{"members": members})
}

type acceptInviteRequest struct {
	Token string `json:"token"`
}

// POST /api/v1/projects/invites/accept
func (h *ProjectHandler) AcceptInvite(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req acceptInviteRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	invite, err := h.projectService.AcceptInvite(r.Context(), userID, req.Token)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInviteTokenRequired):
			WriteError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrInviteNotFound):
			WriteError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrInviteAlreadyUsed):
			WriteError(w, http.StatusConflict, err.Error())
		case errors.Is(err, services.ErrInviteEmailMismatch):
			WriteError(w, http.StatusForbidden, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	WriteJSON(w, http.StatusOK, map[string]interface{}{"invite": invite})
}

// DELETE /api/v1/projects/{projectID}/members/{userID}
func (h *ProjectHandler) RemoveMember(w http.ResponseWriter, r *http.Request) {
	requesterID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, ok := parseProjectID(r)
	if !ok {
		WriteError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	targetUserID, err := ParseIDParam(r, "userID")
	if err != nil {
		WriteError(w, http.StatusBadRequest, "invalid userID")
		return
	}

	err = h.projectService.RemoveMember(r.Context(), requesterID, projectID, targetUserID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			WriteError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			WriteError(w, http.StatusNotFound, "member not found in project")
		case errors.Is(err, services.ErrForbidden):
			WriteError(w, http.StatusForbidden, "you don't have permission to remove this member")
		case errors.Is(err, services.ErrCannotRemoveOwner):
			WriteError(w, http.StatusForbidden, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
