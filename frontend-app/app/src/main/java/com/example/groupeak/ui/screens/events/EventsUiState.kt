package com.example.groupeak.ui.screens.events

data class EventsUiState(
    val events: List<EventUiModel> = emptyList(),
    val selectedEvent: EventUiModel? = null
)

data class EventUiModel(
    val id: Int,
    val time: String,
    val title: String,
    val project: String,
    val date: String,          // Для списка: "28 Марта, пн."
    val fullDate: String,      // Для диалога: "28.03.2026"
    val participants: List<Participant>,
    val link: String?,
    val description: String?
)

data class Participant(
    val name: String,
    val role: String
)

// Данные для мероприятий (имитация backend)
object EventsData {
    val allEvents = listOf(
        EventUiModel(
            id = 1,
            time = "12:45",
            title = "Конференция PR-отдела",
            project = "SmartHome",
            date = "28 Марта, пн.",
            fullDate = "28.03.2026",
            participants = listOf(
                Participant("Дупло М.", "Frontend"),
                Participant("Петров В.", "Backend"),
                Participant("Иванов А.", "Designer"),
                Participant("Сидорова Е.", "PR"),
                Participant("Козлов П.", "Manager"),
                Participant("Морозова Д.", "Analyst"),
                Participant("Новиков С.", "DevOps"),
                Participant("Волкова А.", "QA"),
                Participant("Лебедев К.", "Frontend"),
                Participant("Кузнецова М.", "Backend"),
                Participant("Павлов Д.", "Designer"),
                Participant("Семенова О.", "Manager"),
                Participant("Григорьев И.", "DevOps"),
                Participant("Степанова Н.", "QA")
            ),
            link = "https://telemost.360.yandex.ru/j0031133...",
            description = "Добрый день! Планируется конференция по ссылке. Просьба не опаздывать!"
        ),
        EventUiModel(
            id = 2,
            time = "18:00",
            title = "Корпоратив",
            project = "SmartHome",
            date = "28 Марта, пн.",
            fullDate = "28.03.2026",
            participants = listOf(
                Participant("Все сотрудники", "")
            ),
            link = null,
            description = "Ежегодный корпоратив компании. Место проведения будет уточнено."
        ),
        EventUiModel(
            id = 3,
            time = "13:40",
            title = "Плановое собеседование с потенциальным клиентом",
            project = "SmartHome",
            date = "31 Марта, вт.",
            fullDate = "31.03.2026",
            participants = listOf(
                Participant("Иванов А.", "Manager"),
                Participant("Петрова С.", "HR")
            ),
            link = "https://zoom.us/j/123456789",
            description = "Обсуждение требований к проекту. Подготовка презентации."
        ),
        EventUiModel(
            id = 4,
            time = "18:00",
            title = "Корпоратив",
            project = "SmartHome",
            date = "12 Июля, пт.",
            fullDate = "12.07.2026",
            participants = listOf(
                Participant("Все сотрудники", "")
            ),
            link = null,
            description = "Летний корпоратив на природе."
        )
    )
}