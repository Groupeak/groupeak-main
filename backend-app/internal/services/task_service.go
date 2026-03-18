package services

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"strconv"
	"strings"
	"time"

	"groupeak/internal/models"
)

var (
	ErrTaskNotFound            = errors.New("task not found")
	ErrInvalidTaskStatus       = errors.New("invalid task status for this operation")
	ErrNotTaskAssignee         = errors.New("user is not assignee of this task")
	ErrTaskTitleRequired       = errors.New("task title is required")
	ErrInvalidTaskPriority     = errors.New("invalid task priority")
	ErrInvalidTaskStatusValue  = errors.New("invalid task status")
	ErrInvalidAssigneeIDs      = errors.New("invalid assignee_ids")
	ErrAssigneeNotFound        = errors.New("one or more assignees do not exist")
	ErrAssigneesNotInProject   = errors.New("one or more assignees have no access to this project")
	ErrForbidden               = errors.New("forbidden")
	ErrTaskNotFoundOrBadStatus = errors.New("task not found or bad status")
	ErrDueAtRequired           = errors.New("due_at required")
	ErrBadDecision             = errors.New("bad decision")
	ErrInvalidProjectID        = errors.New("invalid project ID")
	ErrInvalidTaskCategory     = errors.New("invalid task category")
)

type TaskService struct {
	db *sql.DB
}

func NewTaskService(db *sql.DB) *TaskService {
	return &TaskService{db: db}
}

// CreateTask — создавать может только owner проекта
func (s *TaskService) CreateTask(ctx context.Context, ownerID, projectID int64, req models.CreateTaskRequest) (*models.Task, error) {
	title := strings.TrimSpace(req.TaskName)
	if title == "" {
		return nil, ErrTaskTitleRequired
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
		return nil, fmt.Errorf("select project owner: %w", err)
	}
	if dbOwnerID != ownerID {
		return nil, ErrNotProjectOwner
	}

	status := req.Status
	if status == "" {
		status = models.TaskStatusTodo
	}
	switch status {
	case models.TaskStatusTodo, models.TaskStatusInProgress, models.TaskStatusDone, models.TaskStatusPostponed:
	default:
		return nil, ErrInvalidTaskStatusValue
	}

	priority := req.Priority
	if priority == "" {
		priority = models.TaskPriorityMedium
	}
	switch priority {
	case models.TaskPriorityLow, models.TaskPriorityMedium, models.TaskPriorityHigh:
	default:
		return nil, ErrInvalidTaskPriority
	}

	var descParam interface{}
	if req.TaskDescription != nil && strings.TrimSpace(*req.TaskDescription) != "" {
		descParam = strings.TrimSpace(*req.TaskDescription)
	} else {
		descParam = nil
	}

	var dueParam interface{}
	if req.DueDate != nil {
		dueParam = *req.DueDate
	} else {
		dueParam = nil
	}

	assigneeIDs, err := normalizeAssigneeIDs(req.AssigneeIDs)
	if err != nil {
		return nil, err
	}

	if len(assigneeIDs) > 0 {
		if err := s.ensureUsersExist(ctx, assigneeIDs); err != nil {
			return nil, err
		}
		if err := s.ensureUsersAreProjectMembers(ctx, projectID, assigneeIDs); err != nil {
			return nil, err
		}
	}

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("begin tx: %w", err)
	}
	defer func() { _ = tx.Rollback() }()

	query := `
		INSERT INTO tasks (project_id, title, description, comments, status, priority, due_at)
		VALUES ($1, $2, $3, NULL, $4, $5, $6)
		RETURNING id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at;
	`

	var (
		t             models.Task
		descField     sql.NullString
		commentsField sql.NullString
		assigneeField sql.NullInt64
		dueField      sql.NullTime
	)

	err = tx.QueryRowContext(
		ctx,
		query,
		projectID,
		title,
		descParam,
		status,
		priority,
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
		v := descField.String
		t.Description = &v
	}
	if commentsField.Valid {
		v := commentsField.String
		t.Comments = &v
	}
	if assigneeField.Valid {
		v := assigneeField.Int64
		t.AssigneeID = &v
	}
	if dueField.Valid {
		v := dueField.Time
		t.DueAt = &v
	}

	for _, uid := range assigneeIDs {
		_, err = tx.ExecContext(
			ctx,
			`INSERT INTO task_assignees (task_id, user_id) VALUES ($1, $2) ON CONFLICT DO NOTHING`,
			t.ID,
			uid,
		)
		if err != nil {
			return nil, fmt.Errorf("insert task_assignees: %w", err)
		}
	}

	if err := tx.Commit(); err != nil {
		return nil, fmt.Errorf("commit: %w", err)
	}

	t.AssigneeIDs = assigneeIDs
	if t.AssigneeIDs == nil {
		t.AssigneeIDs = []int64{}
	}

	return &t, nil
}

