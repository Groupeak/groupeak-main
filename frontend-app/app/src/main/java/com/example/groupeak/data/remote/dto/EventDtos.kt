package com.example.groupeak.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateEventRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("meeting_url")
    val meetingUrl: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("start_at")
    val startAt: String,
    @SerializedName("end_at")
    val endAt: String?,
    @SerializedName("participant_ids")
    val participantIds: List<Long>
)

data class EventResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("project_id")
    val projectId: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("meeting_url")
    val meetingUrl: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("start_at")
    val startAt: String,
    @SerializedName("end_at")
    val endAt: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("participants")
    val participants: List<Long>?
)
