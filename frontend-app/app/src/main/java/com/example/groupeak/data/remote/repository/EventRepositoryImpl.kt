package com.example.groupeak.data.remote.repository

import android.util.Log
import com.example.groupeak.data.remote.api.EventApi
import com.example.groupeak.data.remote.dto.CreateEventRequest
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
}
