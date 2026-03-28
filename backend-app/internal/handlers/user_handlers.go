package handlers

import (
	"errors"
	"groupeak/internal/models"
	"groupeak/internal/services"
	"net/http"
)

// GET /api/v1/user/get-profile
func (h *AuthHandler) GetProfile(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	user, err := h.authService.GetProfile(r.Context(), userID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrUserNotFound):
			WriteError(w, http.StatusNotFound, "user not found")
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	stats, err := h.authService.GetTaskStats(r.Context(), userID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrUserNotFound):
			WriteError(w, http.StatusNotFound, "user not found")
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{
		"user":  user,
		"stats": stats,
	})
}

// UpdateProfile — PUT /api/v1/user/profile
func (h *AuthHandler) UpdateProfile(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req models.UpdateProfileRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.UpdateProfile(r.Context(), userID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidBirthDate):
			WriteError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrBirthDateOutOfRange):
			WriteError(w, http.StatusBadRequest, err.Error())
		case err.Error() == "no fields to update":
			WriteError(w, http.StatusBadRequest, "no fields to update")
		case err.Error() == "full name cannot be empty":
			WriteError(w, http.StatusBadRequest, "full name cannot be empty")
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = WriteJSON(w, http.StatusOK, resp)
}
