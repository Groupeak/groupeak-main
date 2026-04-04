package com.example.groupeak.domain.repository

import com.example.groupeak.data.remote.dto.TaskDto

interface TaskRepository {

    suspend fun createTask(
        projectId: Long,
        taskName: String,
        taskDescription: String?,
        dueDate: String?,
        priority: String,
        assigneeIds: List<Int>,
    ): Result<TaskDto>

    suspend fun getTasks(
        projectId: Long? = null,
        priority: String? = null,
    ): Result<List<TaskDto>>

    suspend fun getProjectTasks(
        projectId: Long,
        category: String? = null,
        assignees: List<Long> = emptyList(),
        limit: Int? = null,
        offset: Int? = null,
    ): Result<List<TaskDto>>

    suspend fun getTaskById(
        projectId: Long,
        taskId: Long,
    ): Result<TaskDto>

    suspend fun submitForReview(
        projectId: Long,
        taskId: Long,
    ): Result<Unit>

    suspend fun getMyTasks(): Result<List<TaskDto>>

    suspend fun getNearestTasks(): Result<List<TaskDto>>
}