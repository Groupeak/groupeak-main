package services

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"groupeak/internal/models"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
)

var (
	ErrEmailAlreadyTaken      = errors.New("email already taken")
	ErrInvalidPosition        = errors.New("invalid position")
	ErrMissingFields          = errors.New("full_name, email and password are required")
	ErrWeakPassword           = errors.New("password must be at least 8 characters long")
	ErrInvalidEmail           = errors.New("invalid email format")
	ErrUnsupportedEmailDomain = errors.New("email domain is not supported")
	ErrInvalidCredentials     = errors.New("invalid credentials")
	ErrInvalidBirthDate       = errors.New("invalid birth date, expected format dd.mm.yyyy")
	ErrBirthDateOutOfRange    = errors.New("birth date must make age between 8 and 100 years")
	ErrUserNotFound           = errors.New("user not found")
	ErrPasswordDoNotMatch     = errors.New("confirm_new does not match new_password")
	ErrNewPasswordSameAsOld   = errors.New("new password must not match old password")
	ErrEmailSameAsCurrent     = errors.New("new email must not match current email")
	ErrPasswordIsRequired     = errors.New("password is required")
	ErrNewEmailIsRequired     = errors.New("new_email is required")
	ErrNothingToUpdate        = errors.New("nothing to update")
)

const (
	minAgeYears = 8
	maxAgeYears = 100
)

func calcAgeYears(birthDate, now time.Time) int {
	age := now.Year() - birthDate.Year()
	if now.Month() < birthDate.Month() || (now.Month() == birthDate.Month() && now.Day() < birthDate.Day()) {
		age--
	}
	return age
}

func validateBirthDateAge(birthDate time.Time) error {
	now := time.Now()
	age := calcAgeYears(birthDate, now)

	if age < minAgeYears || age > maxAgeYears {
		return ErrBirthDateOutOfRange
	}
	return nil
}

var allowedDomains = map[string]bool{
	"yandex.ru":      true,
	"yandex.com":     true,
	"ya.ru":          true,
	"mail.ru":        true,
	"inbox.ru":       true,
	"rambler.ru":     true,
	"gmail.com":      true,
	"googlemail.com": true,
	"outlook.com":    true,
	"icloud.com":     true,
	"yahoo.com":      true,
	"mail.com":       true,
	"sfedu.ru":       true,
}

type JWTClaims struct {
	UserID   int64  `json:"user_id"`
	Email    string `json:"email"`
	Position string `json:"position"`
	jwt.RegisteredClaims
}

type AuthService struct {
	db       *sql.DB
	jwtToken []byte
}

func NewAuthService(db *sql.DB, jwtToken string) *AuthService {
	return &AuthService{
		db:       db,
		jwtToken: []byte(jwtToken),
	}
}

func validatePosition(position *string) bool {
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

func normalizeAndValidateEmail(email string) (string, error) {
	email = strings.ToLower(strings.TrimSpace(email))
	if email == "" || len(email) < 5 {
		return "", ErrInvalidEmail
	}
	parts := strings.Split(email, "@")
	if len(parts) != 2 || parts[0] == "" || parts[1] == "" {
		return "", ErrInvalidEmail
	}
	domain := parts[1]
	if !allowedDomains[domain] {
		return "", ErrUnsupportedEmailDomain
	}
	return email, nil
}

func hashPassword(plain string) (string, error) {
	if plain == "" {
		return "", errors.New("password is empty")
	}
	hash, err := bcrypt.GenerateFromPassword([]byte(plain), bcrypt.DefaultCost)
	if err != nil {
		return "", fmt.Errorf("bcrypt.GenerateFromPassword: %w", err)
	}
	return string(hash), nil
}

func (s *AuthService) getUserByIDWithPassword(ctx context.Context, userID int64) (models.User, string, error) {
	var (
		user           models.User
		passwordHash   string
		avatarField    sql.NullString
		birthDateField sql.NullTime
		aboutField     sql.NullString
	)

	err := s.db.QueryRowContext(ctx, `
		SELECT
			id, full_name, avatar_url, email, position, birth_date, about, created_at, updated_at, password_hash
		FROM users
		WHERE id = $1
	`, userID).Scan(
		&user.ID,
		&user.FullName,
		&avatarField,
		&user.Email,
		&user.Position,
		&birthDateField,
		&aboutField,
		&user.CreatedAt,
		&user.UpdatedAt,
		&passwordHash,
	)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return models.User{}, "", ErrUserNotFound
		}
		return models.User{}, "", fmt.Errorf("select user by id: %w", err)
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

	return user, passwordHash, nil
}

