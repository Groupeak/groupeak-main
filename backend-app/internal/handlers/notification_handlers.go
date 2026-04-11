package handlers

import (
	"net/http"
	"strconv"

	"groupeak/internal/dto"
	"groupeak/internal/services"
)

type NotificationHandler struct {
	service *services.NotificationService
}

// Конструктор
func NewNotificationHandler(s *services.NotificationService) *NotificationHandler {
	return &NotificationHandler{service: s}
}

// GET /api/v1/notifications
func (h *NotificationHandler) GetNotifications(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	// Пагинация: лимит
	limit, _ := strconv.Atoi(r.URL.Query().Get("limit"))
	if limit <= 0 || limit > 100 {
		limit = 50 // Дефолтное значение или защита от слишком большого лимита
	}

	// Пагинация: оффсет
	offset, _ := strconv.Atoi(r.URL.Query().Get("offset"))
	if offset < 0 {
		offset = 0 // Защита от отрицательного смещения (чтобы PostgreSQL не падал)
	}

	notifs, err := h.service.GetMyNotifications(r.Context(), userID, limit, offset)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"notifications": notifs})
}

// GET /api/v1/notifications/unread-count
func (h *NotificationHandler) GetUnreadCount(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	count, err := h.service.GetUnreadCount(r.Context(), userID)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]int{"unread_count": count})
}

// PATCH /api/v1/notifications/read
func (h *NotificationHandler) MarkRead(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var req dto.ReadNotificationRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid body")
		return
	}

	var errService error
	// Если передали IDшники - читаем только их. Если массив пустой - читаем всё.
	if len(req.NotificationIDs) > 0 {
		errService = h.service.MarkAsRead(r.Context(), userID, req.NotificationIDs)
	} else {
		errService = h.service.MarkAllAsRead(r.Context(), userID)
	}

	if errService != nil {
		HandleError(w, errService)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}
