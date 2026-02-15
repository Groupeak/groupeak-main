package com.example.groupeak.service_locator

import android.content.Context
import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.data.remote.api.ApiClient
import com.example.groupeak.data.remote.repository.AuthRepositoryImpl
import com.example.groupeak.data.remote.repository.UserRepositoryImpl
import com.example.groupeak.domain.repository.AuthRepository
import com.example.groupeak.domain.repository.UserRepository

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

    fun init(context: Context) {
        if (::sessionManager.isInitialized) return
        sessionManager = SessionManager(context)
        ApiClient.init(sessionManager)
    }
}
