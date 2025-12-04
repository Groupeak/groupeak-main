# Структура фронтенда Groupeak (Android / Jetpack Compose)

Основной модуль: `app`

```text
app/
└── src/main/java/com/example/groupeak
    ├── MainActivity.kt
    └── ui/
        ├── components/
        │   ├── Background.kt
        │   ├── GroupeakWordmark.kt
        │   ├── InputField.kt
        │   ├── PasswordInputField.kt
        │   └── PrimaryButton.kt
        │
        ├── navigation/
        │   ├── AppNavGraph.kt
        │   └── Screen.kt
        │
        ├── screens/
        │   ├── start/
        │   │   └── StartScreen.kt
        │   ├── login/
        │   │   └── LoginScreen.kt
        │   └── register/
        │       └── RegisterScreen.kt
        │
        └── theme/
            ├── Color.kt
            ├── Fonts.kt
            ├── Theme.kt
            └── Type.kt
```

---

## 1. Входная точка и навигация

### `MainActivity.kt`

- Точка входа приложения.
- В `setContent { ... }` вызывается:
  - `GroupeakTheme { GroupeakApp() }`
  - внутри `GroupeakApp()` создаётся `NavHostController` и вызывается `AppNavGraph(navController)`.

---

### Пакет `ui/navigation`

**Файлы:**

- `Screen.kt`
  - `sealed class Screen(val route: String)` с тремя экранами:
    - `Screen.Start` – `"start"`
    - `Screen.Login` – `"login"`
    - `Screen.Register` – `"register"`
  - Используется для навигации.

- `AppNavGraph.kt`
  - `@Composable fun AppNavGraph(navController: NavHostController)`
  - Описывает граф навигации:
    - `startDestination = Screen.Start.route`
    - `Screen.Start` → `StartScreen(...)`
    - `Screen.Login` → `LoginScreen(...)`
    - `Screen.Register` → `RegisterScreen(...)`
  - Переходы:
    - со стартового экрана:
      - `onLoginClick` → `navController.navigate(Screen.Login.route)`
      - `onRegisterClick` → `navController.navigate(Screen.Register.route)`
    - стрелка «Назад» на `Login`/`Register` → `navController.popBackStack()`

Если нужно добавить новый экран:
1. Добавить новый `object` в `Screen`.
2. Добавить `composable(Screen.XXX.route) { ... }` в `AppNavGraph`.
3. Навигироваться по `navController.navigate(Screen.XXX.route)`.

---

## 2. Экраны (`ui/screens`)

### Пакет `ui/screens/start`

**Файл:** `StartScreen.kt`  
**Composable:** `StartScreen(onLoginClick, onRegisterClick)`

- Фон: `GroupeakBackground { ... }`.
- Верх:
  - логотип (картинка `R.drawable.logo_groupeak`);
  - `GroupeakWordmark` (градиентный текстовый логотип).
- Текст-слоган по центру.
- Внизу:
  - `PrimaryButton("Войти")` → `onLoginClick()`;
  - текст «Нет аккаунта?» (кликабелен) → `onRegisterClick()`.

Используется как стартовый экран приложения.

---

### Пакет `ui/screens/login`

**Файл:** `LoginScreen.kt`  
**Composable:**  
`LoginScreen(onBack, onForgotPassword = {}, onLogin = { _, _ -> })`

- Фон: `GroupeakBackground`.
- Верхний `Box`:
  - иконка «Назад» (`ic_back_arrow`) слева → `onBack()`;
  - `GroupeakWordmark` по центру.
- Заголовок: «Авторизация».
- Поля:
  - `InputField` для email:
    - `label = "Email"`, `placeholder = "Email"`;
    - `iconResId = R.drawable.ic_email`;
    - `KeyboardType.Email`.
  - `PasswordInputField` для пароля:
    - `label = "Пароль"`;
    - `placeholder = "Пароль"`;
    - `lockIconResId = R.drawable.ic_lock`.
- Низ:
  - `PrimaryButton("Войти")` → `onLogin(email, password)`;
  - текст «Забыли пароль?» → `onForgotPassword()`.

---

### Пакет `ui/screens/register`

**Файл:** `RegisterScreen.kt`  
**Composable:**  
`RegisterScreen(onBack, onForgotPassword = {}, onLogin = { _, _ -> })`

Структура похожа на `LoginScreen`:

- Фон: `GroupeakBackground`.
- Верх:
  - стрелка «Назад» → `onBack()`;
  - `GroupeakWordmark`.
- Заголовок: «Регистрация».

Поля:

1. `InputField` — ФИО:
   - `label = "ФИО"`, `placeholder = "ФИО"`;
   - `iconResId = R.drawable.ic_fio`.

2. `InputField` — Email:
   - `label = "Email"`, `placeholder = "Email"`;
   - `iconResId = R.drawable.ic_email`.

3. `PasswordInputField` — пароль:
   - `label = "Пароль"`, `placeholder = "Пароль"`;
   - `lockIconResId = R.drawable.ic_lock`.

4. `PasswordInputField` — подтверждение пароля:
   - `label = "Подтвердите пароль"`;
   - `placeholder = "Подтверждение пароля"`;
   - `lockIconResId = R.drawable.ic_lock`;
   - принимает флаг `isError` для подсветки.

Валидация паролей:

