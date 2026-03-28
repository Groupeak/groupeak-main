package services

import (
	"context"
	"crypto/rand"
	"database/sql"
	"encoding/hex"
	"errors"
	"fmt"
	"groupeak/internal/models"
	"strconv"
	"strings"
	"time"

	"golang.org/x/crypto/bcrypt"
)

const (
	minAgeYears = 8
	maxAgeYears = 100
)

func CalcAgeYears(birthDate, now time.Time) int {
	age := now.Year() - birthDate.Year()
	if now.Month() < birthDate.Month() || (now.Month() == birthDate.Month() && now.Day() < birthDate.Day()) {
		age--
	}
	return age
}

func ValidateBirthDateAge(birthDate time.Time) error {
	now := time.Now()
	age := CalcAgeYears(birthDate, now)

	if age < minAgeYears || age > maxAgeYears {
		return ErrBirthDateOutOfRange
	}
	return nil
}

// ensureProjectExists — чтобы отличать "проекта нет" от "ты не участник"
func (s *TaskService) EnsureProjectExists(ctx context.Context, projectID int64) error {
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
func (s *TaskService) EnsureProjectMember(ctx context.Context, projectID, userID int64) error {
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

// IsProjectOwner проверяет, является ли пользователь владельцем проекта
func (s *TaskService) IsProjectOwner(ctx context.Context, projectID, userID int64) (bool, error) {
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

func (s *TaskService) EnsureUsersExist(ctx context.Context, ids []int64) error {
	ph := makePlaceholders(1, len(ids))
	query := `SELECT id FROM users WHERE id IN (` + ph + `);`

	args := make([]interface{}, 0, len(ids))
	for _, id := range ids {
		args = append(args, id)
	}

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return fmt.Errorf("EnsureUsersExist: %w", err)
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

func (s *TaskService) EnsureUsersAreProjectMembers(ctx context.Context, projectID int64, ids []int64) error {
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
		return fmt.Errorf("EnsureUsersAreProjectMembers: %w", err)
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

// GetAssigneesForTasks — 1 запросом вытягиваем исполнителей для набора taskID
func (s *TaskService) GetAssigneesForTasks(ctx context.Context, taskIDs []int64) (map[int64][]int64, error) {
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

func (s *TaskService) GetTaskAssigneeIDs(ctx context.Context, taskID int64) ([]int64, error) {
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

// userHasProjectAccess проверяет, имеет ли пользователь доступ хотя бы к одному проекту
func (s *TaskService) UserHasProjectAccess(ctx context.Context, userID int64) (bool, error) {
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

func ValidatePosition(position *string) bool {
	if position == nil {
		return true
	}

	val := strings.TrimSpace(*position)

	switch val {
	case "Frontend", "Backend", "Designer", "QA", "DevOps", "Android", "iOS",
		"Data Analyst", "Product", "HR":
		return true
	default:
		return false
	}
}

func HashPassword(plain string) (string, error) {
	if plain == "" {
		return "", errors.New("password is empty")
	}
	hash, err := bcrypt.GenerateFromPassword([]byte(plain), bcrypt.DefaultCost)
	if err != nil {
		return "", fmt.Errorf("bcrypt.GenerateFromPassword: %w", err)
	}
	return string(hash), nil
}

func generateInviteToken() (string, error) {
	buf := make([]byte, 16)
	_, err := rand.Read(buf)
	if err != nil {
		return "", err
	}
	return hex.EncodeToString(buf), nil
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

func NormalizeAssigneeIDs(ids []int) ([]int64, error) {
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

func NormalizeAssigneeIDs64(ids []int64) ([]int64, error) {
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

func (s *TaskService) ValidateTaskCategory(category models.TaskCategory) error {
	switch category {
	case models.TaskCategoryCurrent, models.TaskCategoryReview, models.TaskCategoryInactive:
		return nil
	default:
		return ErrInvalidTaskCategory
	}
}
