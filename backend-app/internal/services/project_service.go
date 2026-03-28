package services

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"groupeak/internal/models"
	"strings"
)

var (
	ErrProjectNotFound       = errors.New("project not found")
	ErrNotProjectOwner       = errors.New("user is not project owner")
	ErrNotProjectMember      = errors.New("user is not a member of this project")
	ErrAlreadyProjectMember  = errors.New("user is already project member")
	ErrInviteNotFound        = errors.New("invite not found")
	ErrInviteAlreadyUsed     = errors.New("invite is already used or revoked")
	ErrInviteAlreadyPending  = errors.New("invite already pending for this email")
	ErrInviteEmailMismatch   = errors.New("invite email does not match user email")
	ErrInviteTokenRequired   = errors.New("invite token is required")
	ErrProjectNameIsRequired = errors.New("project name is required")
	ErrCannotRemoveOwner     = errors.New("cannot remove project owner")
)

type ProjectService struct {
	db *sql.DB
}

func NewProjectService(db *sql.DB) *ProjectService {
	return &ProjectService{
		db: db,
	}
}

// CreateProject создаёт проект и сразу добавляет owner'а в project_members
func (s *ProjectService) CreateProject(ctx context.Context, ownerID int64, req models.CreateProjectRequest) (*models.Project, error) {
	name := strings.TrimSpace(req.Name)
	if name == "" {
		return nil, ErrProjectNameIsRequired
	}

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return nil, err
	}
	defer func() {
		if err != nil {
			_ = tx.Rollback()
		}
	}()

	var (
		project       models.Project
		descField     sql.NullString
		deadlineField sql.NullTime
	)

	var descParam interface{}
	if req.Description != nil && strings.TrimSpace(*req.Description) != "" {
		descParam = strings.TrimSpace(*req.Description)
	} else {
		descParam = nil
	}

	var deadlineParam interface{}
	if req.DeadlineAt != nil {
		deadlineParam = *req.DeadlineAt
	} else {
		deadlineParam = nil
	}

	query := `
		INSERT INTO projects (owner_id, name, description, deadline_at)
		VALUES ($1, $2, $3, $4)
		RETURNING id, owner_id, name, description, deadline_at, created_at, updated_at;
	`

	err = tx.QueryRowContext(
		ctx,
		query,
		ownerID,
		name,
		descParam,
		deadlineParam,
	).Scan(
		&project.ID,
		&project.OwnerID,
		&project.Name,
		&descField,
		&deadlineField,
		&project.CreatedAt,
		&project.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	if descField.Valid {
		value := descField.String
		project.Description = &value
	}

	if deadlineField.Valid {
		value := deadlineField.Time
		project.DeadlineAt = &value
	}

	_, err = tx.ExecContext(
		ctx,
		`INSERT INTO project_members (project_id, user_id, role)
		 VALUES ($1, $2, 'owner');`,
		project.ID,
		ownerID,
	)
	if err != nil {
		return nil, err
	}

	err = tx.Commit()
	if err != nil {
		return nil, err
	}

	return &project, nil
}

