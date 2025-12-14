package com.example.groupeak.service_locator

import com.example.groupeak.data.remote.api.ApiClient
import com.example.groupeak.data.remote.repository.AuthRepositoryImpl
import com.example.groupeak.domain.repository.AuthRepository

object ServiceLocator {
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(ApiClient.authApi)
    }
}