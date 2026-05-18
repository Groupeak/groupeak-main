package com.example.groupeak.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateTaskRequest(
    @SerializedName("task_name") val taskName: String,
    @SerializedName("task_description") val taskDescription: String? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("status") val status: String = "in_progress",
    @SerializedName("priority") val priority: String,
    @SerializedName("assignee_ids") val assigneeIds: List<Int>,
)

data class TaskDto(
    @SerializedName("id") val id: Long,
    @SerializedName("project_id") val projectId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("comments") val comments: String?,
    @SerializedName("status") val status: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("assignee_ids") val assigneeIds: List<Long>,
    @SerializedName("due_at") val dueAt: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

data class CreateTaskResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("task") val task: TaskDto,
)

data class ListTasksResponse(
    @SerializedName("tasks") val tasks: List<TaskDto>?,
)

data class TaskResponse(
    @SerializedName("task") val task: TaskDto,
)

data class StatusResponse(
    @SerializedName("status") val status: String,
)

data class PatchTaskRequest(
    @SerializedName("task_name") val taskName: String? = null,
    @SerializedName("task_description") val taskDescription: String? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("clear_due_at") val clearDueAt: Boolean? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("assignee_ids") val assigneeIds: List<Int>? = null,
    @SerializedName("comments") val comments: String? = null,
)