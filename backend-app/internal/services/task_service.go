package services

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"strings"

	"groupeak/internal/models"
)

var (
	ErrTaskNotFound = errors.New("task not found")
	ErrNotProjectMember = errors.New("user is not a member of this project")
	ErrInvalidTaskStatus = errors.New("invalid task status for this operation")
	ErrNotTaskAssignee = errors.New("user is not assignee of this task")
)

type TaskService struct {
	db *sql.DB
}

func NewTaskService(db *sql.DB) *TaskService {
	return &TaskService{
		db: db,
	}
}

func (s *TaskService) CreateTask(ctx context.Context, ownerID, projectID int64, req models.CreateTaskRequest) (*models.Task, error) {
	title := strings.TrimSpace(req.Title)
	if title == "" {
		return nil, errors.New("task title is required")
	}

	var dbOwnerID int64
	err := s.db.QueryRowContext(
		ctx,
		`SELECT owner_id FROM projects WHERE id = $1`,
		projectID,
	).Scan(&dbOwnerID)
	if err == sql.ErrNoRows {
		return nil, ErrProjectNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("query project owner: %w", err)
	}
	if dbOwnerID != ownerID {
		return nil, ErrNotProjectOwner
	}

	switch req.Priority {
	case "low", "medium", "high":
	default:
		return nil, errors.New("invalid task priority")
	}

	var (
		descParam interface{}
		commentsParam interface{}
		dueParam interface{}
	)

	if req.Description != nil && strings.TrimSpace(*req.Description) != "" {
		descParam = strings.TrimSpace(*req.Description)
	} else {
		descParam = nil
	}

	if req.Comments != nil && strings.TrimSpace(*req.Comments) != "" {
		commentsParam = strings.TrimSpace(*req.Comments)
	} else {
		commentsParam = nil
	}

	if req.DueAt != nil {
		dueParam = *req.DueAt
	} else {
		dueParam = nil
	}

	query := `
		INSERT INTO tasks (project_id, title, description, comments, priority, due_at)
		VALUES ($1, $2, $3, $4, $5, $6)
		RETURNING id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at;
	`

	var (
		t models.Task
		descField sql.NullString
		commentsField sql.NullString
		dueField sql.NullTime
		assigneeField sql.NullInt64
	)

	err = s.db.QueryRowContext(
		ctx,
		query,
		projectID,
		title,
		descParam,
		commentsParam,
		req.Priority,
		dueParam,
	).Scan(
		&t.ID,
		&t.ProjectID,
		&t.Title,
		&descField,
		&commentsField,
		&t.Status,
		&t.Priority,
		&assigneeField,
		&dueField,
		&t.CreatedAt,
		&t.UpdatedAt,
	)
	if err != nil {
		return nil, fmt.Errorf("insert task: %w", err)
	}

	if descField.Valid {
		desc := descField.String
		t.Description = &desc
	}
	if commentsField.Valid {
		c := commentsField.String
		t.Comments = &c
	}
	if dueField.Valid {
		dt := dueField.Time
		t.DueAt = &dt
	}
	if assigneeField.Valid {
		id := assigneeField.Int64
		t.AssigneeID = &id
	}

	return &t, nil
}

