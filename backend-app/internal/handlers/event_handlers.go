package handlers

import (
	"errors"
	"net/http"
	"strconv"

	"groupeak/internal/models"
	"groupeak/internal/services"

	"github.com/go-chi/chi/v5"
)

var (
	ErrInvalidProjectID           = errors.New("invalid project ID format")
	ErrInvalidEventID             = errors.New("invalid event ID format")
	ErrInvalidRequestBody         = errors.New("invalid request body format")
	ErrMissingRequiredFields      = errors.New("title and start_at fields are required")
	ErrInvalidParticipantIDs      = errors.New("one or more participant IDs are invalid")
	ErrFailedToCreateEvent        = errors.New("failed to create event")
	ErrFailedToUpdateEvent        = errors.New("failed to update event")
	ErrFailedToUpdateParticipants = errors.New("failed to update participants")
	ErrInternalServerError        = errors.New("internal server error")
)

type EventHandler struct {
	service *services.EventService
}

func NewEventHandler(s *services.EventService) *EventHandler {
	return &EventHandler{service: s}
}

func (h *EventHandler) CreateEvent(w http.ResponseWriter, r *http.Request) {
	projectIDStr := chi.URLParam(r, "projectID")
	projectID, err := strconv.Atoi(projectIDStr)
	if err != nil {
		WriteError(w, http.StatusBadRequest, ErrInvalidProjectID.Error())
		return
	}

	var req models.CreateEventRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, ErrInvalidRequestBody.Error()+": "+err.Error())
		return
	}

	if req.Title == "" || req.StartAt.IsZero() {
		WriteError(w, http.StatusBadRequest, ErrMissingRequiredFields.Error())
		return
	}

	event, err := h.service.CreateEvent(r.Context(), projectID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInsertParticipant):
			WriteError(w, http.StatusBadRequest, ErrInvalidParticipantIDs.Error())
		case errors.Is(err, services.ErrInsertEvent):
			WriteError(w, http.StatusInternalServerError, ErrFailedToCreateEvent.Error())
		default:
			WriteError(w, http.StatusInternalServerError, ErrInternalServerError.Error())
		}
		return
	}

	_ = WriteJSON(w, http.StatusCreated, event)
}

func (h *EventHandler) UpdateEvent(w http.ResponseWriter, r *http.Request) {
	projectIDStr := chi.URLParam(r, "projectID")
	eventIDStr := chi.URLParam(r, "eventID")

	projectID, err := strconv.Atoi(projectIDStr)
	if err != nil {
		WriteError(w, http.StatusBadRequest, ErrInvalidProjectID.Error())
		return
	}

	eventID, err := strconv.Atoi(eventIDStr)
	if err != nil {
		WriteError(w, http.StatusBadRequest, ErrInvalidEventID.Error())
		return
	}

	var req models.UpdateEventRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, ErrInvalidRequestBody.Error()+": "+err.Error())
		return
	}

	_, err = h.service.UpdateEvent(r.Context(), projectID, eventID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrInsertParticipant):
			WriteError(w, http.StatusBadRequest, ErrInvalidParticipantIDs.Error())
		case errors.Is(err, services.ErrUpdateEvent):
			WriteError(w, http.StatusInternalServerError, ErrFailedToUpdateEvent.Error())
		case errors.Is(err, services.ErrClearParticipants):
			WriteError(w, http.StatusInternalServerError, ErrFailedToUpdateParticipants.Error())
		default:
			WriteError(w, http.StatusInternalServerError, ErrInternalServerError.Error())
		}
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]string{"status": "success"})
}
