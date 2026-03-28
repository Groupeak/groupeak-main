package services

import (
	"context"
	"database/sql"
	"fmt"
	"groupeak/internal/models"
	"time"
)

// TakeTask — добавляем пользователя в task_assignees и меняем статус
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
		`SELECT EXISTS (SELECT 1 FROM project_members WHERE project_id = $1 AND user_id = $2)`,
		projectID, userID,
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

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return nil, err
	}
	defer func() { _ = tx.Rollback() }()

	_, err = tx.ExecContext(
		ctx,
		`INSERT INTO task_assignees (task_id, user_id) VALUES ($1, $2) ON CONFLICT DO NOTHING`,
		taskID, userID,
	)
	if err != nil {
		return nil, fmt.Errorf("insert task assignee: %w", err)
	}

	_, err = tx.ExecContext(
		ctx,
		`UPDATE tasks SET status = 'in_progress', updated_at = NOW() WHERE id = $1`,
		taskID,
	)
	if err != nil {
		return nil, fmt.Errorf("update task: %w", err)
	}

	if err = tx.Commit(); err != nil {
		return nil, err
	}

	return s.getTaskByID(ctx, taskID)
}

func (s *TaskService) CompleteTask(ctx context.Context, userID, taskID int64) (*models.Task, error) {
	var status string

	err := s.db.QueryRowContext(
		ctx,
		`SELECT status FROM tasks WHERE id = $1`,
		taskID,
	).Scan(&status)

	if err == sql.ErrNoRows {
		return nil, ErrTaskNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("query task before complete: %w", err)
	}

	if status != "in_progress" {
		return nil, ErrInvalidTaskStatus
	}

	var isAssignee bool
	err = s.db.QueryRowContext(
		ctx,
		`SELECT EXISTS(SELECT 1 FROM task_assignees WHERE task_id = $1 AND user_id = $2)`,
		taskID, userID,
	).Scan(&isAssignee)
	if err != nil {
		return nil, fmt.Errorf("check assignee: %w", err)
	}

	if !isAssignee {
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
	ok, err := s.IsProjectOwner(ctx, projectID, ownerID)
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
