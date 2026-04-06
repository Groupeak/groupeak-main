package handlers

import (
	"net/http"
	"time"

	"groupeak/internal/dto"
)

// POST /api/v1/projects/{projectID}/tasks/{taskID}/submit
func (h *TaskHandler) SubmitForReview(w http.ResponseWriter, r *http.Request) {
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

	taskID, err := ParseIDParam(r, "taskID")
	if err != nil {
		WriteError(w, http.StatusBadRequest, "invalid taskID")
		return
	}

	err = h.taskService.SubmitForReview(r.Context(), userID, projectID, taskID)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"status": "ok"})
}

// POST /api/v1/projects/{projectID}/tasks/{taskID}/review
func (h *TaskHandler) ReviewTask(w http.ResponseWriter, r *http.Request) {
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

	taskID, err := ParseIDParam(r, "taskID")
	if err != nil {
		WriteError(w, http.StatusBadRequest, "invalid taskID")
		return
	}

	var req dto.ReviewTaskRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	var newDueAt *time.Time
	if req.Decision == "reject" {
		if req.DueAt == nil || *req.DueAt == "" {
			WriteError(w, http.StatusBadRequest, "due_at is required for reject")
			return
		}
		t, err := time.Parse(time.RFC3339, *req.DueAt)
		if err != nil {
			WriteError(w, http.StatusBadRequest, "invalid due_at format")
			return
		}
		newDueAt = &t
	}

	err = h.taskService.ReviewTask(r.Context(), userID, projectID, taskID, req.Decision, req.Comment, newDueAt)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"status": "ok"})
}
