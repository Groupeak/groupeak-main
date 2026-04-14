package com.example.groupeak.service_locator

import android.content.Context
import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.data.remote.api.ApiClient
import com.example.groupeak.data.remote.repository.AuthRepositoryImpl
import com.example.groupeak.data.remote.repository.ProjectRepositoryImpl
import com.example.groupeak.data.remote.repository.TaskRepositoryImpl
import com.example.groupeak.data.remote.repository.UserRepositoryImpl
import com.example.groupeak.data.remote.repository.EventRepositoryImpl
import com.example.groupeak.domain.repository.AuthRepository
import com.example.groupeak.domain.repository.ProjectRepository
import com.example.groupeak.domain.repository.TaskRepository
import com.example.groupeak.domain.repository.UserRepository
import com.example.groupeak.domain.repository.EventRepository

object ServiceLocator {

    lateinit var sessionManager: SessionManager
        private set

    val authRepository: AuthRepository by lazy {
        ApiClient.checkInitialized()
        AuthRepositoryImpl(ApiClient.authApi, sessionManager)
    }

    val userRepository: UserRepository by lazy {
        ApiClient.checkInitialized()
        UserRepositoryImpl(ApiClient.userApi, sessionManager)
    }

    val projectRepository: ProjectRepository by lazy {
        ApiClient.checkInitialized()
        ProjectRepositoryImpl(ApiClient.projectApi, sessionManager)
    }

    val taskRepository: TaskRepository by lazy {
        ApiClient.checkInitialized()
        TaskRepositoryImpl(ApiClient.taskApi)
    }

    val eventRepository: EventRepository by lazy {
        ApiClient.checkInitialized()
        EventRepositoryImpl(ApiClient.eventApi)
    }

    fun init(context: Context) {
        if (::sessionManager.isInitialized) return
        sessionManager = SessionManager(context)
        ApiClient.init(sessionManager)
    }
}