func (s *TaskService) TakeTask(ctx context.Context, userID, taskID int64) (*models.Task, error) {
	var (
		projectID int64
		status string
		title string
		descField sql.NullString
		commentsField sql.NullString
		dueField sql.NullTime
		priority string
		createdAt sql.NullTime
		updatedAt sql.NullTime
		assigneeField sql.NullInt64
	)

	err := s.db.QueryRowContext(
		ctx,
		`SELECT id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at
		FROM tasks
		WHERE id = $1`,
		taskID,
	).Scan(
		&taskID,
		&projectID,
		&title,
		&descField,
		&commentsField,
		&status,
		&priority,
		&assigneeField,
		&dueField,
		&createdAt,
		&updatedAt,
	)
	if err == sql.ErrNoRows {
		return nil, ErrTaskNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("query task: %w", err)
	}

	var isMember bool
	err = s.db.QueryRowContext(
		ctx,
		`SELECT EXISTS (
			SELECT 1
			FROM project_members
			WHERE project_id = $1 AND user_id = $2
		)`,
		projectID,
		userID,
	).Scan(&isMember)
	if err != nil {
		return nil, fmt.Errorf("check project member: %w", err)
	}
	if !isMember {
		return nil, ErrNotProjectMember
	}

	if status != "todo" && status != "postponed" {
		return nil, ErrInvalidTaskStatus
	}

	_, err = s.db.ExecContext(
		ctx,
		`UPDATE tasks
		SET status = 'in_progress',
		    assignee_id = $1,
		    updated_at = NOW()
		WHERE id = $2`,
		userID,
		taskID,
	)
	if err != nil {
		return nil, fmt.Errorf("update task status: %w", err)
	}

	return s.getTaskByID(ctx, taskID)
}

func (s *TaskService) CompleteTask(ctx context.Context, userID, taskID int64) (*models.Task, error) {
	var (
		projectID int64
		status string
		assigneeField sql.NullInt64
	)

	err := s.db.QueryRowContext(
		ctx,
		`SELECT project_id, status, assignee_id
		FROM tasks
		WHERE id = $1`,
		taskID,
	).Scan(
		&projectID,
		&status,
		&assigneeField,
	)
	if err == sql.ErrNoRows {
		return nil, ErrTaskNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("query task before complete: %w", err)
	}

	var isMember bool
	err = s.db.QueryRowContext(
		ctx,
		`SELECT EXISTS (
			SELECT 1
			FROM project_members
			WHERE project_id = $1 AND user_id = $2
		)`,
		projectID,
		userID,
	).Scan(&isMember)
	if err != nil {
		return nil, fmt.Errorf("check project member: %w", err)
	}
	if !isMember {
		return nil, ErrNotProjectMember
	}

	if status != "in_progress" {
		return nil, ErrInvalidTaskStatus
	}

	if !assigneeField.Valid || assigneeField.Int64 != userID {
		return nil, ErrNotTaskAssignee
	}

	// ставим done
	_, err = s.db.ExecContext(
		ctx,
		`UPDATE tasks
		SET status = 'done',
		    updated_at = NOW()
		WHERE id = $1`,
		taskID,
	)
	if err != nil {
		return nil, fmt.Errorf("update task to done: %w", err)
	}

	return s.getTaskByID(ctx, taskID)
}

func (s *TaskService) getTaskByID(ctx context.Context, taskID int64) (*models.Task, error) {
	query := `
		SELECT id, project_id, title, description, comments, status, priority,
		       assignee_id, due_at, created_at, updated_at
		FROM tasks
		WHERE id = $1;
	`

	var (
		t models.Task
		descField sql.NullString
		commentsField sql.NullString
		dueField sql.NullTime
		assigneeField sql.NullInt64
	)

	err := s.db.QueryRowContext(ctx, query, taskID).Scan(
		&t.ID,
		&t.ProjectID,
		&t.Title,
		&descField,
		&commentsField,
		&t.Status,
		&t.Priority,
		&assigneeField,
		&dueField,
		&t.CreatedAt,
		&t.UpdatedAt,
	)
	if err == sql.ErrNoRows {
		return nil, ErrTaskNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("query task by id: %w", err)
	}

	if descField.Valid {
		desc := descField.String
		t.Description = &desc
	}
	if commentsField.Valid {
		c := commentsField.String
		t.Comments = &c
	}
	if dueField.Valid {
		dt := dueField.Time
		t.DueAt = &dt
	}
	if assigneeField.Valid {
		id := assigneeField.Int64
		t.AssigneeID = &id
	}

	return &t, nil
}

/*
Что ещё можно добавить позже (идеи на будущее):

PostponeTask — отложить (статус → postponed)
ReturnToTodo — вернуть в To Do (сбросить)
ChangeAssignee — передать задачу другому (только owner или текущий assignee)
UpdateTask — менять заголовок, описание, дедлайн
DeleteTask — удалять (с проверкой прав)
*/