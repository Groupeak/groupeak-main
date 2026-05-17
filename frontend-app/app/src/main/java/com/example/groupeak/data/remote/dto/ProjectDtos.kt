package com.example.groupeak.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateProjectRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("deadline_at") val deadlineAt: String? = null,
)

data class ProjectDto(
    @SerializedName("id") val id: Long,
    @SerializedName("owner_id") val ownerId: Long,
    @SerializedName("project_name") val projectName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("deadline_at") val deadlineAt: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
)

data class CreateProjectResponse(
    @SerializedName("project") val project: ProjectDto,
)

data class ListProjectsResponse(
    @SerializedName("projects") val projects: List<ProjectDto>?,
)

data class ProjectMemberDto(
    @SerializedName("id") val id: Long,
    @SerializedName("project_id") val projectId: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("role") val role: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("position") val position: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
)

data class ListProjectMembersResponse(
    @SerializedName("members") val members: List<ProjectMemberDto>,
)

data class ProjectDetailsDto(
    @SerializedName("id") val id: Long,
    @SerializedName("owner_id") val ownerId: Long,
    @SerializedName("project_name") val projectName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("deadline_at") val deadlineAt: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("member_count") val memberCount: Int,
    @SerializedName("task_count") val taskCount: Int,
)

data class GetProjectResponse(
    @SerializedName("project") val project: ProjectDetailsDto,
)

data class InviteMemberRequest(
    @SerializedName("email") val email: String
)