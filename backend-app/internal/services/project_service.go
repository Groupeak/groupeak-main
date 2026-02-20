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
	ErrProjectNotFound      = errors.New("project not found")
	ErrNotProjectOwner      = errors.New("user is not project owner")
	ErrNotProjectMember     = errors.New("user is not a member of this project")
	ErrAlreadyProjectMember = errors.New("user is already project member")

	ErrInviteNotFound        = errors.New("invite not found")
	ErrInviteAlreadyUsed     = errors.New("invite is already used or revoked")
	ErrInviteAlreadyPending  = errors.New("invite already pending for this email")
	ErrInviteEmailMismatch   = errors.New("invite email does not match user email")
	ErrInviteTokenRequired   = errors.New("invite token is required")
	ErrProjectNameIsRequired = errors.New("project name is required")
	ErrNoFieldsToUpdate      = errors.New("no fields to update")
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

// InviteMember создаёт инвайт по email
func (s *ProjectService) InviteMember(ctx context.Context, ownerID, projectID int64, req models.InviteMemberRequest) (*models.ProjectInvite, error) {
	isOwner, err := s.isProjectOwner(ctx, projectID, ownerID)
	if err != nil {
		return nil, err
	}
	if !isOwner {
		return nil, ErrNotProjectOwner
	}

	email, err := normalizeAndValidateEmail(req.Email)
	if err != nil {
		return nil, err
	}

	var alreadyMember bool
	err = s.db.QueryRowContext(
		ctx,
		`
		SELECT EXISTS(
			SELECT 1
			FROM project_members pm
			JOIN users u ON u.id = pm.user_id
			WHERE pm.project_id = $1 AND u.email = $2
		);
		`,
		projectID,
		email,
	).Scan(&alreadyMember)
	if err != nil {
		return nil, err
	}
	if alreadyMember {
		return nil, ErrAlreadyProjectMember
	}

	var existingInviteID int64
	err = s.db.QueryRowContext(
		ctx,
		`
		SELECT id
		FROM project_invites
		WHERE project_id = $1 AND email = $2 AND status = 'pending';
		`,
		projectID,
		email,
	).Scan(&existingInviteID)
	if err != nil && err != sql.ErrNoRows {
		return nil, err
	}
	if err == nil {
		return nil, ErrInviteAlreadyPending
	}

	token, err := generateInviteToken()
	if err != nil {
		return nil, fmt.Errorf("generate invite token: %w", err)
	}

	var invite models.ProjectInvite

	err = s.db.QueryRowContext(
		ctx,
		`
		INSERT INTO project_invites (project_id, email, token, status)
		VALUES ($1, $2, $3, 'pending')
		RETURNING id, project_id, email, token, status, created_at, updated_at;
		`,
		projectID,
		email,
		token,
	).Scan(
		&invite.ID,
		&invite.ProjectID,
		&invite.Email,
		&invite.Token,
		&invite.Status,
		&invite.CreatedAt,
		&invite.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	return &invite, nil
}

// ListProjectMembers возвращает список участников проекта с данными пользователя
func (s *ProjectService) ListProjectMembers(ctx context.Context, userID, projectID int64) ([]models.ProjectMemberWithUser, error) {
	err := s.ensureProjectExists(ctx, projectID)
	if err != nil {
		return nil, err
	}

	isMember, err := s.isProjectMember(ctx, projectID, userID)
	if err != nil {
		return nil, err
	}
	if !isMember {
		return nil, ErrNotProjectMember
	}

	query := `
		SELECT
			pm.id,
			pm.project_id,
			pm.user_id,
			pm.role,
			pm.created_at,
			COALESCE(u.full_name, '') as full_name,
			u.email,
			COALESCE(u.position, '') as position,
			u.avatar_url
		FROM project_members pm
		JOIN users u ON u.id = pm.user_id
		WHERE pm.project_id = $1
		ORDER BY
			CASE pm.role WHEN 'owner' THEN 0 ELSE 1 END,
			COALESCE(u.full_name, '');
	`

	rows, err := s.db.QueryContext(ctx, query, projectID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var members []models.ProjectMemberWithUser

	for rows.Next() {
		var (
			member      models.ProjectMemberWithUser
			avatarField sql.NullString
		)

		err = rows.Scan(
			&member.ID,
			&member.ProjectID,
			&member.UserID,
			&member.Role,
			&member.CreatedAt,
			&member.FullName,
			&member.Email,
			&member.Position,
			&avatarField,
		)
		if err != nil {
			return nil, err
		}

		if avatarField.Valid {
			value := avatarField.String
			member.AvatarURL = &value
		}

		members = append(members, member)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return members, nil
}

// AcceptInvite по токену добавляет юзера в project_members и помечает инвайт как accepted
func (s *ProjectService) AcceptInvite(ctx context.Context, userID int64, token string) (*models.ProjectInvite, error) {
	token = strings.TrimSpace(token)
	if token == "" {
		return nil, ErrInviteTokenRequired
	}

	var userEmail string
	err := s.db.QueryRowContext(
		ctx,
		`SELECT email FROM users WHERE id = $1;`,
		userID,
	).Scan(&userEmail)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, fmt.Errorf("user not found")
		}
		return nil, err
	}

	var invite models.ProjectInvite

	err = s.db.QueryRowContext(
		ctx,
		`
		SELECT id, project_id, email, token, status, created_at, updated_at
		FROM project_invites
		WHERE token = $1;
		`,
		token,
	).Scan(
		&invite.ID,
		&invite.ProjectID,
		&invite.Email,
		&invite.Token,
		&invite.Status,
		&invite.CreatedAt,
		&invite.UpdatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, ErrInviteNotFound
		}
		return nil, err
	}

	if invite.Status != "pending" {
		return nil, ErrInviteAlreadyUsed
	}

	if !strings.EqualFold(invite.Email, userEmail) {
		return nil, ErrInviteEmailMismatch
	}

	var alreadyMember bool
	err = s.db.QueryRowContext(
		ctx,
		`
		SELECT EXISTS(
			SELECT 1
			FROM project_members
			WHERE project_id = $1 AND user_id = $2
		);
		`,
		invite.ProjectID,
		userID,
	).Scan(&alreadyMember)
	if err != nil {
		return nil, err
	}
	if alreadyMember {
		return nil, ErrAlreadyProjectMember
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

	_, err = tx.ExecContext(
		ctx,
		`
		INSERT INTO project_members (project_id, user_id, role)
		VALUES ($1, $2, 'member');
		`,
		invite.ProjectID,
		userID,
	)
	if err != nil {
		return nil, err
	}

	err = tx.QueryRowContext(
		ctx,
		`
		UPDATE project_invites
		SET status = 'accepted', updated_at = NOW()
		WHERE id = $1
		RETURNING id, project_id, email, token, status, created_at, updated_at;
		`,
		invite.ID,
	).Scan(
		&invite.ID,
		&invite.ProjectID,
		&invite.Email,
		&invite.Token,
		&invite.Status,
		&invite.CreatedAt,
		&invite.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	err = tx.Commit()
	if err != nil {
		return nil, err
	}

	return &invite, nil
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

func generateInviteToken() (string, error) {
	buf := make([]byte, 16)
	_, err := rand.Read(buf)
	if err != nil {
		return "", err
	}
	return hex.EncodeToString(buf), nil
}
