# Структура фронтенда (Android / Jetpack Compose)

## Основной модуль: `app`

```
app/
└── src/main/java/com/example/groupeak
    ├── MainActivity.kt

    ├── data
│   ├── local
│   │   └── SessionManager.kt
│   └── remote
│       ├── api
│       │   ├── ApiClient.kt
│       │   ├── AuthApi.kt
│       │   ├── AuthInterceptor.kt
│       │   └── UserApi.kt
│       ├── dto
│       │   ├── AuthDtos.kt
│       │   └── UserDtos.kt
│       ├── errors
│       │   └── ErrorParser.kt
│       └── repository
│           ├── AuthRepositoryImpl.kt
│           └── UserRepositoryImpl.kt

├── domain
│   └── repository
│       ├── AuthRepository.kt
│       └── UserRepository.kt

├── service_locator
│   └── ServiceLocator.kt

└── ui
    ├── components/
    │   ├── AppBottomBar.kt
    │   ├── Background.kt
    │   ├── GroupeakWordmark.kt
    │   ├── InputField.kt
    │   ├── PasswordInputField.kt
    │   └── PrimaryButton.kt
    │
    ├── navigation/
    │   ├── Graph.kt
    │   ├── MainNavGraph.kt
    │   ├── RootNavGraph.kt
    │   └── Screen.kt
    │
    ├── screens/
    │   ├── start/
    │   │   └── StartScreen.kt
    │   ├── login/
    │   │   ├── LoginScreen.kt
    │   │   └── LoginViewModel.kt
    │   ├── register/
    │   │   ├── RegisterScreen.kt
    │   │   └── RegisterViewModel.kt
    │   ├── main/
    │   │   └── MainRoot.kt
    │   ├── account/
    │   │   ├── Account.kt
    │   │   ├── AccountUiState.kt
    │   │   └── AccountViewModel.kt
    │   ├── profile/
    │   │   ├── ProfileScreen.kt
    │   │   ├── ProfileUiState.kt
    │   │   └── ProfileViewModel.kt
    │   ├── feedback/
    │   │   └── FeedbackScreen.kt
    │   ├── change_email/
    │   │   ├── ChangeEmailScreen.kt
    │   │   ├── ChangeEmailUiState.kt
    │   │   └── ChangeEmailViewModel.kt
    │   ├── change_password/
    │   │   ├── ChangePasswordScreen.kt
    │   │   ├── ChangePasswordUiState.kt
    │   │   └── ChangePasswordViewModel.kt
    │   └── notifications/
    │       └── NotificationsScreen.kt
    │
    └── theme/
        ├── Color.kt
        ├── Fonts.kt
        ├── Theme.kt
        └── Type.kt
```


> Примечание: внутри MAIN-графа также есть заглушки экранов `Tasks` и `Calendar` (если они присутствуют в `Screen.kt` и `MainNavGraph.kt`).

---

## 1) Входная точка и инициализация

### `MainActivity.kt`
Точка входа приложения.

Важно: зависимости и сетевой слой инициализируются **до** `setContent`:

- `ServiceLocator.init(applicationContext)`

Далее в `setContent { ... }` вызывается `GroupeakTheme { ... }`, после чего стартует корневая навигация через `RootNavGraph(...)`.

---

## 2) Навигация

В проекте используется разделение на два крупных графа:

- **AUTH-граф** — экраны start/login/register
- **MAIN-граф** — основная часть приложения (нижняя навигация + внутренние экраны)

### `ui/navigation/Graph.kt`
Константы графов: `AUTH`, `MAIN`.

### `ui/navigation/RootNavGraph.kt`
Корневой `NavHost`, который:
- стартует с `AUTH`;
- после успешного логина переводит пользователя в `MAIN` и очищает backstack `AUTH`;
- при выходе (logout) очищает сессию (`SessionManager.clear()`), возвращает в `AUTH` и очищает backstack `MAIN`.

### `ui/navigation/MainNavGraph.kt`
Граф навигации внутри `MAIN`:
- `Account`, `Profile`, `Feedback`
- `ChangeEmail`, `ChangePassword`
- `Notifications`
- заглушки `Tasks`, `Calendar` (если включены)

