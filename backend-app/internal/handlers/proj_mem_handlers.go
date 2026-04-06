package handlers

import (
	"net/http"

	"groupeak/internal/dto"
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

	var req dto.InviteMemberRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid json body")
		return
	}

	invite, err := h.projectService.InviteMember(r.Context(), userID, projectID, req)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusCreated, map[string]interface{}{"invite": invite})
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
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"members": members})
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
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"invite": invite})
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
		HandleError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
