package services

import (
	"context"
	"database/sql"
	"fmt"
	"log/slog"
	"net/http"
	"strings"

	"groupeak/internal/apperror"
	"groupeak/internal/dto"
	"groupeak/internal/models"
	"groupeak/internal/repository"
)

var (
	ErrEventNotFound         = apperror.New(http.StatusNotFound, "event not found")
	ErrNotEventCreator       = apperror.New(http.StatusForbidden, "user is not event creator or project owner")
	ErrEventTitleRequired    = apperror.New(http.StatusBadRequest, "event title is required")
	ErrEventStartAtRequired  = apperror.New(http.StatusBadRequest, "event start_at is required")
	ErrEventEndBeforeStart   = apperror.New(http.StatusBadRequest, "end_at cannot be before start_at")
	ErrEventNoFields         = apperror.New(http.StatusBadRequest, "no fields to update")
	ErrInvalidParticipantIDs = apperror.New(http.StatusBadRequest, "one or more participant IDs are invalid")
)

type EventService struct {
	db     *sql.DB
	repo   repository.EventRepository
	logger *slog.Logger
}

func NewEventService(db *sql.DB, repo repository.EventRepository, logger *slog.Logger) *EventService {
	return &EventService{db: db, repo: repo, logger: logger}
}

// RunInTx оборачивает операции в транзакцию
func (s *EventService) RunInTx(ctx context.Context, fn func(tx repository.DBTX) error) error {
	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		s.logger.Error("failed to begin tx", slog.String("error", err.Error()))
		return apperror.New(http.StatusInternalServerError, "internal server error")
	}
	defer func() { _ = tx.Rollback() }()

	if err := fn(tx); err != nil {
		return err
	}

	if err := tx.Commit(); err != nil {
		s.logger.Error("failed to commit tx", slog.String("error", err.Error()))
		return apperror.New(http.StatusInternalServerError, "internal server error")
	}
	return nil
}

func (s *EventService) CreateEvent(ctx context.Context, userID, projectID int, req dto.CreateEventRequest) (*dto.EventWithParticipants, error) {
	req.Title = strings.TrimSpace(req.Title)
	if req.Title == "" {
		return nil, ErrEventTitleRequired
	}
	if req.StartAt.IsZero() {
		return nil, ErrEventStartAtRequired
	}
	if req.EndAt != nil && req.EndAt.Before(req.StartAt) {
		return nil, ErrEventEndBeforeStart
	}

	event := &models.Event{
		ProjectID:   projectID,
		Title:       req.Title,
		MeetingURL:  req.MeetingURL,
		Description: req.Description,
		StartAt:     req.StartAt,
		EndAt:       req.EndAt,
	}

	err := s.RunInTx(ctx, func(tx repository.DBTX) error {
		if errTx := s.repo.CreateEvent(ctx, tx, event); errTx != nil {
			return errTx
		}

		participants := req.ParticipantIDs
		found := false
		for _, id := range participants {
			if id == userID {
				found = true
				break
			}
		}
		if !found {
			participants = append(participants, userID)
		}

		if len(participants) > 0 {
			exists, errTx := s.repo.CheckUsersExist(ctx, tx, participants)
			if errTx != nil {
				return errTx
			}
			if !exists {
				return ErrInvalidParticipantIDs
			}

			if errTx := s.repo.AddParticipants(ctx, tx, event.ID, participants); errTx != nil {
				return errTx
			}
		}

		event.ParticipantIDs = participants
		return nil
	})

	if err != nil {
		s.logger.Error("failed to create event", slog.String("error", err.Error()))
		return nil, err
	}

	return &dto.EventWithParticipants{
		Event:        *event,
		Participants: event.ParticipantIDs,
	}, nil
}

