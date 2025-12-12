package models

import "time"

type TaskStatus string

const (
	TaskStatusTodo TaskStatus = "todo"
	TaskStatusInProgress TaskStatus = "in_progress"
	TaskStatusDone TaskStatus = "done"
	TaskStatusPostponed TaskStatus = "postponed"
)

type TaskPriority string

const (
	TaskPriorityLow TaskPriority = "low"
	TaskPriorityMedium TaskPriority = "medium"
	TaskPriorityHigh TaskPriority = "high"
)

type Task struct {
	ID int64 `json:"id"`
	ProjectID int64 `json:"project_id"`
	Title string `json:"title"`
	Description *string `json:"description,omitempty"`
	Comments *string `json:"comments,omitempty"`
	Status TaskStatus `json:"status"`
	Priority TaskPriority `json:"priority"`
	AssigneeID *int64 `json:"assignee_id,omitempty"`
	DueAt *time.Time `json:"due_at,omitempty"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type CreateTaskRequest struct {
	Title string `json:"title"`
	Description *string `json:"description"`
	Comments *string `json:"comments"`
	Priority TaskPriority `json:"priority"`
	DueAt *time.Time `json:"due_at"`
}