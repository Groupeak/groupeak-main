package com.example.groupeak.data.remote.api

import com.example.groupeak.data.remote.dto.CreateEventRequest
import com.example.groupeak.data.remote.dto.EventListResponse
import com.example.groupeak.data.remote.dto.EventResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Path

interface EventApi {

    @POST("api/v1/projects/{projectId}/events")
    suspend fun createEvent(
        @Path("projectId") projectId: Long,
        @Body body: CreateEventRequest,
    ): EventResponse

    @GET("api/v1/events/my")
    suspend fun getMyEvents(): EventListResponse
    @PATCH("api/v1/projects/{projectId}/events/{eventId}")
    suspend fun updateEvent(
        @Path("projectId") projectId: Long,
        @Path("eventId") eventId: Long,
        @Body body: com.example.groupeak.data.remote.dto.UpdateEventRequest,
    ): retrofit2.Response<Unit>

    @DELETE("api/v1/projects/{projectId}/events/{eventId}")
    suspend fun deleteEvent(
        @Path("projectId") projectId: Long,
        @Path("eventId") eventId: Long,
    ): retrofit2.Response<Unit>
}
