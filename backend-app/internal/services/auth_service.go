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
type jwtClaims struct {
	UserID int64 `json:"user_id"`
	Email string `json:"email"`
	Position string `json:"position"`
	jwt.RegisteredClaims
}

type AuthService struct {
	db *sql.DB
	jwtToken []byte
}

func NewAuthService(db *sql.DB, jwtToken string) *AuthService {
	return &AuthService {
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

	claims := jwtClaims {
		UserID: user.ID,
		Email: user.Email,
		Position: user.Position,
		RegisteredClaims: jwt.RegisteredClaims {
			Subject: fmt.Sprint(user.ID),
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
		INSERT INTO users (full_name, avatar_url, email, password_hash, position)
		VALUES ($1, $2, $3, $4, $5)
		RETURNING id, full_name, avatar_url, email, position, created_at, updated_at;`

	var (
		user models.User
		avatarField sql.NullString
		avatarParam interface{}
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
	).Scan(
		&user.ID,
		&user.FullName,
		&avatarField,
		&user.Email,
		&user.Position,
		&user.CreatedAt,
		&user.UpdatedAt,
	)
	if err != nil {
		return nil, fmt.Errorf("insert user: %w", err)
	}

	if avatarField.Valid {
		user.AvatarURL = &avatarField.String
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


/*
	=== ЗОНА ДЛЯ ВТОРОГО РАЗРАБОТЧИКА (АВТОРИЗАЦИЯ) ===

	Здесь удобно добавить метод Login:

	func (s *AuthService) Login(ctx context.Context, email, password string) (*models.LoginResponse, error) {
		// 1. Найти пользователя по email.
		// 2. Сравнить пароль через bcrypt.CompareHashAndPassword.
		// 3. Сгенерировать JWT через s.generateToken.
		// 4. Вернуть токен и данные юзера.
	}

	Структуры для Login можно добавить в internal/models, по аналогии с RegisterRequest/Response.
*/