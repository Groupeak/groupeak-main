# 🧩 Архитектура backend-приложения

```txt
backend-app/
├─ cmd/
│  └─ api/
│     └─ main.go
│
├─ internal/
│  ├─ config/
│  │  └─ config.go
│  │
│  ├─ db/
│  │  ├─ postgres.go
│  │  └─ redis.go
│  │
│  ├─ models/
│  │  ├─ user.go
│  │  ├─ project.go
│  │  └─ task.go
│  │
│  ├─ services/
│  │  ├─ auth_service.go
│  │  ├─ user_service.go
│  │  ├─ project_service.go
│  │  └─ task_service.go
│  │
│  ├─ handlers/
│  │  ├─ auth_handlers.go
│  │  ├─ user_handlers.go
│  │  ├─ project_handlers.go
│  │  └─ task_handlers.go
│  │
│  ├─ middleware/
│  │  ├─ auth_middleware.go
│  │  ├─ logging_middleware.go
│  │  └─ recovery_middleware.go
│  │
│  └─ router/
│     └─ router.go
│
├─ migrations/
│  ├─ 001_create_users.sql
│  ├─ 002_create_projects.sql
│  ├─ 003_create_tasks.sql
│  ├─ 004_create_project_calendar.sql
│  ├─ 005_notifications.sql
│
├─ Dockerfile
├─ docker-compose.yml
├─ .env
├─ go.mod
├─ go.sum
└─ README.md
```

## Основные слои

* **`cmd/api/main.go`** – точка входа:

  * загружает конфиг,
  * поднимает подключения к PostgreSQL и Redis,
  * запускает автомиграции БД через goose,
  * собирает роутер и middleware,
  * запускает HTTP-сервер.

* **`internal/config/`** – конфигурация:

  * `config.go` читает переменные окружения / `.env`,
  * собирает структуру `Config` (порты, подключения к БД, секреты и т.д.).

* **`internal/db/`** – доступ к хранилищам:

  * `postgres.go` – инициализация пула подключений к PostgreSQL и запуск SQL-миграций (goose.Up),
  * `redis.go` – клиент Redis (кэш, сессии, токены).

* **`internal/models/`** – модели домена:

  * `user.go`, `project.go`, `task.go` описывают сущности и DTO,
  * структуры мапятся на таблицы БД и JSON-ответы.

* **`internal/services/`** – бизнес-логика:

  * `auth_service.go` – регистрация, логин, работа с паролями и токенами,
  * `user_service.go` – управление пользователями,
  * `project_service.go` – операции с проектами,
  * `task_service.go` – операции с задачами.
  * Сервисы знают про БД/Redis, но не про HTTP.

* **`internal/handlers/`** – HTTP-обработчики:

  * принимают запрос (JSON/параметры),
  * вызывают нужный сервис,
  * формируют HTTP-ответ (статус + JSON).

* **`internal/middleware/`** – общие прослойки:

  * `auth_middleware.go` – проверка JWT/сессии и проброс `userID` в контекст,
  * `logging_middleware.go` – логирование запросов и ответов,
  * `recovery_middleware.go` – защита от паник (возврат 500 вместо падения сервера).

* **`internal/router/router.go`** – маршрутизация:

  * создание и настройка роутера,
  * подключение middleware,
  * регистрация маршрутов (`/auth`, `/users`, `/projects`, `/tasks`).

* **`migrations/`** – SQL-миграции (goose):

  * `001_create_users.sql` – таблица пользователей,
  * `002_create_projects.sql` – таблица проектов,
  * `003_create_tasks.sql` – таблица задач и индексы по проекту/исполнителю,
  * `004_create_project_calendar.sql` – таблица событий календаря и индекс по проекту,
  * `005_notifications.sql` – таблица уведомлений и индексы по связям.

---

## 🗄️ Миграции БД (goose)

В проекте используется инструмент миграций **[github.com/pressly/goose](https://github.com/pressly/goose)**.

### Формат SQL-миграций

Каждый файл в `migrations/` имеет два блока:

```sql
-- +goose Up
-- SQL, который применяется при обновлении схемы (создание/изменение)

-- +goose Down
-- SQL для отката (возврат схемы к предыдущему состоянию)
```

Goose хранит текущую версию схемы в служебной таблице `goose_db_version` и при запуске применяет только те миграции, которые ещё не были выполнены.

### Автомиграции при старте backend-приложения

Файл `internal/db/postgres.go`:

* создаёт подключение к PostgreSQL через `pgx`,
* настраивает диалект `postgres` для goose,
* вызывает:

```go
goose.Up(db, "./migrations")
```

Это означает, что при каждом старте `cmd/api`:

* backend автоматически проверяет состояние схемы БД,
* накатывает все новые миграции из директории `./migrations`,
* если что-то пошло не так (ошибка SQL / нет доступа к БД), приложение не стартует и пишет ошибку в лог.

---

# 🗄️ Схема базы данных

В проекте используется 5 основных таблиц:

1. **`users`** – пользователи

   * ключевые поля: `id`, `full_name`, `avatar_url`, `email` (уникальный), `password_hash`, `position`, `created_at`, `updated_at`;
   * связи:

     * владелец проектов: `projects.owner_id → users.id`;
     * исполнитель задач: `tasks.assignee_id → users.id`;
     * адресат уведомлений: `notifications.user_id → users.id`.

2. **`projects`** – проекты

   * поля: `id`, `owner_id`, `name`, `description`, `deadline_at`, `created_at`, `updated_at`;
   * связи:

     * владелец: `owner_id → users.id`;
     * задачи проекта: `tasks.project_id → projects.id`;
     * уведомления: `notifications.project_id → projects.id`;
     * события календаря: `project_calendar.project_id → projects.id`.

3. **`tasks`** – задачи

   * поля: `id`, `project_id`, `title`, `description`, `comments`, `status` (`todo/in_progress/done/postponed`), `priority` (`low/medium/high`), `assignee_id`, `due_at`, `created_at`, `updated_at`;
   * связи:

     * проект: `project_id → projects.id`;
     * исполнитель: `assignee_id → users.id`;
     * привязанные уведомления: `notifications.task_id → tasks.id`.

4. **`notifications`** – уведомления

   * поля: `id`, `project_id`, `user_id`, `task_id`, `calendar_event_id`, `title`, `message`, `position`, `is_approved`, `created_at`;
   * связи:

     * проект: `project_id → projects.id`;
     * пользователь (получатель): `user_id → users.id` (может быть `NULL`);
     * задача: `task_id → tasks.id` (может быть `NULL`);
     * событие календаря: `calendar_event_id → project_calendar.id` (может быть `NULL`).

5. **`project_calendar`** – календарь созвонов и встреч

   * поля: `id`, `project_id`, `role` (`owner/member/all`), `title`, `description`, `meeting_link`, `event_at`, `created_at`;
   * связи:

     * проект: `project_id → projects.id`;
     * по событиям календаря могут создаваться уведомления: `notifications.calendar_event_id → project_calendar.id`.

Такое разделение позволяет:

* явно хранить пользователей, проекты, задачи и события календаря,
* строить уведомления, привязанные к конкретному проекту / задаче / событию,
* легко расширять бизнес-логику (роли, статусы, кэш, уведомления) без ломки схемы.