func (s *AuthService) generateToken(user models.User) (string, error) {
	now := time.Now()

	position := ""
	if user.Position != nil {
		position = *user.Position
	}

	claims := JWTClaims{
		UserID:   user.ID,
		Email:    user.Email,
		Position: position,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject:   fmt.Sprint(user.ID),
			ExpiresAt: jwt.NewNumericDate(now.Add(24 * time.Hour)),
			IssuedAt:  jwt.NewNumericDate(now),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	signed, err := token.SignedString(s.jwtToken)
	if err != nil {
		return "", fmt.Errorf("token.SignedString: %w", err)
	}
	return signed, nil
}

func (s *AuthService) Register(ctx context.Context, req models.RegisterRequest) (*models.RegisterResponse, error) {
	req.FullName = strings.TrimSpace(req.FullName)

	if req.FullName == "" || req.Email == "" || req.Password == "" {
		return nil, ErrMissingFields
	}
	if len(req.Password) < 8 {
		return nil, ErrWeakPassword
	}

	var positionParam interface{}
	if req.Position != nil {
		p := strings.TrimSpace(*req.Position)

		if p == "" {
			positionParam = nil
			req.Position = nil
		} else {
			if !validatePosition(&p) {
				return nil, ErrInvalidPosition
			}
			req.Position = &p
			positionParam = p
		}
	} else {
		positionParam = nil
	}

	email, err := normalizeAndValidateEmail(req.Email)
	if err != nil {
		return nil, err
	}

	var birthDateParam interface{}

	if req.BirthDate != nil {
		bdStr := strings.TrimSpace(*req.BirthDate)

		if bdStr != "" {
			parsed, err := time.Parse("02.01.2006", bdStr)
			if err != nil {
				return nil, ErrInvalidBirthDate
			}
			if err := validateBirthDateAge(parsed); err != nil {
				return nil, err
			}

			birthDateParam = parsed
		} else {
			birthDateParam = nil
		}
	} else {
		birthDateParam = nil
	}

	var aboutParam interface{}
	if req.About != nil && strings.TrimSpace(*req.About) != "" {
		aboutParam = strings.TrimSpace(*req.About)
	} else {
		aboutParam = nil
	}

	var exists bool
	err = s.db.QueryRowContext(
		ctx,
		`SELECT EXISTS (SELECT 1 FROM users WHERE email = $1)`,
		email,
	).Scan(&exists)
	if err != nil {
		return nil, fmt.Errorf("check email exists: %w", err)
	}
	if exists {
		return nil, ErrEmailAlreadyTaken
	}

	passwordHash, err := hashPassword(req.Password)
	if err != nil {
		return nil, err
	}

	query := `
	INSERT INTO users (full_name, avatar_url, email, password_hash, position, birth_date, about)
	VALUES ($1, $2, $3, $4, $5, $6, $7)
	RETURNING id, full_name, avatar_url, email, position, birth_date, about, created_at, updated_at;
	`

	var (
		user           models.User
		avatarField    sql.NullString
		avatarParam    interface{}
		positionField  sql.NullString
		birthDateField sql.NullTime
		aboutField     sql.NullString
	)

	if req.AvatarURL != nil && strings.TrimSpace(*req.AvatarURL) != "" {
		avatarParam = strings.TrimSpace(*req.AvatarURL)
	} else {
		avatarParam = nil
	}

	err = s.db.QueryRowContext(
		ctx,
		query,
		req.FullName,
		avatarParam,
		email,
		passwordHash,
		positionParam,
		birthDateParam,
		aboutParam,
	).Scan(
		&user.ID,
		&user.FullName,
		&avatarField,
		&user.Email,
		&positionField,
		&birthDateField,
		&aboutField,
		&user.CreatedAt,
		&user.UpdatedAt,
	)
	if err != nil {
		return nil, fmt.Errorf("insert user: %w", err)
	}

	if avatarField.Valid {
		user.AvatarURL = &avatarField.String
	}

	if positionField.Valid {
		p := positionField.String
		user.Position = &p
	} else {
		user.Position = nil
	}

	if birthDateField.Valid {
		bd := birthDateField.Time.Format("02.01.2006")
		user.BirthDate = &bd
	}

	if aboutField.Valid {
		about := aboutField.String
		user.About = &about
	}

	token, err := s.generateToken(user)
	if err != nil {
		return nil, fmt.Errorf("generate token: %w", err)
	}

	resp := &models.RegisterResponse{
		User:  user,
		Token: token,
	}
	return resp, nil
}

func (s *AuthService) Login(ctx context.Context, req models.LoginRequest) (*models.LoginResponse, error) {
	email, err := normalizeAndValidateEmail(req.Email)
	if err != nil {
		return nil, err
	}

	var (
		user           models.User
		passwordHash   string
		avatarField    sql.NullString
		birthDateField sql.NullTime
		aboutField     sql.NullString
	)

	err = s.db.QueryRowContext(ctx,
		`SELECT id, full_name, avatar_url, email, position, birth_date, about, password_hash, created_at, updated_at
		FROM users WHERE email = $1`,
		email,
	).Scan(
		&user.ID,
		&user.FullName,
		&avatarField,
		&user.Email,
		&user.Position,
		&birthDateField,
		&aboutField,
		&passwordHash,
		&user.CreatedAt,
		&user.UpdatedAt,
	)
	if err == sql.ErrNoRows {
		return nil, ErrInvalidCredentials
	} else if err != nil {
		return nil, fmt.Errorf("query user: %w", err)
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

	err = bcrypt.CompareHashAndPassword([]byte(passwordHash), []byte(req.Password))
	if err != nil {
		return nil, ErrInvalidCredentials
	}

	token, err := s.generateToken(user)
	if err != nil {
		return nil, fmt.Errorf("generate token: %w", err)
	}

	return &models.LoginResponse{
		User:  user,
		Token: token,
	}, nil
}

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

func (s *AuthService) ChangePassword(ctx context.Context, userID int64, req models.ChangePasswordRequest) (*models.ChangePasswordResponse, error) {
	req.OldPassword = strings.TrimSpace(req.OldPassword)
	req.NewPassword = strings.TrimSpace(req.NewPassword)
	req.ConfirmNew = strings.TrimSpace(req.ConfirmNew)

	if req.OldPassword == "" || req.NewPassword == "" || req.ConfirmNew == "" {
		return nil, ErrMissingFields
	}
	if len(req.NewPassword) < 8 {
		return nil, ErrWeakPassword
	}
	if req.NewPassword == req.OldPassword {
		return nil, ErrNewPasswordSameAsOld
	}
	if req.NewPassword != req.ConfirmNew {
		return nil, ErrPasswordDoNotMatch
	}

	user, passwordHash, err := s.getUserByIDWithPassword(ctx, userID)
	if err != nil {
		return nil, err
	}

	if err := bcrypt.CompareHashAndPassword([]byte(passwordHash), []byte(req.OldPassword)); err != nil {
		return nil, ErrInvalidCredentials
	}

	newHash, err := hashPassword(req.NewPassword)
	if err != nil {
		return nil, err
	}

	_, err = s.db.ExecContext(ctx, `
		UPDATE users
		SET password_hash = $1, updated_at = NOW()
		WHERE id = $2
	`, newHash, userID)
	if err != nil {
		return nil, fmt.Errorf("update password: %w", err)
	}

	token, err := s.generateToken(user)
	if err != nil {
		return nil, fmt.Errorf("generate token: %w", err)
	}

	return &models.ChangePasswordResponse{
		User:  user,
		Token: token,
	}, nil
}

func (s *AuthService) ChangeEmail(ctx context.Context, userID int64, req models.ChangeEmailRequest) (*models.ChangeEmailResponse, error) {
	req.NewEmail = strings.TrimSpace(req.NewEmail)
	req.Password = strings.TrimSpace(req.Password)

	if req.NewEmail == "" {
		return nil, ErrNewEmailIsRequired
	}
	if req.Password == "" {
		return nil, ErrPasswordIsRequired
	}

	newEmail, err := normalizeAndValidateEmail(req.NewEmail)
	if err != nil {
		return nil, err
	}

	user, passwordHash, err := s.getUserByIDWithPassword(ctx, userID)
	if err != nil {
		return nil, err
	}

	if err := bcrypt.CompareHashAndPassword([]byte(passwordHash), []byte(req.Password)); err != nil {
		return nil, ErrInvalidCredentials
	}

	if strings.EqualFold(user.Email, newEmail) {
		return nil, ErrEmailSameAsCurrent
	}

	var exists bool
	err = s.db.QueryRowContext(ctx,
		`SELECT EXISTS (SELECT 1 FROM users WHERE email = $1)`,
		newEmail,
	).Scan(&exists)
	if err != nil {
		return nil, fmt.Errorf("check email exists: %w", err)
	}
	if exists {
		return nil, ErrEmailAlreadyTaken
	}

	_, err = s.db.ExecContext(ctx, `
		UPDATE users
		SET email = $1, updated_at = NOW()
		WHERE id = $2
	`, newEmail, userID)
	if err != nil {
		return nil, fmt.Errorf("update email: %w", err)
	}

	user.Email = newEmail

	token, err := s.generateToken(user)
	if err != nil {
		return nil, fmt.Errorf("generate token: %w", err)
	}

	return &models.ChangeEmailResponse{
		User:  user,
		Token: token,
	}, nil
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
			if !validatePosition(&p) {
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
			if err := validateBirthDateAge(parsed); err != nil {
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
