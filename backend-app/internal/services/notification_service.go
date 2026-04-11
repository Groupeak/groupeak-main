package services

import (
	"context"
	"database/sql"
	"encoding/json"
	"log/slog"

	"groupeak/internal/eventbus"
	"groupeak/internal/models"
	"groupeak/internal/repository"
)

type NotificationService struct {
	db        *sql.DB
	notifRepo repository.NotificationRepository
	taskRepo  repository.TaskRepository
	eventRepo repository.EventRepository // Добавили для митингов
	logger    *slog.Logger
}

func NewNotificationService(db *sql.DB, nr repository.NotificationRepository, tr repository.TaskRepository, er repository.EventRepository, logger *slog.Logger) *NotificationService {
	return &NotificationService{
		db:        db,
		notifRepo: nr,
		taskRepo:  tr,
		eventRepo: er,
		logger:    logger,
	}
}

// HandleEvent ловит события из шины
func (s *NotificationService) HandleEvent(ctx context.Context, e eventbus.SystemEvent) {
	var targetUserIDs []int64

	switch e.Type {
	case models.EventTypeTaskCreated, models.EventTypeTaskUpdated, models.EventTypeTaskDeleted, models.EventTypeTaskReviewed:
		// Если это задача - берем исполнителей
		assignees, err := s.taskRepo.GetAssigneesForTask(ctx, s.db, e.EntityID)
		if err == nil {
			targetUserIDs = assignees
		}

	case models.EventTypeProjectMember, models.EventTypeProjectKicked:
		// Если инвайт/кик - берем ID из payload
		if tID, ok := e.Payload["target_user_id"].(int64); ok {
			targetUserIDs = []int64{tID}
		}

	case models.EventTypeEventCreated, models.EventTypeEventUpdated:
		// Если изменился митинг - берем участников
		partsMap, err := s.eventRepo.GetParticipantsForEvents(ctx, s.db, []int{int(e.EntityID)})
		if err == nil && len(partsMap[int(e.EntityID)]) > 0 {
			for _, pID := range partsMap[int(e.EntityID)] {
				targetUserIDs = append(targetUserIDs, int64(pID))
			}
		}
	}

	payloadBytes, err := json.Marshal(e.Payload)
	if err != nil {
		s.logger.Error("failed to marshal notification payload", slog.String("error", err.Error()))
		return
	}

	for _, targetID := range targetUserIDs {
		if targetID == e.ActorID {
			continue // Самому себе не шлем
		}

		entID := e.EntityID
		actorID := e.ActorID

		n := &models.Notification{
			ProjectID: e.ProjectID,
			UserID:    targetID,
			ActorID:   &actorID,
			Type:      e.Type, // Используем строгий тип
			EntityID:  &entID,
			Payload:   payloadBytes,
		}

		_ = s.notifRepo.Create(ctx, s.db, n)
	}
}

func (s *NotificationService) GetMyNotifications(ctx context.Context, userID int64, limit, offset int) ([]models.Notification, error) {
	notifs, err := s.notifRepo.GetUserNotifications(ctx, s.db, userID, limit, offset)
	if notifs == nil {
		notifs = []models.Notification{}
	}
	return notifs, err
}

func (s *NotificationService) MarkAsRead(ctx context.Context, userID int64, notificationIDs []int64) error {
	return s.notifRepo.MarkAsRead(ctx, s.db, userID, notificationIDs)
}

func (s *NotificationService) MarkAllAsRead(ctx context.Context, userID int64) error {
	return s.notifRepo.MarkAllAsRead(ctx, s.db, userID)
}

func (s *NotificationService) GetUnreadCount(ctx context.Context, userID int64) (int, error) {
	return s.notifRepo.GetUnreadCount(ctx, s.db, userID)
}