// ListProjectTasks — список задач проекта + assignee_ids
func (s *TaskService) ListProjectTasks(ctx context.Context, userID, projectID int64) ([]models.Task, error) {
	if err := s.ensureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}

	if err := s.ensureProjectMember(ctx, projectID, userID); err != nil {
		return nil, err
	}

	rows, err := s.db.QueryContext(ctx, `
		SELECT id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at
		FROM tasks
		WHERE project_id = $1
		ORDER BY created_at DESC;
	`, projectID)
	if err != nil {
		return nil, fmt.Errorf("list tasks: %w", err)
	}
	defer rows.Close()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			assigneeField sql.NullInt64
			dueField      sql.NullTime
		)

		err = rows.Scan(
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
			return nil, fmt.Errorf("scan task: %w", err)
		}

		if descField.Valid {
			v := descField.String
			t.Description = &v
		}
		if commentsField.Valid {
			v := commentsField.String
			t.Comments = &v
		}
		if assigneeField.Valid {
			v := assigneeField.Int64
			t.AssigneeID = &v
		}
		if dueField.Valid {
			v := dueField.Time
			t.DueAt = &v
		}

		t.AssigneeIDs = []int64{}

		tasks = append(tasks, t)
		taskIDs = append(taskIDs, t.ID)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows tasks: %w", err)
	}

	if len(tasks) == 0 {
		return tasks, nil
	}

	assigneesMap, err := s.getAssigneesForTasks(ctx, taskIDs)
	if err != nil {
		return nil, err
	}

	for i := range tasks {
		ids := assigneesMap[tasks[i].ID]
		if ids == nil {
			ids = []int64{}
		}
		tasks[i].AssigneeIDs = ids
	}

	return tasks, nil
}

// GetProjectTaskByID — одна задача проекта + assignee_ids
func (s *TaskService) GetProjectTaskByID(ctx context.Context, userID, projectID, taskID int64) (*models.Task, error) {
	if err := s.ensureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}
	if err := s.ensureProjectMember(ctx, projectID, userID); err != nil {
		return nil, err
	}

	t, err := s.getTaskByID(ctx, taskID)
	if err != nil {
		return nil, err
	}

	if t.ProjectID != projectID {
		return nil, ErrTaskNotFound
	}

	return t, nil
}

