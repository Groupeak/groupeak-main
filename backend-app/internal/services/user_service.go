package services

import (
	"context"
	"database/sql"
	"fmt"
	"groupeak/internal/models"
	"strings"
	"time"
)

func (s *AuthService) GetProfile(ctx context.Context, userID int64) (*models.User, error) {
	var (
		user           models.User
		avatarField    sql.NullString
		birthDateField sql.NullTime
		aboutField     sql.NullString
	)

	err := s.db.QueryRowContext(
		ctx,
		`SELECT id, full_name, avatar_url, email, position, birth_date, about, created_at, updated_at
		 FROM users
		 WHERE id = $1`,
		userID,
	).Scan(
		&user.ID,
		&user.FullName,
		&avatarField,
		&user.Email,
		&user.Position,
		&birthDateField,
		&aboutField,
		&user.CreatedAt,
		&user.UpdatedAt,
	)

	if err == sql.ErrNoRows {
		return nil, ErrUserNotFound
	}
	if err != nil {
		return nil, fmt.Errorf("get profile: %w", err)
	}

	if avatarField.Valid {
		user.AvatarURL = &avatarField.String
	}
	if birthDateField.Valid {
		bd := birthDateField.Time.Format("02.01.2006")
		user.BirthDate = &bd
	}
	if aboutField.Valid {
		about := aboutField.String
		user.About = &about
	}

	return &user, nil
}

func (s *AuthService) UpdateProfile(ctx context.Context, userID int64, req models.UpdateProfileRequest) (*models.UpdateProfileResponse, error) {
	_, _, err := s.getUserByIDWithPassword(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("get user by ID: %w", err)
	}

	var (
		setClauses []string
		args       []interface{}
		argIndex   = 1
	)

	if req.FullName != nil {
		fullName := strings.TrimSpace(*req.FullName)
		if fullName == "" {
			return nil, ErrMissingFields
		}
		setClauses = append(setClauses, fmt.Sprintf("full_name = $%d", argIndex))
		args = append(args, fullName)
		argIndex++
	}

	if req.Position != nil {
		p := strings.TrimSpace(*req.Position)

		setClauses = append(setClauses, fmt.Sprintf("position = $%d", argIndex))
		if p == "" {
			args = append(args, nil)
		} else {
			if !ValidatePosition(&p) {
				return nil, ErrInvalidPosition
			}
			args = append(args, p)
		}
		argIndex++
	}

	if req.AvatarURL != nil {
		setClauses = append(setClauses, fmt.Sprintf("avatar_url = $%d", argIndex))
		if *req.AvatarURL == "" {
			args = append(args, nil)
		} else {
			args = append(args, *req.AvatarURL)
		}
		argIndex++
	}

	if req.BirthDate != nil {
		bdStr := strings.TrimSpace(*req.BirthDate)

		if bdStr == "" {
			setClauses = append(setClauses, fmt.Sprintf("birth_date = $%d", argIndex))
			args = append(args, nil)
			argIndex++
		} else {
			parsed, err := time.Parse("02.01.2006", bdStr)
			if err != nil {
				return nil, ErrInvalidBirthDate
			}
			if err := ValidateBirthDateAge(parsed); err != nil {
				return nil, err
			}

			setClauses = append(setClauses, fmt.Sprintf("birth_date = $%d", argIndex))
			args = append(args, parsed)
			argIndex++
		}
	}

	if req.About != nil {
		setClauses = append(setClauses, fmt.Sprintf("about = $%d", argIndex))
		if *req.About == "" {
			args = append(args, nil)
		} else {
			about := strings.TrimSpace(*req.About)
			args = append(args, about)
		}
		argIndex++
	}

	if len(setClauses) == 0 {
		return nil, ErrNothingToUpdate
	}

	setClauses = append(setClauses, fmt.Sprintf("updated_at = $%d", argIndex))
	args = append(args, time.Now())
	argIndex++

	args = append(args, userID)

	query := fmt.Sprintf(
		"UPDATE users SET %s WHERE id = $%d RETURNING id, full_name, avatar_url, email, position, birth_date, about, created_at, updated_at",
		strings.Join(setClauses, ", "),
		argIndex,
	)

	var (
		updatedUser    models.User
		avatarField    sql.NullString
		positionField  sql.NullString
		birthDateField sql.NullTime
		aboutField     sql.NullString
	)

	err = s.db.QueryRowContext(ctx, query, args...).Scan(
		&updatedUser.ID,
		&updatedUser.FullName,
		&avatarField,
		&updatedUser.Email,
		&positionField,
		&birthDateField,
		&aboutField,
		&updatedUser.CreatedAt,
		&updatedUser.UpdatedAt,
	)
	if err != nil {
		return nil, fmt.Errorf("update user: %w", err)
	}

	if avatarField.Valid {
		updatedUser.AvatarURL = &avatarField.String
	}

	if positionField.Valid {
		p := positionField.String
		updatedUser.Position = &p
	} else {
		updatedUser.Position = nil
	}

	if birthDateField.Valid {
		bd := birthDateField.Time.Format("02.01.2006")
		updatedUser.BirthDate = &bd
	}

	if aboutField.Valid {
		about := aboutField.String
		updatedUser.About = &about
	}

	newToken, err := s.generateToken(updatedUser)
	if err != nil {
		return nil, fmt.Errorf("generate token: %w", err)
	}

	return &models.UpdateProfileResponse{
		User:  updatedUser,
		Token: newToken,
	}, nil
}

func (s *AuthService) GetTaskStats(ctx context.Context, userID int64) (models.TaskStats, error) {
	const q = `
        SELECT
            COUNT(*) FILTER (WHERE t.status = 'done') AS done,
            COUNT(*) FILTER (
                WHERE t.status <> 'done'
                  AND t.due_at IS NOT NULL
                  AND t.due_at < NOW()
            ) AS overdue
        FROM tasks t
        JOIN task_assignees ta ON ta.task_id = t.id
        WHERE ta.user_id = $1
    `
	var st models.TaskStats
	err := s.db.QueryRowContext(ctx, q, userID).Scan(&st.Done, &st.Overdue)
	if err != nil {
		return models.TaskStats{}, err
	}
	return st, nil
}