### `ui/navigation/Screen.kt`
`sealed class Screen(val route: String)` — маршруты всех экранов.

---

## 3) MAIN-контейнер и нижняя навигация

### `ui/screens/main/MainRoot.kt`
`MainRoot` — контейнер MAIN-части:
- `Scaffold(...)` с `bottomBar`;
- отдельный `NavController` для MAIN-графа;
- внутри размещается `MainNavGraph(...)`.

### `ui/components/AppBottomBar.kt`
Нижняя панель навигации (`NavigationBar`) с вкладками.

---

## 4) AUTH-экраны (start/login/register)

### `ui/screens/start/StartScreen.kt`
Стартовый экран:
- `Войти` → `Login`
- `Регистрация` → `Register`

### `ui/screens/login/LoginScreen.kt` + `LoginViewModel.kt`
Экран авторизации:
- UI не ходит напрямую в сеть — вся логика через `LoginViewModel`
- `LoginViewModel` использует `AuthRepository`

> Ранее в debug мог существовать обход авторизации через `BuildConfig.DEV_BYPASS_AUTH`, но в текущей версии он **не используется**.

### `ui/screens/register/RegisterScreen.kt` + `RegisterViewModel.kt`
Экран регистрации с валидацией и отображением ошибок.

---

## 5) MAIN-экраны

### `ui/screens/account/Account.kt` (+ `AccountViewModel`)
Экран аккаунта:
- отображает данные пользователя из сессии/профиля
- содержит переходы на:
  - профиль
  - обратную связь
  - смену email/пароля
  - уведомления
  - выход (logout)

### `ui/screens/profile/ProfileScreen.kt` (+ `ProfileViewModel`, `ProfileUiState`)
Экран профиля со state-driven подходом:
- `ProfileViewModel` управляет состоянием
- загрузка/обновление профиля через `UserRepository`
- данные пользователя синхронизируются с `SessionManager.user`

### `ui/screens/feedback/FeedbackScreen.kt`
Экран «Обратная связь»:
- email отправителя подставляется из текущей сессии (`SessionManager.user`)

### `ui/screens/change_email/*`
Смена email:
- запрос уходит на backend через `UserRepository.changeEmail(...)`
- при успехе обновляется токен и пользователь в `SessionManager`

### `ui/screens/change_password/*`
Смена пароля:
- запрос уходит на backend через `UserRepository.changePassword(...)`

### `ui/screens/notifications/NotificationsScreen.kt`
Экран уведомлений (на текущий момент может быть заглушкой/простым списком).

---

## 6) Session / JWT

### `data/local/SessionManager.kt`
- хранит JWT в **DataStore Preferences** (ключ `jwt_token`, хранилище `session`)
- предоставляет:
  - `token: StateFlow<String?>`
  - `user: StateFlow<UserDto?>`
- `clear()` очищает токен и пользователя

---

## 7) Сетевой слой и работа с backend

Интеграция с backend реализована через Retrofit/OkHttp:

- `data.remote.api.ApiClient` — создание Retrofit и API-интерфейсов (`AuthApi`, `UserApi`)
- `data.remote.api.AuthInterceptor` — добавляет `Authorization: Bearer <token>` для запросов
- `data.remote.dto.*` — DTO для запросов/ответов
- `data.remote.repository.*` — реализации репозиториев
- `domain.repository.*` — интерфейсы репозиториев
- `service_locator.ServiceLocator` — сборка зависимостей (репозитории, SessionManager, ApiClient.init)

### Endpoints (UserApi)
- `GET api/v1/user/profile`
- `PUT api/v1/user/profile`
- `POST api/v1/user/change-email`
- `POST api/v1/user/change-password`

### Ошибки
`data.remote.errors.ErrorParser` пытается распарсить тело ошибки как JSON (`error`/`message`), иначе показывает текст/код.

---

## 8) Настройка `BASE_URL`

Актуальный `BASE_URL` находится в `ApiClient.kt`:

```kotlin
private const val BASE_URL = "http://10.0.2.2:3030/" // Для Android Emulator