// UpdateProject — частичное обновление проекта
func (s *ProjectService) UpdateProject(ctx context.Context, ownerID, projectID int64, req models.PatchProjectRequest) (*models.Project, error) {
	isOwner, err := s.isProjectOwner(ctx, projectID, ownerID)
	if err != nil {
		return nil, err
	}
	if !isOwner {
		return nil, ErrNotProjectOwner
	}

	namePtr := req.Name
	if namePtr == nil {
		namePtr = req.ProjectName
	}

	setParts := make([]string, 0, 6)
	args := make([]interface{}, 0, 6)
	idx := 1
	changed := false

	if namePtr != nil {
		name := strings.TrimSpace(*namePtr)
		if name == "" {
			return nil, ErrProjectNameIsRequired
		}
		setParts = append(setParts, fmt.Sprintf("name = $%d", idx))
		args = append(args, name)
		idx++
		changed = true
	}

	if req.Description != nil {
		d := strings.TrimSpace(*req.Description)
		if d == "" {
			setParts = append(setParts, "description = NULL")
		} else {
			setParts = append(setParts, fmt.Sprintf("description = $%d", idx))
			args = append(args, d)
			idx++
		}
		changed = true
	}

	if req.ClearDeadlineAt {
		setParts = append(setParts, "deadline_at = NULL")
		changed = true
	} else if req.DeadlineAt != nil {
		setParts = append(setParts, fmt.Sprintf("deadline_at = $%d", idx))
		args = append(args, *req.DeadlineAt)
		idx++
		changed = true
	}

	if !changed {
		return nil, ErrNoFieldsToUpdate
	}

	setParts = append(setParts, "updated_at = NOW()")

	args = append(args, projectID)
	query := fmt.Sprintf(`
		UPDATE projects
		SET %s
		WHERE id = $%d
		RETURNING id, owner_id, name, description, deadline_at, created_at, updated_at;
	`, strings.Join(setParts, ", "), idx)

	var (
		p             models.Project
		descField     sql.NullString
		deadlineField sql.NullTime
	)

	err = s.db.QueryRowContext(ctx, query, args...).Scan(
		&p.ID,
		&p.OwnerID,
		&p.Name,
		&descField,
		&deadlineField,
		&p.CreatedAt,
		&p.UpdatedAt,
	)
	if err != nil {
		return nil, fmt.Errorf("update project: %w", err)
	}

	if descField.Valid {
		v := descField.String
		p.Description = &v
	}
	if deadlineField.Valid {
		v := deadlineField.Time
		p.DeadlineAt = &v
	}

	return &p, nil
}

