package com.example.groupeak.data.remote.api

import com.example.groupeak.data.remote.dto.CreateTaskRequest
import com.example.groupeak.data.remote.dto.CreateTaskResponse
import com.example.groupeak.data.remote.dto.ListTasksResponse
import com.example.groupeak.data.remote.dto.StatusResponse
import com.example.groupeak.data.remote.dto.TaskResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApi {

    @POST("api/v1/projects/{projectId}/tasks")
    suspend fun createTask(
        @Path("projectId") projectId: Long,
        @Body body: CreateTaskRequest,
    ): CreateTaskResponse

    @GET("api/v1/tasks")
    suspend fun getTasks(
        @Query("project_id") projectId: Long? = null,
        @Query("priority") priority: String? = null,
    ): ListTasksResponse

    @GET("api/v1/projects/{projectId}/tasks")
    suspend fun getProjectTasks(
        @Path("projectId") projectId: Long,
        @Query("category") category: String? = null,
        @Query("assignees") assignees: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
    ): ListTasksResponse

    @GET("api/v1/projects/{projectId}/tasks/{taskId}")
    suspend fun getTaskById(
        @Path("projectId") projectId: Long,
        @Path("taskId") taskId: Long,
    ): TaskResponse

    @POST("api/v1/projects/{projectId}/tasks/{taskId}/submit")
    suspend fun submitForReview(
        @Path("projectId") projectId: Long,
        @Path("taskId") taskId: Long,
    ): StatusResponse

    @GET("api/v1/tasks/my")
    suspend fun getMyTasks(): ListTasksResponse

    @GET("api/v1/tasks/nearest")
    suspend fun getNearestTasks(): ListTasksResponse
}