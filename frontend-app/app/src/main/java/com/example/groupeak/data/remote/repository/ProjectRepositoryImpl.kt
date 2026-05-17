package com.example.groupeak.data.remote.repository

import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.data.remote.api.ProjectApi
import com.example.groupeak.data.remote.dto.CreateProjectRequest
import com.example.groupeak.data.remote.dto.ProjectDto
import com.example.groupeak.data.remote.dto.ProjectMemberDto
import com.example.groupeak.data.remote.errors.parseApiError
import com.example.groupeak.domain.repository.ProjectRepository

class ProjectRepositoryImpl(
    private val api: ProjectApi,
    private val sessionManager: SessionManager,
) : ProjectRepository {

    override suspend fun createProject(
        name: String,
        description: String?,
        deadlineAt: String?,
    ): Result<ProjectDto> {
        return try {
            val response = api.createProject(
                CreateProjectRequest(
                    name = name,
                    description = description,
                    deadlineAt = deadlineAt,
                )
            )
            Result.success(response.project)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }

    override suspend fun getProjects(): Result<List<ProjectDto>> {
        return try {
            val response = api.getProjects()
            Result.success(response.projects.orEmpty())
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }

    override suspend fun getProjectMembers(projectId: Long): Result<List<ProjectMemberDto>> {
        return try {
            val response = api.getProjectMembers(projectId)
            Result.success(response.members)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }

    override suspend fun inviteMember(
        projectId: Long,
        request: com.example.groupeak.data.remote.dto.InviteMemberRequest
    ): Result<Unit> {
        return try {
            val response = api.inviteMember(projectId, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("API Error ${response.code()}: $errorBody"))
            }
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }

    override suspend fun getProjectById(projectId: Long): Result<com.example.groupeak.data.remote.dto.ProjectDetailsDto> {
        return try {
            val response = api.getProjectById(projectId)
            Result.success(response.project)
        } catch (e: Throwable) {
            Result.failure(parseApiError(e) {
                sessionManager.clear()
            })
        }
    }
}