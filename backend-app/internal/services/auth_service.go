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
	ErrEmailAlreadyTaken = errors.New("email already taken")
	ErrInvalidPosition = errors.New("invalid position")
	ErrMissingFields = errors.New("full_name, email and password are required")
	ErrWeakPassword = errors.New("password must be at least 8 characters long")
	ErrInvalidEmail = errors.New("invalid email format")
	ErrUnsupportedEmailDomain = errors.New("email domain is not supported")
	ErrInvalidCredentials = errors.New("invalid credentials")
	ErrInvalidBirthDate = errors.New("invalid birth date, expected format dd.mm.yyyy")
)

var allowedDomains = map[string]bool{
	"yandex.ru": true,
	"yandex.com": true,
	"ya.ru": true,
	"mail.ru": true,
	"inbox.ru": true,
	"rambler.ru": true,
	"gmail.com": true,
	"googlemail.com": true,
	"outlook.com": true,
	"icloud.com": true,
	"yahoo.com": true,
	"mail.com": true,
	"sfedu.ru": true,
}

type JWTClaims struct {
	UserID int64  `json:"user_id"`
	Email string `json:"email"`
	Position string `json:"position"`
	jwt.RegisteredClaims
}

type AuthService struct {
	db *sql.DB
	jwtToken []byte
}

func NewAuthService(db *sql.DB, jwtToken string) *AuthService {
	return &AuthService{
		db: db,
		jwtToken: []byte(jwtToken),
	}
}

// временная организация роли
func validatePosition(position string) bool {
	switch position {
	case "backend", "frontend", "fullstack", "mobile", "uiux",
		"devops", "software architect", "tech lead", "qa automation",
		"data engineer", "machine learning engineer", "embedded", "game",
		"blockchain", "systems programmer", "pm":
		return true
	default:
		return false
	}
}

func normalizeAndValidateEmail(email string) (string, error) {
	email = strings.ToLower(strings.TrimSpace(email))
	if email == "" {
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

func (s *AuthService) generateToken(user models.User) (string, error) {
	now := time.Now()
	claims := JWTClaims{
		UserID: user.ID,
		Email: user.Email,
		Position: user.Position,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject: fmt.Sprint(user.ID),
			ExpiresAt: jwt.NewNumericDate(now.Add(24 * time.Hour)),
			IssuedAt: jwt.NewNumericDate(now),
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
	if req.FullName == "" || req.Email == "" || req.Password == "" {
		return nil, ErrMissingFields
	}
	if len(req.Password) < 8 {
		return nil, ErrWeakPassword
	}
	if !validatePosition(req.Position) {
		return nil, ErrInvalidPosition
	}

	email, err := normalizeAndValidateEmail(req.Email)
	if err != nil {
		return nil, err
	}

	// разбор birth_date (опционально)
	var birthDateParam interface{}
	if req.BirthDate != nil && strings.TrimSpace(*req.BirthDate) != "" {
		bdStr := strings.TrimSpace(*req.BirthDate)
		parsed, err := time.Parse("02.01.2006", bdStr)
		if err != nil {
			return nil, ErrInvalidBirthDate
		}
		birthDateParam = parsed
	} else {
		birthDateParam = nil
	}

	// about (опционально)
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
		user models.User
		avatarField sql.NullString
		avatarParam interface{}
		birthDateField sql.NullTime
		aboutField sql.NullString
	)

	if req.AvatarURL != nil && *req.AvatarURL != "" {
		avatarParam = *req.AvatarURL
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
		req.Position,
		birthDateParam,
		aboutParam,
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
	if err != nil {
		return nil, fmt.Errorf("insert user: %w", err)
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
		user models.User
		passwordHash string
		avatarField sql.NullString
		birthDateField sql.NullTime
		aboutField sql.NullString
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
