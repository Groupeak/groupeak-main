package handlers

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"errors"
	"fmt"
	"groupeak/internal/models"
	"groupeak/internal/services"
	"net/http"
	"path/filepath"
	"strings"

	"github.com/minio/minio-go/v7"
)

var (
	ErrUnauthorized         = errors.New("unauthorized")
	ErrFileTooLarge         = errors.New("file too large or invalid form data (max 8MB)")
	ErrFailedToGetFile      = errors.New("failed to get avatar file")
	ErrUnsupportedFormat    = errors.New("unsupported file format. Allowed: jpg, jpeg, png, webp, heic, heif")
	ErrUploadStorageFailed  = errors.New("failed to upload image to storage")
	ErrSaveAvatarDBFailed   = errors.New("failed to save avatar url in database")
	ErrUserNotFound         = errors.New("user not found")
	ErrInternalServerErrorU = errors.New("internal server error")
	ErrInvalidJSONBody      = errors.New("invalid JSON body")
	ErrNoFieldsToUpdate     = errors.New("no fields to update")
	ErrFullNameEmpty        = errors.New("full name cannot be empty")
	ErrDeleteAvatarDBFailed = errors.New("failed to delete avatar from database")
)

func generateRandomFileName(ext string) string {
	bytes := make([]byte, 16)
	rand.Read(bytes)
	return hex.EncodeToString(bytes) + ext
}

var allowedAvatarExts = map[string]string{
	".jpg":  "image/jpeg",
	".jpeg": "image/jpeg",
	".png":  "image/png",
	".webp": "image/webp",
	".heic": "image/heic",
	".heif": "image/heif",
}

// POST /api/v1/user/avatar
func (h *AuthHandler) UploadAvatar(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, ErrUnauthorized.Error())
		return
	}

	userProfile, err := h.authService.GetProfile(r.Context(), userID)
	var oldAvatarUrl string
	if err == nil && userProfile.AvatarURL != nil {
		oldAvatarUrl = *userProfile.AvatarURL
	}
	if err := r.ParseMultipartForm(8 << 20); err != nil {
		WriteError(w, http.StatusBadRequest, ErrFileTooLarge.Error())
		return
	}

	file, header, err := r.FormFile("avatar")
	if err != nil {
		WriteError(w, http.StatusBadRequest, ErrFailedToGetFile.Error())
		return
	}
	defer file.Close()

	ext := strings.ToLower(filepath.Ext(header.Filename))
	contentType, isAllowed := allowedAvatarExts[ext]
	if !isAllowed {
		WriteError(w, http.StatusBadRequest, ErrUnsupportedFormat.Error())
		return
	}

	fileName := generateRandomFileName(ext)

	_, err = h.minioClient.PutObject(r.Context(), h.bucketName, fileName, file, header.Size, minio.PutObjectOptions{
		ContentType: contentType,
	})
	if err != nil {
		WriteError(w, http.StatusInternalServerError, ErrUploadStorageFailed.Error())
		return
	}

	baseURL := h.endpoint
	if !strings.HasPrefix(baseURL, "http://") && !strings.HasPrefix(baseURL, "https://") {
		baseURL = "http://" + baseURL
	}

	avatarURL := fmt.Sprintf("%s/%s/%s", baseURL, h.bucketName, fileName)

	err = h.authService.UpdateAvatarURL(r.Context(), userID, avatarURL)
	if err != nil {
		go func() {
			_ = h.minioClient.RemoveObject(context.Background(), h.bucketName, fileName, minio.RemoveObjectOptions{})
		}()
		WriteError(w, http.StatusInternalServerError, ErrSaveAvatarDBFailed.Error())
		return
	}

	if oldAvatarUrl != "" {
		parts := strings.Split(oldAvatarUrl, "/")
		oldFileName := parts[len(parts)-1]

		if oldFileName != "" {
			go func(name string) {
				_ = h.minioClient.RemoveObject(context.Background(), h.bucketName, name, minio.RemoveObjectOptions{})
			}(oldFileName)
		}
	}

	_ = WriteJSON(w, http.StatusOK, map[string]string{
		"avatar_url": avatarURL,
	})
}

// DELETE /api/v1/user/avatar
func (h *AuthHandler) DeleteAvatar(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, ErrUnauthorized.Error())
		return
	}

	userProfile, err := h.authService.GetProfile(r.Context(), userID)
	if err != nil {
		WriteError(w, http.StatusInternalServerError, ErrInternalServerError.Error())
		return
	}

	if userProfile.AvatarURL == nil || *userProfile.AvatarURL == "" {
		_ = WriteJSON(w, http.StatusOK, map[string]string{
			"message": "avatar already deleted",
		})
		return
	}

	oldAvatarURL := *userProfile.AvatarURL

	err = h.authService.DeleteAvatarURL(r.Context(), userID)
	if err != nil {
		WriteError(w, http.StatusInternalServerError, ErrDeleteAvatarDBFailed.Error())
		return
	}

	parts := strings.Split(oldAvatarURL, "/")
	fileName := parts[len(parts)-1]

	if fileName != "" {
		go func(name string) {
			_ = h.minioClient.RemoveObject(context.Background(), h.bucketName, name, minio.RemoveObjectOptions{})
		}(fileName)
	}

	_ = WriteJSON(w, http.StatusOK, map[string]string{
		"message": "avatar successfully deleted",
	})
}

// GET /api/v1/user/get-profile
func (h *AuthHandler) GetProfile(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, ErrUnauthorized.Error())
		return
	}

	user, err := h.authService.GetProfile(r.Context(), userID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrUserNotFound):
			WriteError(w, http.StatusNotFound, ErrUserNotFound.Error())
		default:
			WriteError(w, http.StatusInternalServerError, ErrInternalServerErrorU.Error())
		}
		return
	}

	stats, err := h.authService.GetTaskStats(r.Context(), userID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrUserNotFound):
			WriteError(w, http.StatusNotFound, ErrUserNotFound.Error())
		default:
			WriteError(w, http.StatusInternalServerError, ErrInternalServerErrorU.Error())
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
		WriteError(w, http.StatusUnauthorized, ErrUnauthorized.Error())
		return
	}

	var req models.UpdateProfileRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, ErrInvalidJSONBody.Error())
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
			WriteError(w, http.StatusBadRequest, ErrNoFieldsToUpdate.Error())
		case err.Error() == "full name cannot be empty":
			WriteError(w, http.StatusBadRequest, ErrFullNameEmpty.Error())
		default:
			WriteError(w, http.StatusInternalServerError, ErrInternalServerErrorU.Error())
		}
		return
	}

	_ = WriteJSON(w, http.StatusOK, resp)
}
