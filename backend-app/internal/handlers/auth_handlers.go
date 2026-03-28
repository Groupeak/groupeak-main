package handlers

import (
	"errors"
	"net/http"

	"groupeak/internal/models"
	"groupeak/internal/services"
)

type AuthHandler struct {
	authService *services.AuthService
}

func NewAuthHandler(authService *services.AuthService) *AuthHandler {
	return &AuthHandler{
		authService: authService,
	}
}

const maxJSONBody = 1 << 20 // 1 Mb

// Register — HTTP-эндпоинт регистрации.
func (h *AuthHandler) Register(w http.ResponseWriter, r *http.Request) {
	var req models.RegisterRequest

	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.Register(r.Context(), req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrEmailAlreadyTaken):
			WriteError(w, http.StatusConflict, "email already taken")
			return
		case errors.Is(err, services.ErrInvalidPosition):
			WriteError(w, http.StatusBadRequest, "invalid position")
			return
		case errors.Is(err, services.ErrMissingFields):
			WriteError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrWeakPassword):
			WriteError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrInvalidEmail):
			WriteError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrUnsupportedEmailDomain):
			WriteError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrInvalidBirthDate):
			WriteError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrBirthDateOutOfRange):
			WriteError(w, http.StatusBadRequest, err.Error())
			return
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
			return
		}
	}

	WriteJSON(w, http.StatusCreated, resp)
}

// Login — HTTP-эндпоинт авторизации.
func (h *AuthHandler) Login(w http.ResponseWriter, r *http.Request) {
	var req models.LoginRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	if req.Email == "" || req.Password == "" {
		WriteError(w, http.StatusBadRequest, "email and password are required")
		return
	}

	resp, err := h.authService.Login(r.Context(), req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidEmail):
			WriteError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrUnsupportedEmailDomain):
			WriteError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrInvalidCredentials):
			WriteError(w, http.StatusUnauthorized, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	WriteJSON(w, http.StatusOK, resp)
}

// ChangePassword — POST /api/v1/user/change-password
func (h *AuthHandler) ChangePassword(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req models.ChangePasswordRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.ChangePassword(r.Context(), userID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidCredentials):
			WriteError(w, http.StatusUnauthorized, "invalid credentials")
		case errors.Is(err, services.ErrWeakPassword),
			errors.Is(err, services.ErrNewPasswordSameAsOld),
			errors.Is(err, services.ErrPasswordDoNotMatch),
			errors.Is(err, services.ErrMissingFields):
			WriteError(w, http.StatusBadRequest, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = WriteJSON(w, http.StatusOK, resp)
}

// ChangeEmail — POST /api/v1/user/change-email
func (h *AuthHandler) ChangeEmail(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req models.ChangeEmailRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.ChangeEmail(r.Context(), userID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidCredentials):
			WriteError(w, http.StatusUnauthorized, "invalid credentials")
		case errors.Is(err, services.ErrEmailAlreadyTaken):
			WriteError(w, http.StatusConflict, "email already taken")
		case errors.Is(err, services.ErrInvalidEmail),
			errors.Is(err, services.ErrUnsupportedEmailDomain),
			errors.Is(err, services.ErrEmailSameAsCurrent),
			errors.Is(err, services.ErrNewEmailIsRequired),
			errors.Is(err, services.ErrPasswordIsRequired):
			WriteError(w, http.StatusBadRequest, err.Error())
		default:
			WriteError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = WriteJSON(w, http.StatusOK, resp)
}
