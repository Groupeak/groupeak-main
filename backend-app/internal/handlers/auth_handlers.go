package handlers

import (
	"encoding/json"
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

func writeJSON(write http.ResponseWriter, status int, data interface{}) {
	write.Header().Set("Content-Type", "application/json; charset=utf-8")
	write.WriteHeader(status)
	_ = json.NewEncoder(write).Encode(data)
}

func writeError(write http.ResponseWriter, status int, msg string) {
	writeJSON(write, status, map[string]string{
		"error": msg,
	})
}

// Register — HTTP-эндпоинт регистрации.
func (h *AuthHandler) Register(write http.ResponseWriter, r *http.Request) {
	var req models.RegisterRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(write, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.Register(r.Context(), req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrEmailAlreadyTaken):
			writeError(write, http.StatusConflict, "email already taken")
			return
		case errors.Is(err, services.ErrInvalidPosition):
			writeError(write, http.StatusBadRequest, "invalid position")
			return
		case errors.Is(err, services.ErrMissingFields):
			writeError(write, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrWeakPassword):
			writeError(write, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrInvalidEmail):
			writeError(write, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrUnsupportedEmailDomain):
			writeError(write, http.StatusBadRequest, err.Error())
			return
		default:
			writeError(write, http.StatusInternalServerError, "internal server error")
			return
		}
	}

	writeJSON(write, http.StatusCreated, resp)
}

// Login — HTTP-эндпоинт авторизации.
func (h *AuthHandler) Login(write http.ResponseWriter, r *http.Request) {
	var req models.LoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(write, http.StatusBadRequest, "invalid JSON body")
		return
	}

	if req.Email == "" || req.Password == "" {
		writeError(write, http.StatusBadRequest, "email and password are required")
		return
	}

	resp, err := h.authService.Login(r.Context(), req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidEmail):
			writeError(write, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrUnsupportedEmailDomain):
			writeError(write, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrInvalidCredentials):
			writeError(write, http.StatusUnauthorized, err.Error())
		default:
			writeError(write, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(write, http.StatusOK, resp)
}
