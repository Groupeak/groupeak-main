package handlers

import (
	"net/http"

	"groupeak/internal/dto"
	"groupeak/internal/services"

	"github.com/minio/minio-go/v7"
)

type AuthHandler struct {
	authService *services.AuthService
	minioClient *minio.Client
	bucketName  string
	endpoint    string
}

func NewAuthHandler(authService *services.AuthService, minioClient *minio.Client, bucketName string, endpoint string) *AuthHandler {
	return &AuthHandler{
		authService: authService,
		minioClient: minioClient,
		bucketName:  bucketName,
		endpoint:    endpoint,
	}
}

// Register — HTTP-эндпоинт регистрации
func (h *AuthHandler) Register(w http.ResponseWriter, r *http.Request) {
	var req dto.RegisterRequest

	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.Register(r.Context(), req)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusCreated, resp)
}

// Login — HTTP-эндпоинт авторизации
func (h *AuthHandler) Login(w http.ResponseWriter, r *http.Request) {
	var req dto.LoginRequest
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
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, resp)
}

// ChangePassword — POST /api/v1/user/change-password
func (h *AuthHandler) ChangePassword(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req dto.ChangePasswordRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.ChangePassword(r.Context(), userID, req)
	if err != nil {
		HandleError(w, err)
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

	var req dto.ChangeEmailRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	resp, err := h.authService.ChangeEmail(r.Context(), userID, req)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, resp)
}
