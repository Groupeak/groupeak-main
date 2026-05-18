package com.example.groupeak.data.remote.repository

import android.util.Log
import com.example.groupeak.data.remote.api.EventApi
import com.example.groupeak.data.remote.dto.CreateEventRequest
import com.example.groupeak.data.remote.dto.EventResponse
import com.example.groupeak.domain.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepositoryImpl(
    private val api: EventApi
) : EventRepository {

    override suspend fun createEvent(
        projectId: Long,
        title: String,
        meetingUrl: String?,
        description: String?,
        startAt: String,
        endAt: String?,
        participantIds: List<Long>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = CreateEventRequest(
                title = title,
                meetingUrl = meetingUrl,
                description = description,
                startAt = startAt,
                endAt = endAt,
                participantIds = participantIds
            )
            api.createEvent(projectId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepositoryImpl", "createEvent error", e)
            Result.failure(e)
        }
    }

    override suspend fun getMyEvents(): Result<List<EventResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMyEvents()
            Result.success(response.events ?: emptyList())
        } catch (e: Exception) {
            Log.e("EventRepositoryImpl", "getMyEvents error", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEvent(
        projectId: Long,
        eventId: Long,
        title: String?,
        meetingUrl: String?,
        description: String?,
        startAt: String?,
        endAt: String?,
        participantIds: List<Long>?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = com.example.groupeak.data.remote.dto.UpdateEventRequest(
                title = title,
                meetingUrl = meetingUrl,
                description = description,
                startAt = startAt,
                endAt = endAt,
                participantIds = participantIds
            )
            api.updateEvent(projectId, eventId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepositoryImpl", "updateEvent error", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(
        projectId: Long,
        eventId: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.deleteEvent(projectId, eventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepositoryImpl", "deleteEvent error", e)
            Result.failure(e)
        }
    }
}
