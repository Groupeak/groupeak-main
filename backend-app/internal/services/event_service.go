package services

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"groupeak/internal/models"
	"log"
)

var (
	ErrBeginTx           = errors.New("failed to begin transaction")
	ErrInsertEvent       = errors.New("failed to insert event")
	ErrPrepareStmt       = errors.New("failed to prepare participant statement")
	ErrInsertParticipant = errors.New("failed to insert participant")
	ErrCommitTx          = errors.New("failed to commit transaction")
	ErrUpdateEvent       = errors.New("failed to update event fields")
	ErrClearParticipants = errors.New("failed to clear old participants")
	ErrEventNotFound     = errors.New("event not found")
	ErrDeleteEvent       = errors.New("failed to delete event")
	ErrGetEvents         = errors.New("failed to get events")
	ErrNotEventCreator   = errors.New("user is not event creator or project owner")
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
        INSERT INTO project_events 
            (project_id, title, meeting_url, description, start_at, end_at, created_at, updated_at)
        VALUES ($1, $2, $3, $4, $5, $6, NOW(), NOW())
        RETURNING id, created_at, updated_at`

	err = tx.QueryRowContext(ctx, queryEvent,
		event.ProjectID,
		event.Title,
		event.MeetingURL,
		event.Description,
		event.StartAt,
		event.EndAt,
	).Scan(&event.ID, &event.CreatedAt, &event.UpdatedAt)

	if err != nil {
		log.Printf("[ERROR] CreateEvent INSERT failed: %+v", err) // ← добавь это
		return nil, fmt.Errorf("%w: %v", ErrInsertEvent, err)
	}

	// ←←← остальной код (участники) без изменений ↓
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

func (s *EventService) DeleteEvent(ctx context.Context, userID, projectID, eventID int) error {
	isAuthorized, err := s.isEventAuthorized(ctx, userID, projectID, eventID)
	if err != nil {
		return err
	}
	if !isAuthorized {
		return ErrNotEventCreator
	}

	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return fmt.Errorf("%w: %v", ErrBeginTx, err)
	}
	defer tx.Rollback()

	_, err = tx.ExecContext(ctx, `DELETE FROM event_participants WHERE event_id = $1`, eventID)
	if err != nil {
		return fmt.Errorf("%w: %v", ErrClearParticipants, err)
	}

	_, err = tx.ExecContext(ctx, `DELETE FROM project_events WHERE id = $1 AND project_id = $2`, eventID, projectID)
	if err != nil {
		return fmt.Errorf("%w: %v", ErrDeleteEvent, err)
	}

	if err := tx.Commit(); err != nil {
		return fmt.Errorf("%w: %v", ErrCommitTx, err)
	}

	return nil
}

func (s *EventService) GetUserEvents(ctx context.Context, userID int) ([]models.Event, error) {
	query := `
		SELECT 
			pe.id, pe.project_id, pe.title, pe.meeting_url, pe.description,
			pe.start_at, pe.end_at, pe.created_at, pe.updated_at
		FROM project_events pe
		INNER JOIN event_participants ep ON ep.event_id = pe.id
		WHERE ep.user_id = $1
		ORDER BY pe.start_at ASC
	`

	rows, err := s.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, fmt.Errorf("%w: %v", ErrGetEvents, err)
	}
	defer rows.Close()

	events := make([]models.Event, 0)

	for rows.Next() {
		var event models.Event
		var meetingURL, description sql.NullString
		var endAt sql.NullTime

		err := rows.Scan(
			&event.ID,
			&event.ProjectID,
			&event.Title,
			&meetingURL,
			&description,
			&event.StartAt,
			&endAt,
			&event.CreatedAt,
			&event.UpdatedAt,
		)
		if err != nil {
			return nil, fmt.Errorf("failed to scan event: %v", err)
		}

		if meetingURL.Valid {
			event.MeetingURL = &meetingURL.String
		}
		if description.Valid {
			event.Description = &description.String
		}
		if endAt.Valid {
			event.EndAt = &endAt.Time
		}

		participantIDs, err := s.getEventParticipants(ctx, event.ID)
		if err != nil {
			return nil, err
		}
		event.ParticipantIDs = participantIDs

		events = append(events, event)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("%w: %v", ErrGetEvents, err)
	}

	return events, nil
}

func (s *EventService) isEventAuthorized(ctx context.Context, userID, projectID, eventID int) (bool, error) {
	query := `
		SELECT EXISTS(
			SELECT 1 FROM event_participants ep
			WHERE ep.event_id = $1 AND ep.user_id = $2
		) OR (
			SELECT owner_id FROM projects WHERE id = $3
		) = $2
	`

	var authorized bool
	err := s.db.QueryRowContext(ctx, query, eventID, userID, projectID).Scan(&authorized)
	if err != nil {
		return false, fmt.Errorf("failed to check event authorization: %v", err)
	}

	return authorized, nil
}

func (s *EventService) getEventParticipants(ctx context.Context, eventID int) ([]int, error) {
	query := `SELECT user_id FROM event_participants WHERE event_id = $1 ORDER BY user_id`

	rows, err := s.db.QueryContext(ctx, query, eventID)
	if err != nil {
		return nil, fmt.Errorf("failed to get participants: %v", err)
	}
	defer rows.Close()

	participants := make([]int, 0)
	for rows.Next() {
		var userID int
		if err := rows.Scan(&userID); err != nil {
			return nil, fmt.Errorf("failed to scan participant: %v", err)
		}
		participants = append(participants, userID)
	}

	return participants, nil
}
