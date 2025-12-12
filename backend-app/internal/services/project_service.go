package services

import (
	"context"
	"crypto/rand"
	"database/sql"
	"encoding/hex"
	"errors"
	"fmt"
	"groupeak/internal/models"
	"strings"
)

var (
	ErrProjectNotFound = errors.New("project not found")
	ErrNotProjectOwner = errors.New("user is not project owner")
	ErrInviteAlreadyPending = errors.New("invite already pending for this email")
	ErrAlreadyProjectMember = errors.New("user is already project member")
)

type ProjectService struct {
	db *sql.DB
}

func NewProjectService(db *sql.DB) *ProjectService {
	return &ProjectService{
		db: db,
	}
}

func generateInviteToken() (string, error) {
	buf := make([]byte, 16)
	if _, err := rand.Read(buf); err != nil {
		return "", fmt.Errorf("rand.Read: %w", err)
	}
	return hex.EncodeToString(buf), nil
}

func (s *ProjectService) CreateProject(ctx context.Context, ownerID int64, req models.CreateProjectRequest) (*models.Project, error) {
	name := strings.TrimSpace(req.Name)
	if name == "" {
		return nil, errors.New("project name is required")
	}

	var (
		descParam interface{}
		deadlineParam interface{}
	)

	if req.Description != nil && strings.TrimSpace(*req.Description) != "" {
		descParam = strings.TrimSpace(*req.Description)
	} else {
		descParam = nil
	}

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

	var (
		p models.Project
		descField sql.NullString
		deadlineField sql.NullTime
	)

	err := s.db.QueryRowContext(ctx, query, ownerID, name, descParam, deadlineParam).Scan(
		&p.ID,
		&p.OwnerID,
		&p.Name,
		&descField,
		&deadlineField,
		&p.CreatedAt,
		&p.UpdatedAt,
	)
	if err != nil {
		return nil, fmt.Errorf("insert project: %w", err)
	}

	if descField.Valid {
		desc := descField.String
		p.Description = &desc
	}
	if deadlineField.Valid {
		dt := deadlineField.Time
		p.DeadlineAt = &dt
	}

	_, err = s.db.ExecContext(ctx,
		`INSERT INTO project_members (project_id, user_id, role)
		 VALUES ($1, $2, 'owner')
		 ON CONFLICT (project_id, user_id) DO NOTHING;`,
		p.ID, ownerID,
	)
	if err != nil {
		return nil, fmt.Errorf("insert project owner to project_members: %w", err)
	}

	return &p, nil
}

func (s *ProjectService) ListUserProjects(ctx context.Context, userID int64) ([]models.ProjectListItem, error) {
	query := `
		SELECT
			p.id,
			p.owner_id,
			p.name,
			p.description,
			p.deadline_at,
			p.created_at,
			p.updated_at,
			pm.role
		FROM projects p
		JOIN project_members pm ON pm.project_id = p.id
		WHERE pm.user_id = $1
		ORDER BY p.created_at DESC;
	`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, fmt.Errorf("query projects: %w", err)
	}
	defer rows.Close()

	var result []models.ProjectListItem
	for rows.Next() {
		var (
			item models.ProjectListItem
			descField sql.NullString
			deadlineField sql.NullTime
		)

		err := rows.Scan(
			&item.ID,
			&item.OwnerID,
			&item.Name,
			&descField,
			&deadlineField,
			&item.CreatedAt,
			&item.UpdatedAt,
			&item.Role,
		)
		if err != nil {
			return nil, fmt.Errorf("scan project: %w", err)
		}

		if descField.Valid {
			desc := descField.String
			item.Description = &desc
		}
		if deadlineField.Valid {
			dt := deadlineField.Time
			item.DeadlineAt = &dt
		}

		result = append(result, item)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows err: %w", err)
	}

	return result, nil
}

func (s *ProjectService) InviteMember(ctx context.Context, ownerID, projectID int64, req models.InviteMemberRequest) (*models.ProjectInvite, error) {
	email, err := normalizeAndValidateEmail(req.Email)
	if err != nil {
		return nil, err
	}

	var dbOwnerID int64
	err = s.db.QueryRowContext(ctx, `SELECT owner_id FROM projects WHERE id = $1`, projectID).Scan(&dbOwnerID)
	if err == sql.ErrNoRows {
		return nil, ErrProjectNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("query project owner: %w", err)
	}
	if dbOwnerID != ownerID {
		return nil, ErrNotProjectOwner
	}

	var alreadyMember bool
	err = s.db.QueryRowContext(ctx,
		`SELECT EXISTS (
			SELECT 1 FROM project_members pm
			JOIN users u ON u.id = pm.user_id
			WHERE pm.project_id = $1 AND u.email = $2
		)`, projectID, email,
	).Scan(&alreadyMember)
	if err != nil {
		return nil, fmt.Errorf("check existing member: %w", err)
	}
	if alreadyMember {
		return nil, ErrAlreadyProjectMember
	}

	var pendingExists bool
	err = s.db.QueryRowContext(ctx,
		`SELECT EXISTS (
			SELECT 1 FROM project_invites
			WHERE project_id = $1 AND email = $2 AND status = 'pending'
		)`, projectID, email,
	).Scan(&pendingExists)
	if err != nil {
		return nil, fmt.Errorf("check existing invite: %w", err)
	}
	if pendingExists {
		return nil, ErrInviteAlreadyPending
	}

	token, err := generateInviteToken()
	if err != nil {
		return nil, err
	}

	query := `
		INSERT INTO project_invites (project_id, email, token, status)
		VALUES ($1, $2, $3, 'pending')
		RETURNING id, project_id, email, token, status, created_at, updated_at;
	`

	var invite models.ProjectInvite
	err = s.db.QueryRowContext(ctx, query, projectID, email, token).Scan(
		&invite.ID,
		&invite.ProjectID,
		&invite.Email,
		&invite.Token,
		&invite.Status,
		&invite.CreatedAt,
		&invite.UpdatedAt,
	)
	if err != nil {
		return nil, fmt.Errorf("insert project invite: %w", err)
	}

	// TODO: отправка email
	return &invite, nil
}