# Структура фронтенда (Android / Jetpack Compose)

## Основной модуль: `app`

```
app/
└── src/main/java/com/example/groupeak
    ├── MainActivity.kt

    ├── data
    │   └── remote
    │       ├── api
    │       │   ├── ApiClient.kt
    │       │   └── AuthApi.kt
    │       ├── dto
    │       │   └── AuthDtos.kt
    │       ├── errors
    │       │   └── ErrorParser.kt
    │       └── repository
    │           └── AuthRepositoryImpl.kt

    ├── domain
    │   └── repository
    │       └── AuthRepository.kt

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
        │   │   └── Account.kt
        │   ├── profile/
        │   │   ├── ProfileScreen.kt
        │   │   ├── ProfileUiState.kt
        │   │   └── ProfileViewModel.kt
        │   └── feedback/
        │       └── FeedbackScreen.kt
        │
        └── theme/
            ├── Color.kt
            ├── Fonts.kt
            ├── Theme.kt
            └── Type.kt
```

> Примечание: внутри MAIN-графа также могут быть дополнительные экраны настроек (например, изменение email/пароля), если они присутствуют в проекте — они подключаются в `MainNavGraph.kt`.

---

## 1) Входная точка и навигация

### `MainActivity.kt`
Точка входа приложения. В `setContent { ... }` вызывается тема `GroupeakTheme { ... }`, после чего стартует **корневая навигация** через `RootNavGraph(...)`.

---

## 2) Навигационные графы

В проекте используется разделение на два крупных графа:

- **AUTH-граф** — экраны входа/регистрации/старта
- **MAIN-граф** — основная часть приложения (нижняя навигация + внутренние экраны)

### `ui/navigation/Graph.kt`
Файл с константами/именами графов (например `AUTH`, `MAIN`) для удобного `navigate(...)` и `popUpTo(...)`.

### `ui/navigation/RootNavGraph.kt`
Корневой `NavHost`, который:
- стартует с AUTH-части (экран `Start` → `Login` / `Register`);
- после успешной авторизации переводит пользователя в MAIN;
- при выходе (logout) возвращает в AUTH и очищает backstack MAIN (через `popUpTo(...){ inclusive = true }`).

### `ui/navigation/MainNavGraph.kt`
Граф навигации **внутри** основной части приложения (MAIN). Здесь объявляются composable-экраны вкладок и связанные с ними переходы.

### `ui/navigation/Screen.kt`
`sealed class Screen(val route: String)` — набор маршрутов для экранов (и AUTH, и MAIN), используемый в `navigate(...)`.

---

## 3) MAIN-контейнер и нижняя навигация

### `ui/screens/main/MainRoot.kt`
`MainRoot` — это общий контейнер MAIN-части:
- `Scaffold(...)` с `bottomBar`;
- отдельный `NavController` для MAIN-графа;
- внутри размещается `MainNavGraph(...)`.

### `ui/components/AppBottomBar.kt`
Нижняя панель навигации (`NavigationBar`) с вкладками. Обычно содержит 3 пункта:
- **Аккаунт**
- **Профиль**
- **Связь / Обратная связь**

---

## 4) AUTH-экраны (start/login/register)

### `ui/screens/start/StartScreen.kt`
Стартовый экран с фоном `GroupeakBackground`, логотипом и CTA:
- `Войти` → переход на `Login`
- `Нет аккаунта?` → переход на `Register`

### `ui/screens/login/LoginScreen.kt` + `LoginViewModel.kt`
Экран авторизации:
- UI не ходит напрямую в сеть — вся логика через `LoginViewModel`
- `LoginViewModel` использует `AuthRepository` из `ServiceLocator`

#### Dev-режим: обход авторизации (Debug)
В debug-сборке может быть включён флаг обхода авторизации через `BuildConfig.DEV_BYPASS_AUTH`.
- Это ускоряет разработку UI
- В релизе должен быть выключен

### `ui/screens/register/RegisterScreen.kt` + `RegisterViewModel.kt`
Экран регистрации с валидацией и отображением ошибок (полевых и общих).

---

## 5) MAIN-экраны (account/profile/feedback)

### `ui/screens/account/Account.kt`
Экран аккаунта.
- Вёрстка адаптирована под общий `MainRoot` (нижняя панель вынесена в `AppBottomBar`).
- Иконка уведомлений имеет увеличенную область нажатия (hitbox) для удобства.

### `ui/screens/profile/ProfileScreen.kt`
Экран профиля переработан под state-driven подход:
- UI получает состояние и коллбеки
- Бизнес-логика унесена в ViewModel

#### `ProfileUiState.kt`
Модель состояния экрана профиля (значения полей, флаги режима редактирования и т.п.).

#### `ProfileViewModel.kt`
ViewModel, которая:
- хранит `uiState`;
- обрабатывает изменения полей (`onFullNameChange`, `onBirthDateChange`, и т.д.);
- управляет режимом редактирования/сохранения (`onEditClick`, `onSaveClick`).

### `ui/screens/feedback/FeedbackScreen.kt`
Экран «Обратная связь» заменён с заглушки на полноценную форму:
- поля ввода (включая многострочное);
- скролл/компоновка под длинный текст;
- кнопка отправки.

---

## 6) UI-компоненты (ui/components)

### `Background.kt`
`GroupeakBackground(content)` — градиентный фон + белый полупрозрачный слой, в который оборачиваются экраны.

### `GroupeakWordmark.kt`
`GroupeakWordmark(...)` — градиентный текст «Groupeak» на шрифте Onest.

### `InputField.kt` / `PasswordInputField.kt`
Кастомные поля ввода на базе `TextField`:
- рамки/цвета для focus/error
- иконки слева/справа
- поддержка ошибок (текст под полем), если она добавлена в текущей реализации

### `PrimaryButton.kt`
Основная CTA-кнопка (фиксированные размеры, радиус 18dp, брендовый цвет).

---

## 7) Тема и оформление (ui/theme)
- `Color.kt` — брендовые цвета + вспомогательная палитра
- `Fonts.kt` — шрифт Onest (несколько начертаний)
- `Theme.kt` / `Type.kt` — обёртка `GroupeakTheme`, типографика Material

---

## 8) Сетевой слой и работа с backend

Интеграция с backend реализована через Retrofit/OkHttp:

- `data.remote.api.ApiClient` — настройка Retrofit и baseUrl
- `data.remote.api.AuthApi` — эндпоинты авторизации
- `data.remote.dto.AuthDtos` — DTO запросов/ответов
- `data.remote.repository.AuthRepositoryImpl` — реализация репозитория с `Result<AuthResponse>`
- `domain.repository.AuthRepository` — интерфейс
- `service_locator.ServiceLocator` — единая точка сборки зависимостей для репозиториев

---

## 9) Ошибки и валидация

- Полевые ошибки показываются рядом с полями (email, пароль и т.д.)
- Общие ошибки показываются под кнопкой/внизу формы
- Валидация на клиенте:
  - email (формат)
  - пароль (минимальная длина, подтверждение)
  - ФИО (не пустое)
  - позиция (допустимые значения)

---

### Настройка `BASE_URL`
```kotlin
// ApiClient.kt
private const val BASE_URL = "http://10.0.2.2:8080/" // Для эмулятора
// или "http://localhost:8080/" для реального устройства
```

---
