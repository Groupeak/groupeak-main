package models

import "time"

type User struct {
	ID int64 `json:"id"`
	FullName string `json:"full_name"`
	AvatarURL *string `json:"avatar_url,omitempty"`
	Email string `json:"email"`
	Position string `json:"position"` // (backend, frontend, uiux, pm)
	BirthDate *string `json:"birth_date,omitempty"` // формат "dd.mm.yyyy"
	About *string `json:"about,omitempty"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type RegisterRequest struct {
	FullName string `json:"full_name"`
	Email string `json:"email"`
	Password string `json:"password"`
	Position string `json:"position"`
	AvatarURL *string `json:"avatar_url"`
	BirthDate *string `json:"birth_date"` // "dd.mm.yyyy"
	About *string `json:"about"`
}

type RegisterResponse struct {
	User User `json:"user"`
	Token string `json:"token"` // jwt token
}

type LoginRequest struct {
	Email string `json:"email"`
	Password string `json:"password"`
}

type LoginResponse struct {
	User User `json:"user"`
	Token string `json:"token"` // jwt token
}