func (s *EventService) UpdateEvent(ctx context.Context, userID, projectID, eventID int, req dto.UpdateEventRequest) error {
	authorized, err := s.repo.IsEventAuthorized(ctx, s.db, userID, projectID, eventID)
	if err != nil {
		s.logger.Error("failed to check event auth", slog.String("error", err.Error()))
		return apperror.New(http.StatusInternalServerError, "internal server error")
	}
	if !authorized {
		return ErrNotEventCreator
	}

	setParts := make([]string, 0)
	args := make([]interface{}, 0)
	idx := 1

	if req.Title != nil {
		val := strings.TrimSpace(*req.Title)
		if val == "" {
			return ErrEventTitleRequired
		}
		setParts = append(setParts, fmt.Sprintf("title = $%d", idx))
		args = append(args, val)
		idx++
	}

	if req.MeetingURL != nil {
		setParts = append(setParts, fmt.Sprintf("meeting_url = $%d", idx))
		args = append(args, *req.MeetingURL)
		idx++
	}

	if req.Description != nil {
		setParts = append(setParts, fmt.Sprintf("description = $%d", idx))
		args = append(args, *req.Description)
		idx++
	}

	if req.StartAt != nil {
		setParts = append(setParts, fmt.Sprintf("start_at = $%d", idx))
		args = append(args, *req.StartAt)
		idx++
	}

	if req.EndAt != nil {
		setParts = append(setParts, fmt.Sprintf("end_at = $%d", idx))
		args = append(args, *req.EndAt)
		idx++
	}

	if len(setParts) == 0 && req.ParticipantIDs == nil {
		return ErrEventNoFields
	}

	err = s.RunInTx(ctx, func(tx repository.DBTX) error {
		if len(setParts) > 0 {
			if errTx := s.repo.UpdateEventDynamic(ctx, tx, eventID, setParts, args); errTx != nil {
				return errTx
			}
		}

		if req.ParticipantIDs != nil {
			if errTx := s.repo.ClearParticipants(ctx, tx, eventID); errTx != nil {
				return errTx
			}
			if len(req.ParticipantIDs) > 0 {
				exists, errTx := s.repo.CheckUsersExist(ctx, tx, req.ParticipantIDs)
				if errTx != nil {
					return errTx
				}
				if !exists {
					return ErrInvalidParticipantIDs
				}

				if errTx := s.repo.AddParticipants(ctx, tx, eventID, req.ParticipantIDs); errTx != nil {
					return errTx
				}
			}
		}
		return nil
	})

	if err != nil {
		s.logger.Error("failed to update event", slog.String("error", err.Error()))
		return err
	}

	s.logger.Info("event updated", slog.Int("event_id", eventID))
	return nil
}

func (s *EventService) DeleteEvent(ctx context.Context, userID, projectID, eventID int) error {
	authorized, err := s.repo.IsEventAuthorized(ctx, s.db, userID, projectID, eventID)
	if err != nil {
		s.logger.Error("failed to check event auth", slog.String("error", err.Error()))
		return apperror.New(http.StatusInternalServerError, "internal server error")
	}
	if !authorized {
		return ErrNotEventCreator
	}

	err = s.repo.DeleteEvent(ctx, s.db, eventID)
	if err == sql.ErrNoRows {
		return ErrEventNotFound
	} else if err != nil {
		s.logger.Error("failed to delete event", slog.String("error", err.Error()))
		return apperror.New(http.StatusInternalServerError, "internal server error")
	}

	s.logger.Info("event deleted", slog.Int("event_id", eventID))
	return nil
}

func (s *EventService) GetUserEvents(ctx context.Context, userID int) ([]models.Event, error) {
	events, eventIDs, err := s.repo.GetUserEvents(ctx, s.db, userID)
	if err != nil {
		s.logger.Error("failed to get user events", slog.String("error", err.Error()))
		return nil, apperror.New(http.StatusInternalServerError, "internal server error")
	}

	if len(events) == 0 {
		return events, nil
	}

	partsMap, err := s.repo.GetParticipantsForEvents(ctx, s.db, eventIDs)
	if err != nil {
		s.logger.Error("failed to get participants", slog.String("error", err.Error()))
		return nil, apperror.New(http.StatusInternalServerError, "internal server error")
	}

	for i := range events {
		if pIDs := partsMap[events[i].ID]; pIDs != nil {
			events[i].ParticipantIDs = pIDs
		} else {
			events[i].ParticipantIDs = []int{}
		}
	}

	return events, nil
}
