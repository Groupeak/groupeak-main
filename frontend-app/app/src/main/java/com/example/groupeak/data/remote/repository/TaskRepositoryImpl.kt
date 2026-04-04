package com.example.groupeak.data.remote.repository

import com.example.groupeak.data.remote.api.TaskApi
import com.example.groupeak.data.remote.dto.CreateTaskRequest
import com.example.groupeak.data.remote.dto.TaskDto
import com.example.groupeak.data.remote.errors.parseApiError
import com.example.groupeak.domain.repository.TaskRepository

class TaskRepositoryImpl(
    private val api: TaskApi,
) : TaskRepository {

    override suspend fun createTask(
        projectId: Long,
        taskName: String,
        taskDescription: String?,
        dueDate: String?,
        priority: String,
        assigneeIds: List<Int>,
    ): Result<TaskDto> {
        return try {
            val response = api.createTask(
                projectId = projectId,
                body = CreateTaskRequest(
                    taskName = taskName,
                    taskDescription = taskDescription,
                    dueDate = dueDate,
                    priority = priority,
                    assigneeIds = assigneeIds,
                )
            )
            Result.success(response.task)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }

    override suspend fun getTasks(
        projectId: Long?,
        priority: String?,
    ): Result<List<TaskDto>> {
        return try {
            val response = api.getTasks(
                projectId = projectId,
                priority = priority,
            )
            Result.success(response.tasks.orEmpty())
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }

    override suspend fun getProjectTasks(
        projectId: Long,
        category: String?,
        assignees: List<Long>,
        limit: Int?,
        offset: Int?,
    ): Result<List<TaskDto>> {
        return try {
            val response = api.getProjectTasks(
                projectId = projectId,
                category = category,
                assignees = assignees.takeIf { it.isNotEmpty() }?.joinToString(","),
                limit = limit,
                offset = offset,
            )
            Result.success(response.tasks.orEmpty())
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }

    override suspend fun getTaskById(
        projectId: Long,
        taskId: Long,
    ): Result<TaskDto> {
        return try {
            val response = api.getTaskById(
                projectId = projectId,
                taskId = taskId,
            )
            Result.success(response.task)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }

    override suspend fun submitForReview(
        projectId: Long,
        taskId: Long,
    ): Result<Unit> {
        return try {
            api.submitForReview(
                projectId = projectId,
                taskId = taskId,
            )
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }

    override suspend fun getMyTasks(): Result<List<TaskDto>> {
        return try {
            val response = api.getMyTasks()
            Result.success(response.tasks.orEmpty())
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }

    override suspend fun getNearestTasks(): Result<List<TaskDto>> {
        return try {
            val response = api.getNearestTasks()
            Result.success(response.tasks.orEmpty())
        } catch (e: Throwable) {
            Result.failure(parseApiError(e))
        }
    }
}