package models

import "time"

type Project struct {
	ID int64 `json:"id"`
	OwnerID int64 `json:"owner_id"`
	Name string `json:"project_name"`
	Description *string `json:"description,omitempty"`
	DeadlineAt *time.Time `json:"deadline_at,omitempty"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

type CreateProjectRequest struct {
	Name string `json:"name"`
	Description *string `json:"description"`
	DeadlineAt *time.Time `json:"deadline_at"`
}

type ProjectRole string

const (
	ProjectRoleOwner ProjectRole = "owner"
	ProjectRoleMember ProjectRole = "member"
)

type ProjectListItem struct {
	Project
	Role ProjectRole `json:"role"`
}

type InviteMemberRequest struct {
	Email string `json:"email"`
}

type ProjectMember struct {
	ID int64 `json:"id"`
	ProjectID int64 `json:"project_id"`
	UserID int64 `json:"user_id"`
	Role ProjectRole `json:"role"`
	CreatedAt time.Time `json:"created_at"`
}

type ProjectInvite struct {
	ID int64 `json:"id"`
	ProjectID int64     `json:"project_id"`
	Email string `json:"email"`
	Token string `json:"token"`
	Status string `json:"status"` // pending / accepted / revoked
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}
