package handlers

import (
	"context"
	"encoding/json"
	"errors"
	"io"
	"net/http"

	"groupeak/internal/middleware"
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

func writeJSON(w http.ResponseWriter, status int, data interface{}) error {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	return json.NewEncoder(w).Encode(data)
}

func writeError(w http.ResponseWriter, status int, msg string) {
	_ = writeJSON(w, status, map[string]string{"error": msg})
}

func decodeJSON(w http.ResponseWriter, r *http.Request, dst interface{}) error {
	r.Body = http.MaxBytesReader(w, r.Body, maxJSONBody)
	dec := json.NewDecoder(r.Body)
	dec.DisallowUnknownFields()

	if err := dec.Decode(dst); err != nil {
		return err
	}

	if err := dec.Decode(&struct{}{}); err != io.EOF {
		return errors.New("request body must constain only one JSON object")
	}

	return nil
}

func getUserIDFromContext(ctx context.Context) (int64, bool) {
	v := ctx.Value(middleware.UserIDKey)
	id, ok := v.(int64)
	return id, ok
}

// Register — HTTP-эндпоинт регистрации.
func (h *AuthHandler) Register(w http.ResponseWriter, r *http.Request) {
	var req models.RegisterRequest

	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.Register(r.Context(), req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrEmailAlreadyTaken):
			writeError(w, http.StatusConflict, "email already taken")
			return
		case errors.Is(err, services.ErrInvalidPosition):
			writeError(w, http.StatusBadRequest, "invalid position")
			return
		case errors.Is(err, services.ErrMissingFields):
			writeError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrWeakPassword):
			writeError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrInvalidEmail):
			writeError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrUnsupportedEmailDomain):
			writeError(w, http.StatusBadRequest, err.Error())
			return
		case errors.Is(err, services.ErrInvalidBirthDate):
			writeError(w, http.StatusBadRequest, err.Error())
			return
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
			return
		}
	}

	writeJSON(w, http.StatusCreated, resp)
}

// Login — HTTP-эндпоинт авторизации.
func (h *AuthHandler) Login(w http.ResponseWriter, r *http.Request) {
	var req models.LoginRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	if req.Email == "" || req.Password == "" {
		writeError(w, http.StatusBadRequest, "email and password are required")
		return
	}

	resp, err := h.authService.Login(r.Context(), req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidEmail):
			writeError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrUnsupportedEmailDomain):
			writeError(w, http.StatusBadRequest, err.Error())
		case errors.Is(err, services.ErrInvalidCredentials):
			writeError(w, http.StatusUnauthorized, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusOK, resp)
}

// GET /api/v1/user/get-profile
func (h *AuthHandler) GetProfile(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	user, err := h.authService.GetProfile(r.Context(), userID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrUserNotFound):
			writeError(w, http.StatusNotFound, "user not found")
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = writeJSON(w, http.StatusOK, map[string]interface{}{"user": user})
}

// ChangePassword — POST /api/v1/user/change-password
func (h *AuthHandler) ChangePassword(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req models.ChangePasswordRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.ChangePassword(r.Context(), userID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidCredentials):
			writeError(w, http.StatusUnauthorized, "invalid credentials")
		case errors.Is(err, services.ErrWeakPassword),
			errors.Is(err, services.ErrNewPasswordSameAsOld),
			errors.Is(err, services.ErrPasswordDoNotMatch),
			errors.Is(err, services.ErrMissingFields):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = writeJSON(w, http.StatusOK, resp)
}

// ChangeEmail — POST /api/v1/user/change-email
func (h *AuthHandler) ChangeEmail(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req models.ChangeEmailRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.ChangeEmail(r.Context(), userID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidCredentials):
			writeError(w, http.StatusUnauthorized, "invalid credentials")
		case errors.Is(err, services.ErrEmailAlreadyTaken):
			writeError(w, http.StatusConflict, "email already taken")
		case errors.Is(err, services.ErrInvalidEmail),
			errors.Is(err, services.ErrUnsupportedEmailDomain),
			errors.Is(err, services.ErrEmailSameAsCurrent),
			errors.Is(err, services.ErrNewEmailIsRequired),
			errors.Is(err, services.ErrPasswordIsRequired):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = writeJSON(w, http.StatusOK, resp)
}

// UpdateProfile — PUT /api/v1/user/profile
func (h *AuthHandler) UpdateProfile(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req models.UpdateProfileRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.UpdateProfile(r.Context(), userID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInvalidBirthDate):
			writeError(w, http.StatusBadRequest, err.Error())
		case err.Error() == "no fields to update":
			writeError(w, http.StatusBadRequest, "no fields to update")
		case err.Error() == "full name cannot be empty":
			writeError(w, http.StatusBadRequest, "full name cannot be empty")
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = writeJSON(w, http.StatusOK, resp)
}