// UpdateTask — частичное обновление задачи
func (s *TaskService) UpdateTask(ctx context.Context, ownerID, projectID, taskID int64, req models.PatchTaskRequest) (*models.Task, error) {
	var dbOwnerID int64
	err := s.db.QueryRowContext(ctx, `SELECT owner_id FROM projects WHERE id = $1`, projectID).Scan(&dbOwnerID)
	if err == sql.ErrNoRows {
		return nil, ErrProjectNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("select project owner: %w", err)
	}
	if dbOwnerID != ownerID {
		return nil, ErrNotProjectOwner
	}

	var taskProjectID int64
	err = s.db.QueryRowContext(ctx, `SELECT project_id FROM tasks WHERE id = $1`, taskID).Scan(&taskProjectID)
	if err == sql.ErrNoRows {
		return nil, ErrTaskNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("select task project: %w", err)
	}
	if taskProjectID != projectID {
		return nil, ErrTaskNotFound
	}

	titlePtr := req.Title
	if titlePtr == nil {
		titlePtr = req.TaskName
	}
	descPtr := req.Description
	if descPtr == nil {
		descPtr = req.TaskDescription
	}
	duePtr := req.DueAt
	if duePtr == nil {
		duePtr = req.DueDate
	}

	setParts := make([]string, 0, 10)
	args := make([]interface{}, 0, 10)
	idx := 1
	changed := false

	if titlePtr != nil {
		title := strings.TrimSpace(*titlePtr)
		if title == "" {
			return nil, ErrTaskTitleRequired
		}
		setParts = append(setParts, fmt.Sprintf("title = $%d", idx))
		args = append(args, title)
		idx++
		changed = true
	}

	if descPtr != nil {
		d := strings.TrimSpace(*descPtr)
		if d == "" {
			setParts = append(setParts, "description = NULL")
		} else {
			setParts = append(setParts, fmt.Sprintf("description = $%d", idx))
			args = append(args, d)
			idx++
		}
		changed = true
	}

	if req.Comments != nil {
		c := strings.TrimSpace(*req.Comments)
		if c == "" {
			setParts = append(setParts, "comments = NULL")
		} else {
			setParts = append(setParts, fmt.Sprintf("comments = $%d", idx))
			args = append(args, c)
			idx++
		}
		changed = true
	}

	if req.Status != nil {
		sv := *req.Status
		switch sv {
		case models.TaskStatusTodo, models.TaskStatusInProgress, models.TaskStatusDone, models.TaskStatusPostponed:
		default:
			return nil, ErrInvalidTaskStatusValue
		}
		setParts = append(setParts, fmt.Sprintf("status = $%d", idx))
		args = append(args, sv)
		idx++
		changed = true
	}

	if req.Priority != nil {
		pv := *req.Priority
		switch pv {
		case models.TaskPriorityLow, models.TaskPriorityMedium, models.TaskPriorityHigh:
		default:
			return nil, ErrInvalidTaskPriority
		}
		setParts = append(setParts, fmt.Sprintf("priority = $%d", idx))
		args = append(args, pv)
		idx++
		changed = true
	}

	if req.ClearAssigneeID {
		setParts = append(setParts, "assignee_id = NULL")
		changed = true
	} else if req.AssigneeID != nil {
		if *req.AssigneeID <= 0 {
			return nil, ErrInvalidAssigneeIDs
		}
		id := *req.AssigneeID
		if err := s.ensureUsersExist(ctx, []int64{id}); err != nil {
			return nil, err
		}
		if err := s.ensureUsersAreProjectMembers(ctx, projectID, []int64{id}); err != nil {
			return nil, err
		}
		setParts = append(setParts, fmt.Sprintf("assignee_id = $%d", idx))
		args = append(args, id)
		idx++
		changed = true
	}

	if req.ClearDueAt {
		setParts = append(setParts, "due_at = NULL")
		changed = true
	} else if duePtr != nil {
		setParts = append(setParts, fmt.Sprintf("due_at = $%d", idx))
		args = append(args, *duePtr)
		idx++
		changed = true
	}

	var newAssignees []int64
	if req.AssigneeIDs != nil {
		newAssignees, err = normalizeAssigneeIDs64(*req.AssigneeIDs)
		if err != nil {
			return nil, err
		}
		if len(newAssignees) > 0 {
			if err := s.ensureUsersExist(ctx, newAssignees); err != nil {
				return nil, err
			}
			if err := s.ensureUsersAreProjectMembers(ctx, projectID, newAssignees); err != nil {
				return nil, err
			}
		}
		changed = true
	}

	if !changed {
		return nil, ErrNoFieldsToUpdate
	}

	setParts = append(setParts, "updated_at = NOW()")

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("begin tx: %w", err)
	}
	defer func() { _ = tx.Rollback() }()

	args = append(args, taskID)
	query := fmt.Sprintf(`
		UPDATE tasks
		SET %s
		WHERE id = $%d
		RETURNING id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at;
	`, strings.Join(setParts, ", "), idx)

	var (
		t             models.Task
		descField     sql.NullString
		commentsField sql.NullString
		assigneeField sql.NullInt64
		dueField      sql.NullTime
	)

	err = tx.QueryRowContext(ctx, query, args...).Scan(
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
		return nil, fmt.Errorf("update task: %w", err)
	}

	if descField.Valid {
		v := descField.String
		t.Description = &v
	}
	if commentsField.Valid {
		v := commentsField.String
		t.Comments = &v
	}
	if assigneeField.Valid {
		v := assigneeField.Int64
		t.AssigneeID = &v
	}
	if dueField.Valid {
		v := dueField.Time
		t.DueAt = &v
	}

	if req.AssigneeIDs != nil {
		_, err = tx.ExecContext(ctx, `DELETE FROM task_assignees WHERE task_id = $1`, taskID)
		if err != nil {
			return nil, fmt.Errorf("delete task_assignees: %w", err)
		}
		for _, uid := range newAssignees {
			_, err = tx.ExecContext(
				ctx,
				`INSERT INTO task_assignees (task_id, user_id) VALUES ($1, $2) ON CONFLICT DO NOTHING`,
				taskID,
				uid,
			)
			if err != nil {
				return nil, fmt.Errorf("insert task_assignees: %w", err)
			}
		}
	}

	if err := tx.Commit(); err != nil {
		return nil, fmt.Errorf("commit: %w", err)
	}

	t.AssigneeIDs = newAssignees
	if t.AssigneeIDs == nil {
		t.AssigneeIDs = []int64{}
	}

	return &t, nil
}

// ensureProjectExists — чтобы отличать "проекта нет" от "ты не участник"
func (s *TaskService) ensureProjectExists(ctx context.Context, projectID int64) error {
	var id int64
	err := s.db.QueryRowContext(ctx, `SELECT id FROM projects WHERE id = $1`, projectID).Scan(&id)
	if err == sql.ErrNoRows {
		return ErrProjectNotFound
	}
	if err != nil {
		return fmt.Errorf("ensureProjectExists: %w", err)
	}
	return nil
}

// ensureProjectMember — доступ по таблице project_members (owner тоже там есть)
func (s *TaskService) ensureProjectMember(ctx context.Context, projectID, userID int64) error {
	var ok bool
	err := s.db.QueryRowContext(ctx, `
		SELECT EXISTS(
			SELECT 1 FROM project_members
			WHERE project_id = $1 AND user_id = $2
		);
	`, projectID, userID).Scan(&ok)
	if err != nil {
		return fmt.Errorf("ensureProjectMember: %w", err)
	}
	if !ok {
		return ErrNotProjectMember
	}
	return nil
}

// getAssigneesForTasks — 1 запросом вытягиваем исполнителей для набора taskID
func (s *TaskService) getAssigneesForTasks(ctx context.Context, taskIDs []int64) (map[int64][]int64, error) {
	ph := makePlaceholders(1, len(taskIDs))
	query := `SELECT task_id, user_id FROM task_assignees WHERE task_id IN (` + ph + `) ORDER BY task_id, user_id;`

	args := make([]interface{}, 0, len(taskIDs))
	for _, id := range taskIDs {
		args = append(args, id)
	}

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, fmt.Errorf("query task_assignees: %w", err)
	}
	defer rows.Close()

	out := make(map[int64][]int64)
	for rows.Next() {
		var taskID, userID int64
		if err := rows.Scan(&taskID, &userID); err != nil {
			return nil, fmt.Errorf("scan task_assignees: %w", err)
		}
		out[taskID] = append(out[taskID], userID)
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows task_assignees: %w", err)
	}

	return out, nil
}

