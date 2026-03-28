package services

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
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
	ErrNoFieldsToUpdate        = errors.New("no fields to update")
	ErrTextTooLong             = errors.New("text is too long (max 255 characters)")
	ErrDueDateInPast           = errors.New("due date cannot be in the past")
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
	if req.TaskDescription != nil {
		d := strings.TrimSpace(*req.TaskDescription)
		// ФИКС: Защита от переполнения при создании задачи
		if len(d) > 255 {
			return nil, ErrTextTooLong
		}
		if d != "" {
			descParam = d
		} else {
			descParam = nil
		}
	} else {
		descParam = nil
	}

	var dueParam interface{}
	if req.DueDate != nil {
		// ФИКС: Запрет на создание задач с дедлайном в прошлом
		if req.DueDate.Before(time.Now()) {
			return nil, ErrDueDateInPast
		}
		dueParam = *req.DueDate
	} else {
		dueParam = nil
	}

	assigneeIDs, err := NormalizeAssigneeIDs(req.AssigneeIDs)
	if err != nil {
		return nil, err
	}

	if len(assigneeIDs) > 0 {
		if err := s.EnsureUsersExist(ctx, assigneeIDs); err != nil {
			return nil, err
		}
		if err := s.EnsureUsersAreProjectMembers(ctx, projectID, assigneeIDs); err != nil {
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
        RETURNING id, project_id, title, description, comments, status, priority, due_at, created_at, updated_at;
    `

	var (
		t             models.Task
		descField     sql.NullString
		commentsField sql.NullString
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

// ListProjectTasks — список задач проекта + пагинация
func (s *TaskService) ListProjectTasks(ctx context.Context, userID, projectID, limit, offset int64) ([]models.Task, error) {
	if err := s.EnsureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}

	if err := s.EnsureProjectMember(ctx, projectID, userID); err != nil {
		return nil, err
	}

	// ДОБАВЛЕН LIMIT и OFFSET
	rows, err := s.db.QueryContext(ctx, `
        SELECT id, project_id, title, description, comments, status, priority, due_at, created_at, updated_at
        FROM tasks
        WHERE project_id = $1
        ORDER BY created_at DESC
        LIMIT $2 OFFSET $3;
    `, projectID, limit, offset)
	if err != nil {
		return nil, fmt.Errorf("list tasks: %w", err)
	}
	defer func() {
		_ = rows.Close()
	}()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			dueField      sql.NullTime
		)

		err = rows.Scan(
			&t.ID, &t.ProjectID, &t.Title, &descField, &commentsField,
			&t.Status, &t.Priority, &dueField,
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

	assigneesMap, err := s.GetAssigneesForTasks(ctx, taskIDs)
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
	if err := s.EnsureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}
	if err := s.EnsureProjectMember(ctx, projectID, userID); err != nil {
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

	// ФИКС 500 ОШИБКИ: Защита от переполнения
	if descPtr != nil {
		d := strings.TrimSpace(*descPtr)
		if len(d) > 255 {
			return nil, ErrTextTooLong
		}

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
		if len(c) > 255 {
			return nil, ErrTextTooLong
		}

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
		newAssignees, err = NormalizeAssigneeIDs64(*req.AssigneeIDs)
		if err != nil {
			return nil, err
		}
		if len(newAssignees) > 0 {
			if err := s.EnsureUsersExist(ctx, newAssignees); err != nil {
				return nil, err
			}
			if err := s.EnsureUsersAreProjectMembers(ctx, projectID, newAssignees); err != nil {
				return nil, err
			}
		}
		changed = true
	}

	if !changed {
		return nil, ErrNoFieldsToUpdate
	}

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("begin tx: %w", err)
	}
	defer func() { _ = tx.Rollback() }()

	var t models.Task

	if len(setParts) > 0 {
		setParts = append(setParts, "updated_at = NOW()")
		args = append(args, taskID)
		query := fmt.Sprintf(`
            UPDATE tasks
            SET %s
            WHERE id = $%d
            RETURNING id, project_id, title, description, comments, status, priority, due_at, created_at, updated_at;
        `, strings.Join(setParts, ", "), idx)

		var (
			descField     sql.NullString
			commentsField sql.NullString
			dueField      sql.NullTime
		)

		err = tx.QueryRowContext(ctx, query, args...).Scan(
			&t.ID, &t.ProjectID, &t.Title, &descField, &commentsField,
			&t.Status, &t.Priority, &dueField, &t.CreatedAt, &t.UpdatedAt,
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
		if dueField.Valid {
			v := dueField.Time
			t.DueAt = &v
		}
	} else {
		taskObj, err := s.getTaskByID(ctx, taskID)
		if err != nil {
			return nil, err
		}
		t = *taskObj
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
				taskID, uid,
			)
			if err != nil {
				return nil, fmt.Errorf("insert task_assignees: %w", err)
			}
		}
		t.AssigneeIDs = newAssignees
	} else {
		ids, err := s.GetTaskAssigneeIDs(ctx, taskID)
		if err != nil {
			return nil, err
		}
		t.AssigneeIDs = ids
	}

	if t.AssigneeIDs == nil {
		t.AssigneeIDs = []int64{}
	}

	if err := tx.Commit(); err != nil {
		return nil, fmt.Errorf("commit: %w", err)
	}

	return &t, nil
}

func (s *TaskService) ListTasksWithFilter(ctx context.Context, userID int64, filter models.TaskFilterRequest) ([]models.Task, error) {
	if err := s.validateTaskFilter(filter); err != nil {
		return nil, err
	}

	// Базовый запрос: ищем задачи в проектах, где пользователь является участником
	query := `
        SELECT DISTINCT t.id, t.project_id, t.title, t.description, t.comments, t.status, t.priority, t.due_at, t.created_at, t.updated_at
        FROM tasks t
        JOIN project_members pm ON pm.project_id = t.project_id
        WHERE pm.user_id = $1
    `
	args := []interface{}{userID}
	idx := 2

	if filter.ProjectID != nil {
		query += fmt.Sprintf(" AND t.project_id = $%d", idx)
		args = append(args, *filter.ProjectID)
		idx++
	}

	if filter.Priority != nil {
		query += fmt.Sprintf(" AND t.priority = $%d", idx)
		args = append(args, *filter.Priority)
		idx++
	}

	query += " ORDER BY t.created_at DESC;"

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, fmt.Errorf("list tasks with filter: %w", err)
	}
	defer func() {
		_ = rows.Close()
	}()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			dueField      sql.NullTime
		)

		err = rows.Scan(
			&t.ID, &t.ProjectID, &t.Title, &descField, &commentsField,
			&t.Status, &t.Priority, &dueField,
			&t.CreatedAt, &t.UpdatedAt,
		)
		if err != nil {
			return nil, fmt.Errorf("scan filtered task: %w", err)
		}

		if descField.Valid {
			v := descField.String
			t.Description = &v
		}
		if commentsField.Valid {
			v := commentsField.String
			t.Comments = &v
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
		return nil, fmt.Errorf("rows filtered tasks: %w", err)
	}

	if len(tasks) == 0 {
		return tasks, nil
	}

	assigneesMap, err := s.GetAssigneesForTasks(ctx, taskIDs)
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

// getTaskByID — возвращает задачу и список assignee_ids
func (s *TaskService) getTaskByID(ctx context.Context, taskID int64) (*models.Task, error) {
	query := `
        SELECT id, project_id, title, description, comments, status, priority, due_at, created_at, updated_at
        FROM tasks
        WHERE id = $1;
    `

	var (
		t             models.Task
		descField     sql.NullString
		commentsField sql.NullString
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
	if dueField.Valid {
		v := dueField.Time
		t.DueAt = &v
	}

	ids, err := s.GetTaskAssigneeIDs(ctx, taskID)
	if err != nil {
		return nil, err
	}
	t.AssigneeIDs = ids
	if t.AssigneeIDs == nil {
		t.AssigneeIDs = []int64{}
	}

	return &t, nil
}

// DeleteTask удаляет задачу и все связанные данные
func (s *TaskService) DeleteTask(ctx context.Context, userID, projectID, taskID int64) error {
	var taskProjectID int64
	err := s.db.QueryRowContext(ctx,
		`SELECT project_id FROM tasks WHERE id = $1`,
		taskID,
	).Scan(&taskProjectID)

	if err == sql.ErrNoRows {
		return ErrTaskNotFound
	}
	if err != nil {
		return fmt.Errorf("query task: %w", err)
	}

	if taskProjectID != projectID {
		return ErrTaskNotFound
	}

	isOwner, err := s.IsProjectOwner(ctx, projectID, userID)
	if err != nil {
		return err
	}

	var isAssignee bool
	err = s.db.QueryRowContext(
		ctx,
		`SELECT EXISTS(SELECT 1 FROM task_assignees WHERE task_id = $1 AND user_id = $2)`,
		taskID, userID,
	).Scan(&isAssignee)
	if err != nil {
		return fmt.Errorf("check if assignee: %w", err)
	}

	if !isOwner && !isAssignee {
		return ErrForbidden
	}

	_, err = s.db.ExecContext(ctx, `DELETE FROM tasks WHERE id = $1`, taskID)
	if err != nil {
		return fmt.Errorf("delete task: %w", err)
	}

	return nil
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

func (s *TaskService) ListProjectTasksByAssignees(ctx context.Context, userID, projectID int64, assigneeFilter []int64) ([]models.Task, error) {
	if err := s.EnsureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}
	if err := s.EnsureProjectMember(ctx, projectID, userID); err != nil {
		return nil, err
	}

	query := `
        SELECT id, project_id, title, description, comments, status, priority, due_at, created_at, updated_at
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

	defer func() {
		_ = rows.Close()
	}()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			dueField      sql.NullTime
		)

		err = rows.Scan(
			&t.ID, &t.ProjectID, &t.Title, &descField, &commentsField,
			&t.Status, &t.Priority, &dueField,
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

	assigneesMap, err := s.GetAssigneesForTasks(ctx, taskIDs)
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

func (s *TaskService) ListProjectTasksByCategory(ctx context.Context, userID, projectID int64, category models.TaskCategory) ([]models.Task, error) {
	if err := s.EnsureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}
	if err := s.EnsureProjectMember(ctx, projectID, userID); err != nil {
		return nil, err
	}
	if err := s.ValidateTaskCategory(category); err != nil {
		return nil, err
	}

	query := `
        SELECT id, project_id, title, description, comments, status, priority, due_at, created_at, updated_at
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
	defer func() {
		_ = rows.Close()
	}()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
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

	assigneesMap, err := s.GetAssigneesForTasks(ctx, taskIDs)
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
        SELECT DISTINCT t.id, t.project_id, t.title, t.description, t.comments, t.status, t.priority, t.due_at, t.created_at, t.updated_at
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
	defer func() {
		_ = rows.Close()
	}()

	tasks := make([]models.Task, 0, 3)
	taskIDs := make([]int64, 0, 3)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
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

	assigneesMap, err := s.GetAssigneesForTasks(ctx, taskIDs)
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
        SELECT id, project_id, title, description, comments, status, priority, due_at, created_at, updated_at
        FROM tasks t
        JOIN task_assignees ta ON ta.task_id = t.id
        WHERE ta.user_id = $1
        ORDER BY t.created_at DESC;
    `

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, fmt.Errorf("get my tasks: %w", err)
	}
	defer func() {
		_ = rows.Close()
	}()

	tasks := make([]models.Task, 0)
	taskIDs := make([]int64, 0)

	for rows.Next() {
		var (
			t             models.Task
			descField     sql.NullString
			commentsField sql.NullString
			dueField      sql.NullTime
		)

		err = rows.Scan(
			&t.ID, &t.ProjectID, &t.Title, &descField, &commentsField,
			&t.Status, &t.Priority, &dueField,
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

	assigneesMap, err := s.GetAssigneesForTasks(ctx, taskIDs)
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
