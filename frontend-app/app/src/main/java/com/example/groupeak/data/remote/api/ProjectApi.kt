package com.example.groupeak.data.remote.api

import com.example.groupeak.data.remote.dto.CreateProjectRequest
import com.example.groupeak.data.remote.dto.CreateProjectResponse
import com.example.groupeak.data.remote.dto.ListProjectMembersResponse
import com.example.groupeak.data.remote.dto.ListProjectsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProjectApi {

    @POST("api/v1/projects/")
    suspend fun createProject(
        @Body body: CreateProjectRequest,
    ): CreateProjectResponse

    @GET("api/v1/projects/")
    suspend fun getProjects(): ListProjectsResponse

    @GET("api/v1/projects/{projectId}/members")
    suspend fun getProjectMembers(
        @Path("projectId") projectId: Long,
    ): ListProjectMembersResponse
}