// TakeTask — добавляем пользователя в task_assignees
func (s *TaskService) TakeTask(ctx context.Context, userID, taskID int64) (*models.Task, error) {
	var (
		projectID int64
		status    string
	)

	err := s.db.QueryRowContext(
		ctx,
		`SELECT project_id, status FROM tasks WHERE id = $1`,
		taskID,
	).Scan(&projectID, &status)

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
			SELECT 1 FROM project_members WHERE project_id = $1 AND user_id = $2
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

	_, _ = s.db.ExecContext(
		ctx,
		`INSERT INTO task_assignees (task_id, user_id) VALUES ($1, $2) ON CONFLICT DO NOTHING`,
		taskID, userID,
	)

	_, err = s.db.ExecContext(
		ctx,
		`UPDATE tasks
		 SET status = 'in_progress',
		     assignee_id = $1,
		     updated_at = NOW()
		 WHERE id = $2`,
		userID, taskID,
	)
	if err != nil {
		return nil, fmt.Errorf("update task: %w", err)
	}

	return s.getTaskByID(ctx, taskID)
}

func (s *TaskService) CompleteTask(ctx context.Context, userID, taskID int64) (*models.Task, error) {
	var (
		status        string
		assigneeField sql.NullInt64
	)

	err := s.db.QueryRowContext(
		ctx,
		`SELECT status, assignee_id FROM tasks WHERE id = $1`,
		taskID,
	).Scan(&status, &assigneeField)

	if err == sql.ErrNoRows {
		return nil, ErrTaskNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("query task before complete: %w", err)
	}

	if status != "in_progress" {
		return nil, ErrInvalidTaskStatus
	}

	if !assigneeField.Valid || assigneeField.Int64 != userID {
		return nil, ErrNotTaskAssignee
	}

	_, err = s.db.ExecContext(
		ctx,
		`UPDATE tasks SET status = 'done', updated_at = NOW() WHERE id = $1`,
		taskID,
	)
	if err != nil {
		return nil, fmt.Errorf("complete task: %w", err)
	}

	return s.getTaskByID(ctx, taskID)
}

// getTaskByID — возвращает и список assignee_ids
func (s *TaskService) getTaskByID(ctx context.Context, taskID int64) (*models.Task, error) {
	query := `
		SELECT id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at
		FROM tasks
		WHERE id = $1;
	`

	var (
		t             models.Task
		descField     sql.NullString
		commentsField sql.NullString
		assigneeField sql.NullInt64
		dueField      sql.NullTime
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
		return nil, fmt.Errorf("getTaskByID: %w", err)
	}

	if descField.Valid {
		v := descField.String
		t.Description = &v
	}
	if commentsField.Valid {
		v := commentsField.String
		t.Comments = &v
	}
	if assigneeField.Valid {
		v := assigneeField.Int64
		t.AssigneeID = &v
	}
	if dueField.Valid {
		v := dueField.Time
		t.DueAt = &v
	}

	ids, err := s.getTaskAssigneeIDs(ctx, taskID)
	if err != nil {
		return nil, err
	}
	t.AssigneeIDs = ids
	if t.AssigneeIDs == nil {
		t.AssigneeIDs = []int64{}
	}

	return &t, nil
}

func (s *TaskService) getTaskAssigneeIDs(ctx context.Context, taskID int64) ([]int64, error) {
	rows, err := s.db.QueryContext(
		ctx,
		`SELECT user_id FROM task_assignees WHERE task_id = $1 ORDER BY user_id`,
		taskID,
	)
	if err != nil {
		return nil, fmt.Errorf("query task_assignees: %w", err)
	}
	defer rows.Close()

	out := make([]int64, 0)
	for rows.Next() {
		var id int64
		if err := rows.Scan(&id); err != nil {
			return nil, fmt.Errorf("scan task_assignees: %w", err)
		}
		out = append(out, id)
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows task_assignees: %w", err)
	}

	return out, nil
}

func normalizeAssigneeIDs(ids []int) ([]int64, error) {
	if len(ids) == 0 {
		return []int64{}, nil
	}

	seen := make(map[int64]struct{}, len(ids))
	out := make([]int64, 0, len(ids))

	for _, v := range ids {
		if v <= 0 {
			return nil, ErrInvalidAssigneeIDs
		}
		id := int64(v)
		if _, ok := seen[id]; ok {
			continue
		}
		seen[id] = struct{}{}
		out = append(out, id)
	}

	return out, nil
}

func normalizeAssigneeIDs64(ids []int64) ([]int64, error) {
	if len(ids) == 0 {
		return []int64{}, nil
	}

	seen := make(map[int64]struct{}, len(ids))
	out := make([]int64, 0, len(ids))

	for _, v := range ids {
		if v <= 0 {
			return nil, ErrInvalidAssigneeIDs
		}
		if _, ok := seen[v]; ok {
			continue
		}
		seen[v] = struct{}{}
		out = append(out, v)
	}

	return out, nil
}

