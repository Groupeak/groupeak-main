package com.example.groupeak.data.remote.api

import com.example.groupeak.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.token.value

        val original = chain.request()
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