- Есть локальный стейт `showPasswordError: Boolean`.
- При изменении `password` или `secondPassword` ошибка сбрасывается.
- При клике по кнопке «Зарегистрироваться»:
  - вычисляется `hasError = (password.isNotEmpty() && secondPassword.isNotEmpty() && password != secondPassword)`;
  - `showPasswordError = hasError`;
  - если `hasError == false` → вызывается `onLogin(email, password)`.
- Если `showPasswordError == true`:
  - второе поле пароля (`PasswordInputField` для подтверждения) подсвечивается красной рамкой (`isError = true`);
  - под полем отображается текст «Пароли не совпадают».

Низ:

- `PrimaryButton("Зарегистрироваться")`.
- Текст «Есть аккаунт?» с `onForgotPassword()` по клику (при желании можно заменить на переход на экран логина).

---

## 3. UI-компоненты (`ui/components`)

### `Background.kt`

**Composable:** `GroupeakBackground(content: @Composable () -> Unit)`

- Рисует градиентный фон + белый полупрозрачный слой сверху.
- Внутрь передаётся любой контент (экраны оборачиваются этим фоном).

---

### `GroupeakWordmark.kt`

**Composable:** `GroupeakWordmark(fontSize, modifier = Modifier)`

- Текст «Groupeak» с горизонтальным градиентом.
- Использует `OnestFontFamily` и `FontWeight.SemiBold`.

---

### `InputField.kt`

**Composable:**  
`InputField(value, onValueChange, placeholder, modifier = Modifier, label = null, iconResId = null, keyboardOptions = ..., visualTransformation = ..., singleLine = true, trailingIcon = null, isError = false)`

Обёртка над `TextField` с кастомным стилем:

- Фиксированная ширина/высота (под дизайн).
- Скругления `RoundedCornerShape(18.dp)`.
- Кастомная рамка:
  - без фокуса: без рамки;
  - в фокусе: рамка `BluePrimary300`;
  - при ошибке (`isError = true`): рамка красная (`0xFFFF4D4F`).
- Placeholder и label:
  - имеют `fontSize = 16.sp`;
  - при ошибке становятся красными;
  - при фокусе label/placeholder меняют цвет согласно логике (см. код).
- Иконка слева:
  - по `iconResId`, размер `24.dp`, цвет `Gray700`.
- Иконка справа:
  - через `trailingIcon` (например, глаз в `PasswordInputField`).
- Цвета контейнера/индикатора:
  - фон всегда белый (`focused/unfocused/errorContainerColor = Color.White`);
  - все индикаторы (`focused/unfocused/errorIndicatorColor`) — `Color.Transparent`;
  - чтобы не было линии/подчёркивания снизу даже при ошибке.

---

### `PasswordInputField.kt`

**Composable:**  
`PasswordInputField(modifier = Modifier, value, label = null, onValueChange, placeholder = "Пароль", lockIconResId = null, isError = false)`

Основан на `InputField`:

- `keyboardOptions = KeyboardType.Password`.
- Локальный стейт `passwordVisible` (`true/false`).
- `visualTransformation`:
  - `PasswordVisualTransformation()` если `!passwordVisible`;
  - `VisualTransformation.None` если `passwordVisible`.
- Слева: иконка замка (`lockIconResId`).
- Справа: иконка глаза (`ic_eye_on` / `ic_eye_off`), по клику — переключение видимости.
- Флаг `isError` пробрасывается в `InputField` → управляет рамкой и цветами.

---

### `PrimaryButton.kt`

**Composable:**  
`PrimaryButton(text, onClick, fontSize)`

- Кнопка с фиксированными размерами и скруглением 18dp.
- Цвет фона — `BluePrimary300`.
- Текст:
  - шрифт `OnestFontFamily`,
  - `FontWeight.SemiBold`,
  - `fontSize` передаётся параметром.

Используется как основная CTA-кнопка на экранах.

---

## 4. Тема и оформление (`ui/theme`)

### `Color.kt`

- Описаны все цвета, используемые в компонентах:
  - брендовые (`GroupeakBlue`, `GroupeakGray`, градиенты),
  - дополнительные (`BluePrimary200/300`, `Gray600/650` и т.п.).
- Эти цвета используются напрямую в компонентах (`InputField`, `PrimaryButton`, `StartScreen`, и т.д.).

### `Fonts.kt`

- Подключён шрифт **Onest**:
  - `OnestFontFamily` с весами `Light`, `Normal`, `SemiBold`.
- Используется во всех основных текстах (`StartScreen`, `LoginScreen`, `RegisterScreen`, кнопка, логотип).

### `Type.kt`, `Theme.kt`

- `Type.kt`:
  - определяет `Typography` для `MaterialTheme` (пока почти дефолтный, Onest в основном задаётся явно в компонентах/экранах).
- `Theme.kt`:
  - `GroupeakTheme { ... }` — обёртка над `MaterialTheme`.
  - Сейчас `LightColorScheme` / `DarkColorScheme` — почти стандартные (фиолетово-розовые), а реальные цвета задаются вручную в компонентах.

> Для фронтенда важно: глобальная тема уже есть (`GroupeakTheme`), но кастомные цвета и шрифт в основном задаются на уровне компонентов/экранов, а не через `MaterialTheme.colorScheme/typography`.

---