func (s *TaskService) ensureUsersExist(ctx context.Context, ids []int64) error {
	ph := makePlaceholders(1, len(ids))
	query := `SELECT id FROM users WHERE id IN (` + ph + `);`

	args := make([]interface{}, 0, len(ids))
	for _, id := range ids {
		args = append(args, id)
	}

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return fmt.Errorf("ensureUsersExist: %w", err)
	}
	defer rows.Close()

	found := make(map[int64]struct{}, len(ids))
	for rows.Next() {
		var id int64
		if err := rows.Scan(&id); err != nil {
			return fmt.Errorf("scan users exist: %w", err)
		}
		found[id] = struct{}{}
	}
	if err := rows.Err(); err != nil {
		return fmt.Errorf("rows users exist: %w", err)
	}

	if len(found) != len(ids) {
		return ErrAssigneeNotFound
	}
	return nil
}

func (s *TaskService) ensureUsersAreProjectMembers(ctx context.Context, projectID int64, ids []int64) error {
	ph := makePlaceholders(2, len(ids))
	query := `
		SELECT user_id
		FROM project_members
		WHERE project_id = $1 AND user_id IN (` + ph + `);
	`

	args := make([]interface{}, 0, 1+len(ids))
	args = append(args, projectID)
	for _, id := range ids {
		args = append(args, id)
	}

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return fmt.Errorf("ensureUsersAreProjectMembers: %w", err)
	}
	defer rows.Close()

	found := make(map[int64]struct{}, len(ids))
	for rows.Next() {
		var id int64
		if err := rows.Scan(&id); err != nil {
			return fmt.Errorf("scan project members: %w", err)
		}
		found[id] = struct{}{}
	}
	if err := rows.Err(); err != nil {
		return fmt.Errorf("rows project members: %w", err)
	}

	if len(found) != len(ids) {
		return ErrAssigneesNotInProject
	}
	return nil
}

func makePlaceholders(start, count int) string {
	var b strings.Builder
	for i := 0; i < count; i++ {
		if i > 0 {
			b.WriteByte(',')
		}
		b.WriteByte('$')
		b.WriteString(strconv.Itoa(start + i))
	}
	return b.String()
}

// DeleteTask удаляет задачу и все связанные данные
func (s *TaskService) DeleteTask(ctx context.Context, userID, projectID, taskID int64) error {
	var taskProjectID int64
	var taskAssigneeID sql.NullInt64
	err := s.db.QueryRowContext(ctx,
		`SELECT project_id, assignee_id FROM tasks WHERE id = $1`,
		taskID,
	).Scan(&taskProjectID, &taskAssigneeID)

	if err == sql.ErrNoRows {
		return ErrTaskNotFound
	}
	if err != nil {
		return fmt.Errorf("query task: %w", err)
	}

	if taskProjectID != projectID {
		return ErrTaskNotFound
	}

	isOwner, err := s.isProjectOwner(ctx, projectID, userID)
	if err != nil {
		return err
	}

	isAssignee := false
	if taskAssigneeID.Valid && taskAssigneeID.Int64 == userID {
		isAssignee = true
	}

	if !isOwner && !isAssignee {
		isMember, err := s.isProjectMember(ctx, projectID, userID)
		if err != nil {
			return err
		}
		if !isMember {
			return ErrNotProjectMember
		}
	}

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return fmt.Errorf("begin tx: %w", err)
	}
	defer func() {
		if err != nil {
			_ = tx.Rollback()
		}
	}()

	_, err = tx.ExecContext(ctx,
		`DELETE FROM notifications WHERE task_id = $1`,
		taskID,
	)
	if err != nil {
		return fmt.Errorf("delete notifications: %w", err)
	}

	_, err = tx.ExecContext(ctx,
		`DELETE FROM task_assignees WHERE task_id = $1`,
		taskID,
	)
	if err != nil {
		return fmt.Errorf("delete task assignees: %w", err)
	}

	_, err = tx.ExecContext(ctx,
		`DELETE FROM tasks WHERE id = $1`,
		taskID,
	)
	if err != nil {
		return fmt.Errorf("delete task: %w", err)
	}

	err = tx.Commit()
	if err != nil {
		return fmt.Errorf("commit: %w", err)
	}

	return nil
}

// isProjectOwner проверяет, является ли пользователь владельцем проекта
func (s *TaskService) isProjectOwner(ctx context.Context, projectID, userID int64) (bool, error) {
	const q = `SELECT owner_id FROM projects WHERE id = $1`
	var ownerID int64

	err := s.db.QueryRowContext(ctx, q, projectID).Scan(&ownerID)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return false, ErrProjectNotFound
		}
		return false, err
	}

	return ownerID == userID, nil
}

// isProjectMember проверяет, является ли пользователь участником проекта
func (s *TaskService) isProjectMember(ctx context.Context, projectID, userID int64) (bool, error) {
	var exists bool
	err := s.db.QueryRowContext(ctx,
		`SELECT EXISTS(SELECT 1 FROM project_members WHERE project_id = $1 AND user_id = $2)`,
		projectID,
		userID,
	).Scan(&exists)

	if err != nil {
		return false, fmt.Errorf("check project member: %w", err)
	}

	return exists, nil
}

