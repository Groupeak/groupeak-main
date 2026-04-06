package handlers

import (
	"net/http"

	"groupeak/internal/dto"
	"groupeak/internal/services"
)

type EventHandler struct {
	service *services.EventService
}

func NewEventHandler(s *services.EventService) *EventHandler {
	return &EventHandler{service: s}
}

// POST /api/v1/projects/{projectID}/events
func (h *EventHandler) CreateEvent(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := ParseIDParam(r, "projectID")
	if err != nil {
		WriteError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	var req dto.CreateEventRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	event, err := h.service.CreateEvent(r.Context(), int(userID), int(projectID), req)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusCreated, event)
}

// PATCH /api/v1/projects/{projectID}/events/{eventID}
func (h *EventHandler) UpdateEvent(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := ParseIDParam(r, "projectID")
	if err != nil {
		WriteError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	eventID, err := ParseIDParam(r, "eventID")
	if err != nil {
		WriteError(w, http.StatusBadRequest, "invalid eventID")
		return
	}

	var req dto.UpdateEventRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	err = h.service.UpdateEvent(r.Context(), int(userID), int(projectID), int(eventID), req)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]string{"status": "success"})
}

// DELETE /api/v1/projects/{projectID}/events/{eventID}
func (h *EventHandler) DeleteEvent(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := ParseIDParam(r, "projectID")
	if err != nil {
		WriteError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	eventID, err := ParseIDParam(r, "eventID")
	if err != nil {
		WriteError(w, http.StatusBadRequest, "invalid eventID")
		return
	}

	err = h.service.DeleteEvent(r.Context(), int(userID), int(projectID), int(eventID))
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]string{"status": "success"})
}

// GET /api/v1/events/my
func (h *EventHandler) GetUserEvents(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	events, err := h.service.GetUserEvents(r.Context(), int(userID))
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"events": events})
}
