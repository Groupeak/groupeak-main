package handlers

import (
	"encoding/json"
	"errors"
	"net/http"
	"strconv"
	"strings"
	"time"

	"groupeak/internal/models"
	"groupeak/internal/services"

	"github.com/go-chi/chi/v5"
)

type TaskHandler struct {
	taskService *services.TaskService
}

type reviewTaskRequest struct {
	Decision string  `json:"decision"` // "approve" | "reject"
	Comment  *string `json:"comment"`
	DueAt    *string `json:"due_at"` // RFC3339, обязателен при reject
}

func NewTaskHandler(taskService *services.TaskService) *TaskHandler {
	return &TaskHandler{taskService: taskService}
}

// parseIDParam — общий парсер числовых URL-параметров
func parseIDParam(r *http.Request, name string) (int64, error) {
	v := chi.URLParam(r, name)
	id, err := strconv.ParseInt(v, 10, 64)
	if err != nil || id <= 0 {
		return 0, errors.New("invalid id")
	}
	return id, nil
}

// POST /api/v1/projects/{projectID}/tasks
func (h *TaskHandler) CreateTask(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectIDStr := chi.URLParam(r, "projectID")
	projectID, err := strconv.ParseInt(projectIDStr, 10, 64)
	if err != nil || projectID <= 0 {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	var req models.CreateTaskRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	task, err := h.taskService.CreateTask(r.Context(), userID, projectID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectOwner):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrTaskTitleRequired),
			errors.Is(err, services.ErrInvalidTaskPriority),
			errors.Is(err, services.ErrInvalidTaskStatusValue),
			errors.Is(err, services.ErrInvalidAssigneeIDs),
			errors.Is(err, services.ErrAssigneeNotFound),
			errors.Is(err, services.ErrAssigneesNotInProject):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusCreated, map[string]interface{}{
		"id":   task.ID,
		"task": task,
	})
}

func (h *TaskHandler) ListTasks(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := parseIDParam(r, "projectID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	category := strings.TrimSpace(r.URL.Query().Get("category"))

	var tasks []models.Task
	if category == "" {
		tasks, err = h.taskService.ListProjectTasks(r.Context(), userID, projectID)
	} else {
		tasks, err = h.taskService.ListProjectTasksByCategory(r.Context(), userID, projectID, models.TaskCategory(category))
	}
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrInvalidTaskCategory):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{
		"tasks": tasks,
	})
}

// GET /api/v1/projects/{projectID}/tasks/{taskID}
func (h *TaskHandler) GetTaskByID(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := parseIDParam(r, "projectID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	taskID, err := parseIDParam(r, "taskID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid taskID")
		return
	}

	task, err := h.taskService.GetProjectTaskByID(r.Context(), userID, projectID, taskID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrTaskNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{
		"task": task,
	})
}

// PATCH /api/v1/projects/{projectID}/tasks/{taskID}
func (h *TaskHandler) UpdateTask(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := parseIDParam(r, "projectID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	taskID, err := parseIDParam(r, "taskID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid taskID")
		return
	}

	var req models.PatchTaskRequest
	if err := decodeJSON(w, r, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	task, err := h.taskService.UpdateTask(r.Context(), userID, projectID, taskID, req)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectOwner):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrTaskNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrTaskTitleRequired),
			errors.Is(err, services.ErrInvalidTaskPriority),
			errors.Is(err, services.ErrInvalidTaskStatusValue),
			errors.Is(err, services.ErrInvalidAssigneeIDs),
			errors.Is(err, services.ErrAssigneeNotFound),
			errors.Is(err, services.ErrAssigneesNotInProject),
			errors.Is(err, services.ErrNoFieldsToUpdate):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{"task": task})
}

// DELETE /api/v1/projects/{projectID}/tasks/{taskID}
func (h *TaskHandler) DeleteTask(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := parseIDParam(r, "projectID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	taskID, err := parseIDParam(r, "taskID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid taskID")
		return
	}

	err = h.taskService.DeleteTask(r.Context(), userID, projectID, taskID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrTaskNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			writeError(w, http.StatusForbidden, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// GET /api/v1/tasks?project_id=1&priority=high
func (h *TaskHandler) GetFilteredTasks(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var filter models.TaskFilterRequest

	projectIDStr := r.URL.Query().Get("project_id")
	if projectIDStr != "" {
		projectID, err := strconv.ParseInt(projectIDStr, 10, 64)
		if err != nil || projectID <= 0 {
			writeError(w, http.StatusBadRequest, "invalid project_id")
			return
		}
		filter.ProjectID = &projectID
	}

	priorityStr := r.URL.Query().Get("priority")
	if priorityStr != "" {
		priority := models.TaskPriority(priorityStr)
		filter.Priority = &priority
	}

	tasks, err := h.taskService.ListTasksWithFilter(r.Context(), userID, filter)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrInvalidTaskPriority),
			errors.Is(err, services.ErrInvalidProjectID):
			writeError(w, http.StatusBadRequest, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{
		"tasks": tasks,
	})
}

// POST /api/v1/projects/{projectID}/tasks/{taskID}/submit
func (h *TaskHandler) SubmitForReview(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := parseIDParam(r, "projectID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	taskID, err := parseIDParam(r, "taskID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid taskID")
		return
	}

	err = h.taskService.SubmitForReview(r.Context(), userID, projectID, taskID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrTaskNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			writeError(w, http.StatusForbidden, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = writeJSON(w, http.StatusOK, map[string]interface{}{"status": "ok"})
}

// POST /api/v1/projects/{projectID}/tasks/{taskID}/review
func (h *TaskHandler) ReviewTask(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	projectID, err := parseIDParam(r, "projectID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid projectID")
		return
	}

	taskID, err := parseIDParam(r, "taskID")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid taskID")
		return
	}

	var req reviewTaskRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	var newDueAt *time.Time
	if req.Decision == "reject" {
		if req.DueAt == nil || *req.DueAt == "" {
			writeError(w, http.StatusBadRequest, "due_at is required for reject")
			return
		}
		t, err := time.Parse(time.RFC3339, *req.DueAt)
		if err != nil {
			writeError(w, http.StatusBadRequest, "invalid due_at format")
			return
		}
		newDueAt = &t
	}

	err = h.taskService.ReviewTask(r.Context(), userID, projectID, taskID, req.Decision, req.Comment, newDueAt)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrTaskNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			writeError(w, http.StatusForbidden, err.Error())
		case errors.Is(err, services.ErrNotProjectOwner):
			writeError(w, http.StatusForbidden, err.Error())
		default:
			writeError(w, http.StatusInternalServerError, "internal server error")
		}
		return
	}

	_ = writeJSON(w, http.StatusOK, map[string]interface{}{"status": "ok"})
}

// GET /api/v1/tasks/nearest
func (h *TaskHandler) GetNearestTasks(w http.ResponseWriter, r *http.Request) {
	userID, ok := getUserIDFromContext(r.Context())
	if !ok {
		writeError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	tasks, err := h.taskService.ListNearestTasks(r.Context(), userID)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "internal server error")
		return
	}

	writeJSON(w, http.StatusOK, map[string]interface{}{
		"tasks": tasks,
	})
}
