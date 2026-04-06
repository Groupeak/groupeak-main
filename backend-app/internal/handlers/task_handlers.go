package handlers

import (
	"net/http"
	"strconv"
	"strings"

	"groupeak/internal/dto"
	"groupeak/internal/models"
	"groupeak/internal/services"
)

type TaskHandler struct {
	taskService *services.TaskService
}

func NewTaskHandler(taskService *services.TaskService) *TaskHandler {
	return &TaskHandler{taskService: taskService}
}

// POST /api/v1/projects/{projectID}/tasks
func (h *TaskHandler) CreateTask(w http.ResponseWriter, r *http.Request) {
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

	var req dto.CreateTaskRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	task, err := h.taskService.CreateTask(r.Context(), userID, projectID, req)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusCreated, map[string]interface{}{
		"id":   task.ID,
		"task": task,
	})
}

// GET /api/v1/projects/{projectID}/tasks/{taskID}
func (h *TaskHandler) GetTaskByID(w http.ResponseWriter, r *http.Request) {
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

	task, err := h.taskService.GetProjectTaskByID(r.Context(), userID, projectID, taskID)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"task": task})
}

// PATCH /api/v1/projects/{projectID}/tasks/{taskID}
func (h *TaskHandler) UpdateTask(w http.ResponseWriter, r *http.Request) {
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

	var req dto.PatchTaskRequest
	if err := DecodeJSON(w, r, &req); err != nil {
		WriteError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}

	task, err := h.taskService.UpdateTask(r.Context(), userID, projectID, taskID, req)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"task": task})
}

// DELETE /api/v1/projects/{projectID}/tasks/{taskID}
func (h *TaskHandler) DeleteTask(w http.ResponseWriter, r *http.Request) {
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

	err = h.taskService.DeleteTask(r.Context(), userID, projectID, taskID)
	if err != nil {
		HandleError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

// GET /api/v1/tasks?project_id=1&priority=high
func (h *TaskHandler) GetFilteredTasks(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	var filter dto.TaskFilterRequest

	if projectIDStr := r.URL.Query().Get("project_id"); projectIDStr != "" {
		projectID, err := strconv.ParseInt(projectIDStr, 10, 64)
		if err != nil || projectID <= 0 {
			WriteError(w, http.StatusBadRequest, "invalid project_id")
			return
		}
		filter.ProjectID = &projectID
	}

	if priorityStr := r.URL.Query().Get("priority"); priorityStr != "" {
		if priorityStr != string(models.TaskPriorityLow) && priorityStr != string(models.TaskPriorityMedium) && priorityStr != string(models.TaskPriorityHigh) {
			WriteError(w, http.StatusBadRequest, "invalid priority filter")
			return
		}
		priority := models.TaskPriority(priorityStr)
		filter.Priority = &priority
	}

	tasks, err := h.taskService.ListTasksWithFilter(r.Context(), userID, filter)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"tasks": tasks})
}

// GET  /api/v1/projects/{projectID}/tasks
func (h *TaskHandler) ListTasks(w http.ResponseWriter, r *http.Request) {
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

	limitStr := r.URL.Query().Get("limit")
	limit, _ := strconv.ParseInt(limitStr, 10, 64)
	if limit <= 0 || limit > 100 {
		limit = 30
	}

	offsetStr := r.URL.Query().Get("offset")
	offset, _ := strconv.ParseInt(offsetStr, 10, 64)
	if offset < 0 {
		offset = 0
	}

	category := strings.TrimSpace(r.URL.Query().Get("category"))
	if category != "" && category != "current" && category != "review" && category != "inactive" {
		WriteError(w, http.StatusBadRequest, "invalid category")
		return
	}

	assigneeFilter := parseAssigneeFilter(r)

	var tasks []models.Task

	if len(assigneeFilter) > 0 {
		tasks, err = h.taskService.ListProjectTasksByAssignees(r.Context(), userID, projectID, assigneeFilter)
	} else if category != "" {
		tasks, err = h.taskService.ListProjectTasksByCategory(r.Context(), userID, projectID, models.TaskCategory(category))
	} else {
		tasks, err = h.taskService.ListProjectTasks(r.Context(), userID, projectID, limit, offset)
	}

	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"tasks": tasks})
}

// GET /api/v1/tasks/nearest
func (h *TaskHandler) GetNearestTasks(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	tasks, err := h.taskService.ListNearestTasks(r.Context(), userID)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"tasks": tasks})
}

// GET /api/v1/tasks/my
func (h *TaskHandler) GetMyTasks(w http.ResponseWriter, r *http.Request) {
	userID, ok := GetUserIDFromContext(r.Context())
	if !ok {
		WriteError(w, http.StatusUnauthorized, "unauthorized")
		return
	}

	tasks, err := h.taskService.GetMyTasks(r.Context(), userID)
	if err != nil {
		HandleError(w, err)
		return
	}

	_ = WriteJSON(w, http.StatusOK, map[string]interface{}{"tasks": tasks})
}

func parseAssigneeFilter(r *http.Request) []int64 {
	assigneesStr := r.URL.Query().Get("assignees")
	if assigneesStr == "" {
		return nil
	}
	parts := strings.Split(assigneesStr, ",")
	filter := make([]int64, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p == "" {
			continue
		}
		if id, err := strconv.ParseInt(p, 10, 64); err == nil && id > 0 {
			filter = append(filter, id)
		}
	}
	return filter
}
