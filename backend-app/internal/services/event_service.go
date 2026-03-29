package services

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"groupeak/internal/models"
)

var (
	ErrBeginTx           = errors.New("failed to begin transaction")
	ErrInsertEvent       = errors.New("failed to insert event")
	ErrPrepareStmt       = errors.New("failed to prepare participant statement")
	ErrInsertParticipant = errors.New("failed to insert participant")
	ErrCommitTx          = errors.New("failed to commit transaction")
	ErrUpdateEvent       = errors.New("failed to update event fields")
	ErrClearParticipants = errors.New("failed to clear old participants")
)

type EventService struct {
	db *sql.DB
}

func NewEventService(db *sql.DB) *EventService {
	return &EventService{db: db}
}

func (s *EventService) CreateEvent(ctx context.Context, projectID int, req models.CreateEventRequest) (*models.Event, error) {
	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("%w: %v", ErrBeginTx, err)
	}
	defer tx.Rollback()

	event := &models.Event{
		ProjectID:   projectID,
		Title:       req.Title,
		MeetingURL:  req.MeetingURL,
		Description: req.Description,
		StartAt:     req.StartAt,
		EndAt:       req.EndAt,
	}

	queryEvent := `
        INSERT INTO project_events (project_id, title, meeting_url, description, start_at, end_at)
        VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING id, created_at, updated_at`

	err = tx.QueryRowContext(ctx, queryEvent,
		event.ProjectID, event.Title, event.MeetingURL, event.Description, event.StartAt, event.EndAt,
	).Scan(&event.ID, &event.CreatedAt, &event.UpdatedAt)
	if err != nil {
		return nil, fmt.Errorf("%w: %v", ErrInsertEvent, err)
	}

	if len(req.ParticipantIDs) > 0 {
		queryParticipant := `INSERT INTO event_participants (event_id, user_id) VALUES ($1, $2)`
		stmt, err := tx.PrepareContext(ctx, queryParticipant)
		if err != nil {
			return nil, fmt.Errorf("%w: %v", ErrPrepareStmt, err)
		}
		defer stmt.Close()

		for _, userID := range req.ParticipantIDs {
			if _, err := stmt.ExecContext(ctx, event.ID, userID); err != nil {
				return nil, fmt.Errorf("%w (user_id: %d): %v", ErrInsertParticipant, userID, err)
			}
			event.ParticipantIDs = append(event.ParticipantIDs, userID)
		}
	}

	if err := tx.Commit(); err != nil {
		return nil, fmt.Errorf("%w: %v", ErrCommitTx, err)
	}

	return event, nil
}

func (s *EventService) UpdateEvent(ctx context.Context, projectID, eventID int, req models.UpdateEventRequest) (*models.Event, error) {
	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("%w: %v", ErrBeginTx, err)
	}
	defer tx.Rollback()

	queryUpdate := `
        UPDATE project_events 
        SET 
            title = COALESCE($1, title),
            meeting_url = CASE WHEN $2::boolean THEN $3 ELSE meeting_url END,
            description = CASE WHEN $4::boolean THEN $5 ELSE description END,
            start_at = COALESCE($6, start_at),
            end_at = CASE WHEN $7::boolean THEN $8 ELSE end_at END,
            updated_at = NOW()
        WHERE id = $9 AND project_id = $10`

	_, err = tx.ExecContext(ctx, queryUpdate,
		req.Title,
		req.MeetingURL != nil, req.MeetingURL,
		req.Description != nil, req.Description,
		req.StartAt,
		req.EndAt != nil, req.EndAt,
		eventID, projectID)

	if err != nil {
		return nil, fmt.Errorf("%w: %v", ErrUpdateEvent, err)
	}

	if req.ParticipantIDs != nil {
		_, err = tx.ExecContext(ctx, `DELETE FROM event_participants WHERE event_id = $1`, eventID)
		if err != nil {
			return nil, fmt.Errorf("%w: %v", ErrClearParticipants, err)
		}

		if len(req.ParticipantIDs) > 0 {
			queryParticipant := `INSERT INTO event_participants (event_id, user_id) VALUES ($1, $2)`
			stmt, err := tx.PrepareContext(ctx, queryParticipant)
			if err != nil {
				return nil, fmt.Errorf("%w: %v", ErrPrepareStmt, err)
			}
			defer stmt.Close()

			for _, userID := range req.ParticipantIDs {
				if _, err := stmt.ExecContext(ctx, eventID, userID); err != nil {
					return nil, fmt.Errorf("%w (user_id: %d): %v", ErrInsertParticipant, userID, err)
				}
			}
		}
	}

	if err := tx.Commit(); err != nil {
		return nil, fmt.Errorf("%w: %v", ErrCommitTx, err)
	}

	return nil, nil
}