// ListUserProjects возвращает все проекты, где юзер — owner или member
func (s *ProjectService) ListUserProjects(ctx context.Context, userID int64) ([]models.Project, error) {
	query := `
		SELECT DISTINCT
			p.id,
			p.owner_id,
			p.name,
			p.description,
			p.deadline_at,
			p.created_at,
			p.updated_at
		FROM projects p
		LEFT JOIN project_members pm ON pm.project_id = p.id
		WHERE p.owner_id = $1 OR pm.user_id = $1
		ORDER BY p.created_at DESC;
	`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var projects []models.Project

	for rows.Next() {
		var (
			project       models.Project
			descField     sql.NullString
			deadlineField sql.NullTime
		)

		err = rows.Scan(
			&project.ID,
			&project.OwnerID,
			&project.Name,
			&descField,
			&deadlineField,
			&project.CreatedAt,
			&project.UpdatedAt,
		)
		if err != nil {
			return nil, err
		}

		if descField.Valid {
			value := descField.String
			project.Description = &value
		}

		if deadlineField.Valid {
			value := deadlineField.Time
			project.DeadlineAt = &value
		}

		projects = append(projects, project)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return projects, nil
}

func (s *ProjectService) isProjectOwner(ctx context.Context, projectID, userID int64) (bool, error) {
	var ownerID int64
	err := s.db.QueryRowContext(
		ctx,
		`SELECT owner_id FROM projects WHERE id = $1;`,
		projectID,
	).Scan(&ownerID)
	if err != nil {
		if err == sql.ErrNoRows {
			return false, ErrProjectNotFound
		}
		return false, err
	}

	return ownerID == userID, nil
}

func (s *ProjectService) isProjectMember(ctx context.Context, projectID, userID int64) (bool, error) {
	var exists bool
	err := s.db.QueryRowContext(
		ctx,
		`
		SELECT EXISTS(
			SELECT 1
			FROM project_members
			WHERE project_id = $1 AND user_id = $2
		);
		`,
		projectID,
		userID,
	).Scan(&exists)
	if err != nil {
		return false, err
	}

	return exists, nil
}

func (s *ProjectService) ensureProjectExists(ctx context.Context, projectID int64) error {
	var exists bool
	err := s.db.QueryRowContext(
		ctx,
		`SELECT EXISTS(SELECT 1 FROM projects WHERE id = $1);`,
		projectID,
	).Scan(&exists)
	if err != nil {
		return err
	}
	if !exists {
		return ErrProjectNotFound
	}
	return nil
}

// DeleteProject удаляет проект. Благодаря ON DELETE CASCADE в БД
func (s *ProjectService) DeleteProject(ctx context.Context, ownerID, projectID int64) error {
	isOwner, err := s.isProjectOwner(ctx, projectID, ownerID)
	if err != nil {
		return err
	}
	if !isOwner {
		return ErrNotProjectOwner
	}

	_, err = s.db.ExecContext(ctx, `DELETE FROM projects WHERE id = $1`, projectID)
	if err != nil {
		return fmt.Errorf("delete project: %w", err)
	}

	return nil
}

func (s *ProjectService) GetProjectDetails(ctx context.Context, userID, projectID int64) (*models.ProjectWithStats, error) {
	if err := s.ensureProjectExists(ctx, projectID); err != nil {
		return nil, err
	}
	if isMember, err := s.isProjectMember(ctx, projectID, userID); err != nil || !isMember {
		if err != nil {
			return nil, err
		}
		return nil, ErrNotProjectMember
	}

	query := `
		SELECT 
			p.id, p.owner_id, p.name, p.description, p.deadline_at,
			p.created_at, p.updated_at,
			(SELECT COUNT(*) FROM project_members WHERE project_id = p.id) AS member_count,
			(SELECT COUNT(*) FROM tasks WHERE project_id = p.id) AS task_count
		FROM projects p 
		WHERE p.id = $1;
	`

	var (
		stats         models.ProjectWithStats
		descField     sql.NullString
		deadlineField sql.NullTime
	)

	err := s.db.QueryRowContext(ctx, query, projectID).Scan(
		&stats.ID,
		&stats.OwnerID,
		&stats.Name,
		&descField,
		&deadlineField,
		&stats.CreatedAt,
		&stats.UpdatedAt,
		&stats.MemberCount,
		&stats.TaskCount,
	)
	if err == sql.ErrNoRows {
		return nil, ErrProjectNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("get project details: %w", err)
	}

	if descField.Valid {
		v := descField.String
		stats.Description = &v
	}
	if deadlineField.Valid {
		v := deadlineField.Time
		stats.DeadlineAt = &v
	}

	return &stats, nil
}

// RemoveMember удаляет участника из проекта
func (s *ProjectService) RemoveMember(ctx context.Context, requesterID, projectID, targetUserID int64) error {
	var ownerID int64
	err := s.db.QueryRowContext(ctx, `SELECT owner_id FROM projects WHERE id = $1`, projectID).Scan(&ownerID)
	if err == sql.ErrNoRows {
		return ErrProjectNotFound
	}
	if err != nil {
		return fmt.Errorf("query project owner: %w", err)
	}

	isOwner := (requesterID == ownerID)
	isSelf := (requesterID == targetUserID)

	if !isOwner && !isSelf {
		return ErrForbidden
	}

	if targetUserID == ownerID {
		return ErrCannotRemoveOwner
	}

	var exists bool
	err = s.db.QueryRowContext(ctx, `SELECT EXISTS(SELECT 1 FROM project_members WHERE project_id = $1 AND user_id = $2)`, projectID, targetUserID).Scan(&exists)
	if err != nil {
		return fmt.Errorf("check member exists: %w", err)
	}
	if !exists {
		return ErrNotProjectMember
	}

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return fmt.Errorf("begin tx: %w", err)
	}
	defer func() { _ = tx.Rollback() }()

	_, err = tx.ExecContext(ctx, `DELETE FROM project_members WHERE project_id = $1 AND user_id = $2`, projectID, targetUserID)
	if err != nil {
		return fmt.Errorf("delete project member: %w", err)
	}

	_, err = tx.ExecContext(ctx, `
		DELETE FROM task_assignees 
		WHERE user_id = $1 
		  AND task_id IN (SELECT id FROM tasks WHERE project_id = $2)
	`, targetUserID, projectID)
	if err != nil {
		return fmt.Errorf("delete member from task_assignees: %w", err)
	}

	if err := tx.Commit(); err != nil {
		return fmt.Errorf("commit: %w", err)
	}

	return nil
}
