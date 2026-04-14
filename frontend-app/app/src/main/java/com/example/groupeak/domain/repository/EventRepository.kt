package com.example.groupeak.domain.repository

interface EventRepository {
    suspend fun createEvent(
        projectId: Long,
        title: String,
        meetingUrl: String?,
        description: String?,
        startAt: String,
        endAt: String?,
        participantIds: List<Long>
    ): Result<Unit>
}
