package services

import (
	"context"
	"database/sql"
	"fmt"
	"groupeak/internal/models"
	"strings"
)

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
