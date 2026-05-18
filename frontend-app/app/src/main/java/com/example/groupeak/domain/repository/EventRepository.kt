package com.example.groupeak.domain.repository

import com.example.groupeak.data.remote.dto.EventResponse

interface EventRepository {
    suspend fun getMyEvents(): Result<List<EventResponse>>

    suspend fun createEvent(
        projectId: Long,
        title: String,
        meetingUrl: String?,
        description: String?,
        startAt: String,
        endAt: String?,
        participantIds: List<Long>
    ): Result<Unit>
    suspend fun updateEvent(
        projectId: Long,
        eventId: Long,
        title: String?,
        meetingUrl: String?,
        description: String?,
        startAt: String?,
        endAt: String?,
        participantIds: List<Long>?
    ): Result<Unit>

    suspend fun deleteEvent(
        projectId: Long,
        eventId: Long
    ): Result<Unit>
}
