package models

import "time"


type User struct {
	ID int64 `json:"id"`
	FullName string `json:"full_name"`
	AvatarURL *string `json:"avatar_url,omitempty"`
	Email string `json:"email"`
	Position string `json:"position"`  // (backend, frontend, uiux, pm)
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type RegisterRequest struct {
	FullName string `json:"full_name"`
	Email string `json:"email"`
	Password  string `json:"password"`
	Position string `json:"position"`
	AvatarURL *string `json:"avatar_url"`
}

type RegisterResponse struct {
	User User `json:"user"`
	Token string `json:"token"` // jwt token
}