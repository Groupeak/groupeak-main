package models

import "time"

type User struct {
	ID        int64     `json:"id"`
	FullName  string    `json:"full_name"`
	AvatarURL *string   `json:"avatar_url,omitempty"`
	Email     string    `json:"email"`
	Position  string    `json:"position"`
	BirthDate *string   `json:"birth_date,omitempty"`
	About     *string   `json:"about,omitempty"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type RegisterRequest struct {
	FullName  string  `json:"full_name"`
	Email     string  `json:"email"`
	Password  string  `json:"password"`
	Position  string  `json:"position"`
	AvatarURL *string `json:"avatar_url"`
	BirthDate *string `json:"birth_date"`
	About     *string `json:"about"`
}

type RegisterResponse struct {
	User  User   `json:"user"`
	Token string `json:"token"`
}

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type LoginResponse struct {
	User  User   `json:"user"`
	Token string `json:"token"`
}

type ChangePasswordRequest struct {
	OldPassword string `json:"old_password"`
	NewPassword string `json:"new_password"`
	ConfirmNew  string `json:"confirm_new"`
}

type ChangePasswordResponse struct {
	User  User   `json:"user"`
	Token string `json:"token"`
}

type ChangeEmailRequest struct {
	NewEmail string `json:"new_email"`
	Password string `json:"password"`
}

type ChangeEmailResponse struct {
	User  User   `json:"user"`
	Token string `json:"token"`
}