// validateTaskFilter проверяет валидность параметров фильтрации
func (s *TaskService) validateTaskFilter(filter models.TaskFilterRequest) error {
	if filter.Priority != nil {
		switch *filter.Priority {
		case models.TaskPriorityLow, models.TaskPriorityMedium, models.TaskPriorityHigh:
		default:
			return ErrInvalidTaskPriority
		}
	}
	if filter.ProjectID != nil && *filter.ProjectID <= 0 {
		return ErrInvalidProjectID
	}
	return nil
}

// ListTasksWithFilter возвращает список задач с фильтрацией по проекту и/или приоритету
func (s *TaskService) ListTasksWithFilter(ctx context.Context, userID int64, filter models.TaskFilterRequest) ([]models.Task, error) {
	if err := s.validateTaskFilter(filter); err != nil {
		return nil, err
	}

	// Если выбран конкретный проект — проверяем существование и членство
	if filter.ProjectID != nil {
		if err := s.ensureProjectExists(ctx, *filter.ProjectID); err != nil {
			return nil, err
		}
		if err := s.ensureProjectMember(ctx, *filter.ProjectID, userID); err != nil {
			return nil, err
		}
	}

	query := `
    SELECT id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at
    FROM tasks
    WHERE 1=1
  `

	args := make([]interface{}, 0)
	argIndex := 1

	// Если projectID НЕ задан — показываем задачи только из проектов, куда юзер имеет доступ (member или owner)
	if filter.ProjectID == nil {
		query += fmt.Sprintf(`
      AND (
        project_id IN (SELECT project_id FROM project_members WHERE user_id = $%d)
        OR project_id IN (SELECT id FROM projects WHERE owner_id = $%d)
      )
    `, argIndex, argIndex)
		args = append(args, userID)
		argIndex++
	}

	if filter.ProjectID != nil {
		query += fmt.Sprintf(" AND project_id = $%d", argIndex)
		args = append(args, *filter.ProjectID)
		argIndex++
	}

	if filter.Priority != nil {
		query += fmt.Sprintf(" AND priority = $%d", argIndex)
		args = append(args, *filter.Priority)
	}

	query += " ORDER BY created_at DESC"

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, fmt.Errorf("list tasks with filter: %w", err)
	}
	defer rows.Close()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			assigneeField sql.NullInt64
			dueField      sql.NullTime
		)

		err = rows.Scan(
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
			return nil, fmt.Errorf("scan task: %w", err)
		}

		if descField.Valid {
			v := descField.String
			t.Description = &v
		}
		if commentsField.Valid {
			v := commentsField.String
			t.Comments = &v
		}
		if assigneeField.Valid {
			v := assigneeField.Int64
			t.AssigneeID = &v
		}
		if dueField.Valid {
			v := dueField.Time
			t.DueAt = &v
		}

		t.AssigneeIDs = []int64{}

		tasks = append(tasks, t)
		taskIDs = append(taskIDs, t.ID)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows tasks: %w", err)
	}

	if len(tasks) == 0 {
		return tasks, nil
	}

	assigneesMap, err := s.getAssigneesForTasks(ctx, taskIDs)
	if err != nil {
		return nil, err
	}

	for i := range tasks {
		ids := assigneesMap[tasks[i].ID]
		if ids == nil {
			ids = []int64{}
		}
		tasks[i].AssigneeIDs = ids
	}

	return tasks, nil
}

// userHasProjectAccess проверяет, имеет ли пользователь доступ хотя бы к одному проекту
func (s *TaskService) userHasProjectAccess(ctx context.Context, userID int64) (bool, error) {
	var exists bool
	err := s.db.QueryRowContext(ctx, `
    SELECT EXISTS(
      SELECT 1 FROM project_members WHERE user_id = $1
    );`, userID).Scan(&exists)

	if err != nil {
		return false, fmt.Errorf("check project access: %w", err)
	}

	return exists, nil
}

// SubmitForReview ставит статус задачи с in_progress на in_review
func (s *TaskService) SubmitForReview(ctx context.Context, userID, projectID, taskID int64) error {
	const q = `
    UPDATE tasks t
    SET
        status_before_review = t.status,
        status = 'in_review',
        submitted_at = NOW(),
        updated_at = NOW()
    FROM task_assignees ta
    WHERE t.id = $1
      AND t.project_id = $2
      AND t.status = 'in_progress'
      AND ta.task_id = t.id
      AND ta.user_id = $3
`

	res, err := s.db.ExecContext(ctx, q, taskID, projectID, userID)
	if err != nil {
		return err
	}
	n, _ := res.RowsAffected()
	if n == 0 {
		return ErrTaskNotFound
	}
	return nil
}

