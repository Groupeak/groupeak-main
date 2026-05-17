package com.example.groupeak.domain.repository

import com.example.groupeak.data.remote.dto.ProjectDto
import com.example.groupeak.data.remote.dto.ProjectMemberDto

interface ProjectRepository {

    suspend fun createProject(
        name: String,
        description: String?,
        deadlineAt: String?,
    ): Result<ProjectDto>

    suspend fun getProjects(): Result<List<ProjectDto>>

    suspend fun getProjectMembers(projectId: Long): Result<List<ProjectMemberDto>>
    
    suspend fun inviteMember(projectId: Long, request: com.example.groupeak.data.remote.dto.InviteMemberRequest): Result<Unit>

    suspend fun getProjectById(projectId: Long): Result<com.example.groupeak.data.remote.dto.ProjectDetailsDto>
}