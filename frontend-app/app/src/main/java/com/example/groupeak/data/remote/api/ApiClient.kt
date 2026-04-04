package com.example.groupeak.data.remote.api

import com.example.groupeak.data.local.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:3030/"

    private var isInitialized = false

    lateinit var authApi: AuthApi
        private set

    lateinit var userApi: UserApi
        private set

    lateinit var projectApi: ProjectApi
        private set

    lateinit var taskApi: TaskApi
        private set

    fun init(sessionManager: SessionManager) {
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        userApi = retrofit.create(UserApi::class.java)
        projectApi = retrofit.create(ProjectApi::class.java)
        taskApi = retrofit.create(TaskApi::class.java)

        isInitialized = true
    }

    fun checkInitialized() {
        check(isInitialized) {
            "ApiClient is not initialized. Call ServiceLocator.init(context) in MainActivity before using it."
        }
    }
}