// ReviewTask логика принять задачу или отклонить
func (s *TaskService) ReviewTask(ctx context.Context, ownerID, projectID, taskID int64, decision string, comment *string, newDueAt *time.Time) error {
	ok, err := s.isProjectOwner(ctx, projectID, ownerID)
	if err != nil {
		return err
	}
	if !ok {
		return ErrNotProjectOwner
	}

	switch decision {
	case "approve":
		const q = `
            UPDATE tasks
            SET
                status = 'done',
                reviewed_at = NOW(),
                reviewed_by = $1,
                review_comment = $2,
                updated_at = NOW()
            WHERE id = $3
              AND project_id = $4
              AND status = 'in_review'
        `
		res, err := s.db.ExecContext(ctx, q, ownerID, comment, taskID, projectID)
		if err != nil {
			return err
		}
		n, _ := res.RowsAffected()
		if n == 0 {
			return ErrTaskNotFoundOrBadStatus
		}
		return nil

	case "reject":
		if newDueAt == nil {
			return ErrDueAtRequired
		}

		const q = `
            UPDATE tasks
            SET
                status = 'in_progress',
                due_at = $1,
                reviewed_at = NOW(),
                reviewed_by = $2,
                review_comment = $3,
                updated_at = NOW()
            WHERE id = $4
              AND project_id = $5
              AND status = 'in_review'
        `
		res, err := s.db.ExecContext(ctx, q, newDueAt, ownerID, comment, taskID, projectID)
		if err != nil {
			return err
		}
		n, _ := res.RowsAffected()
		if n == 0 {
			return ErrTaskNotFoundOrBadStatus
		}
		return nil

	default:
		return ErrBadDecision
	}
}

func (s *TaskService) validateTaskCategory(category models.TaskCategory) error {
	switch category {
	case models.TaskCategoryCurrent, models.TaskCategoryReview, models.TaskCategoryInactive:
		return nil
	default:
		return ErrInvalidTaskCategory
	}
}

func (s *TaskService) ListProjectTasksByCategory(ctx context.Context, userID, projectID int64, category models.TaskCategory) ([]models.Task, error) {
	if err := s.ensureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}
	if err := s.ensureProjectMember(ctx, projectID, userID); err != nil {
		return nil, err
	}
	if err := s.validateTaskCategory(category); err != nil {
		return nil, err
	}

	query := `
		SELECT id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at
		FROM tasks
		WHERE project_id = $1
	`

	switch category {
	case models.TaskCategoryCurrent:
		query += `
			AND status IN ('todo', 'in_progress', 'postponed')
			AND (due_at IS NULL OR due_at >= NOW())
		`
	case models.TaskCategoryReview:
		query += `
			AND status = 'in_review'
		`
	case models.TaskCategoryInactive:
		query += `
			AND (
				status = 'done'
				OR (status <> 'done' AND due_at IS NOT NULL AND due_at < NOW())
			)
		`
	}

	query += ` ORDER BY created_at DESC;`

	rows, err := s.db.QueryContext(ctx, query, projectID)
	if err != nil {
		return nil, fmt.Errorf("list project tasks by category: %w", err)
	}
	defer rows.Close()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			assigneeField sql.NullInt64
			dueField      sql.NullTime
		)

		err = rows.Scan(
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
			return nil, fmt.Errorf("scan task: %w", err)
		}

		if descField.Valid {
			v := descField.String
			t.Description = &v
		}
		if commentsField.Valid {
			v := commentsField.String
			t.Comments = &v
		}
		if assigneeField.Valid {
			v := assigneeField.Int64
			t.AssigneeID = &v
		}
		if dueField.Valid {
			v := dueField.Time
			t.DueAt = &v
		}

		t.AssigneeIDs = []int64{}
		tasks = append(tasks, t)
		taskIDs = append(taskIDs, t.ID)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows tasks: %w", err)
	}

	if len(tasks) == 0 {
		return tasks, nil
	}

	assigneesMap, err := s.getAssigneesForTasks(ctx, taskIDs)
	if err != nil {
		return nil, err
	}

	for i := range tasks {
		ids := assigneesMap[tasks[i].ID]
		if ids == nil {
			ids = []int64{}
		}
		tasks[i].AssigneeIDs = ids
	}

	return tasks, nil
}

func (s *TaskService) ListNearestTasks(ctx context.Context, userID int64) ([]models.Task, error) {
	rows, err := s.db.QueryContext(ctx, `
		SELECT DISTINCT t.id, t.project_id, t.title, t.description, t.comments, t.status, t.priority, t.assignee_id, t.due_at, t.created_at, t.updated_at
		FROM tasks t
		JOIN task_assignees ta ON ta.task_id = t.id
		WHERE ta.user_id = $1
		  AND t.status <> 'done'
		  AND t.due_at IS NOT NULL
		  AND t.due_at >= NOW()
		ORDER BY t.due_at ASC, t.created_at ASC
		LIMIT 3;
	`, userID)
	if err != nil {
		return nil, fmt.Errorf("list nearest tasks: %w", err)
	}
	defer rows.Close()

	tasks := make([]models.Task, 0, 3)
	taskIDs := make([]int64, 0, 3)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			assigneeField sql.NullInt64
			dueField      sql.NullTime
		)

		err = rows.Scan(
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
			return nil, fmt.Errorf("scan nearest task: %w", err)
		}

		if descField.Valid {
			v := descField.String
			t.Description = &v
		}
		if commentsField.Valid {
			v := commentsField.String
			t.Comments = &v
		}
		if assigneeField.Valid {
			v := assigneeField.Int64
			t.AssigneeID = &v
		}
		if dueField.Valid {
			v := dueField.Time
			t.DueAt = &v
		}

		t.AssigneeIDs = []int64{}
		tasks = append(tasks, t)
		taskIDs = append(taskIDs, t.ID)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows nearest tasks: %w", err)
	}

	if len(tasks) == 0 {
		return tasks, nil
	}

	assigneesMap, err := s.getAssigneesForTasks(ctx, taskIDs)
	if err != nil {
		return nil, err
	}

	for i := range tasks {
		ids := assigneesMap[tasks[i].ID]
		if ids == nil {
			ids = []int64{}
		}
		tasks[i].AssigneeIDs = ids
	}

	return tasks, nil
}

