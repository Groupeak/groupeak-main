package com.example.groupeak.data.remote.api

import com.example.groupeak.data.remote.dto.CreateEventRequest
import com.example.groupeak.data.remote.dto.EventResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface EventApi {

    @POST("api/v1/projects/{projectId}/events")
    suspend fun createEvent(
        @Path("projectId") projectId: Long,
        @Body body: CreateEventRequest,
    ): EventResponse
}
