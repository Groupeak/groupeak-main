package handlers

import (
	"errors"
	"net/http"
	"strconv"

	"groupeak/internal/models"
	"groupeak/internal/services"

	"github.com/go-chi/chi/v5"
)

type TaskHandler struct {
	taskService *services.TaskService
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

// GET /api/v1/projects/{projectID}/tasks
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

	tasks, err := h.taskService.ListProjectTasks(r.Context(), userID, projectID)
	if err != nil {
		switch {
		case errors.Is(err, services.ErrProjectNotFound):
			writeError(w, http.StatusNotFound, err.Error())
		case errors.Is(err, services.ErrNotProjectMember):
			writeError(w, http.StatusForbidden, err.Error())
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