func (s *TaskService) ListProjectTasksByAssignees(ctx context.Context, userID, projectID int64, assigneeFilter []int64) ([]models.Task, error) {
	if err := s.ensureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}
	if err := s.ensureProjectMember(ctx, projectID, userID); err != nil {
		return nil, err
	}

	query := `
		SELECT id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at
		FROM tasks
		WHERE project_id = $1
	`
	args := []interface{}{projectID}

	if len(assigneeFilter) > 0 {
		ph := makePlaceholders(2, len(assigneeFilter))
		query += fmt.Sprintf(` AND EXISTS (
			SELECT 1 FROM task_assignees ta 
			WHERE ta.task_id = tasks.id AND ta.user_id IN (%s)
		)`, ph)
		for _, uid := range assigneeFilter {
			args = append(args, uid)
		}
	}

	query += ` ORDER BY created_at DESC;`

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, fmt.Errorf("list tasks by assignees: %w", err)
	}
	defer rows.Close()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			assigneeField sql.NullInt64
			dueField      sql.NullTime
		)

		err = rows.Scan(
			&t.ID, &t.ProjectID, &t.Title, &descField, &commentsField,
			&t.Status, &t.Priority, &assigneeField, &dueField,
			&t.CreatedAt, &t.UpdatedAt,
		)
		if err != nil {
			return nil, fmt.Errorf("scan task: %w", err)
		}

		if descField.Valid {
			v := descField.String
			t.Description = &v
		}
		if commentsField.Valid {
			v := commentsField.String
			t.Comments = &v
		}
		if assigneeField.Valid {
			v := assigneeField.Int64
			t.AssigneeID = &v
		}
		if dueField.Valid {
			v := dueField.Time
			t.DueAt = &v
		}

		t.AssigneeIDs = []int64{}
		tasks = append(tasks, t)
		taskIDs = append(taskIDs, t.ID)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows tasks: %w", err)
	}

	if len(tasks) == 0 {
		return tasks, nil
	}

	assigneesMap, err := s.getAssigneesForTasks(ctx, taskIDs)
	if err != nil {
		return nil, err
	}

	for i := range tasks {
		ids := assigneesMap[tasks[i].ID]
		if ids == nil {
			ids = []int64{}
		}
		tasks[i].AssigneeIDs = ids
	}

	return tasks, nil
}

func (s *TaskService) GetMyTasks(ctx context.Context, userID int64) ([]models.Task, error) {
	query := `
		SELECT id, project_id, title, description, comments, status, priority, assignee_id, due_at, created_at, updated_at
		FROM tasks t
		JOIN task_assignees ta ON ta.task_id = t.id
		WHERE ta.user_id = $1
		ORDER BY t.created_at DESC;
	`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, fmt.Errorf("get my tasks: %w", err)
	}
	defer rows.Close()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			assigneeField sql.NullInt64
			dueField      sql.NullTime
		)

		err = rows.Scan(
			&t.ID, &t.ProjectID, &t.Title, &descField, &commentsField,
			&t.Status, &t.Priority, &assigneeField, &dueField,
			&t.CreatedAt, &t.UpdatedAt,
		)
		if err != nil {
			return nil, fmt.Errorf("scan my task: %w", err)
		}

		if descField.Valid {
			v := descField.String
			t.Description = &v
		}
		if commentsField.Valid {
			v := commentsField.String
			t.Comments = &v
		}
		if assigneeField.Valid {
			v := assigneeField.Int64
			t.AssigneeID = &v
		}
		if dueField.Valid {
			v := dueField.Time
			t.DueAt = &v
		}

		t.AssigneeIDs = []int64{}
		tasks = append(tasks, t)
		taskIDs = append(taskIDs, t.ID)
	}

	if err = rows.Err(); err != nil {
		return nil, fmt.Errorf("rows my tasks: %w", err)
	}

	if len(tasks) == 0 {
		return tasks, nil
	}

	assigneesMap, err := s.getAssigneesForTasks(ctx, taskIDs)
	if err != nil {
		return nil, err
	}
	for i := range tasks {
		if ids := assigneesMap[tasks[i].ID]; ids != nil {
			tasks[i].AssigneeIDs = ids
		}
	}

	return tasks, nil
}
