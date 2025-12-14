package com.example.groupeak.data.remote.errors

import org.json.JSONObject
import retrofit2.HttpException

fun parseApiError(throwable: Throwable): Throwable {
    if (throwable !is HttpException) return throwable

    return try {
        val errorBody = throwable.response()?.errorBody()?.string()

        val message = if (!errorBody.isNullOrEmpty()) {
            val json = JSONObject(errorBody)
            json.optString("error")
                .ifEmpty { json.optString("message") }
                .ifEmpty { "Ошибка сервера (${throwable.code()})" }
        } else {
            "Ошибка сервера (${throwable.code()})"
        }

        Exception(message)
    } catch (e: Exception) {
        Exception("Ошибка сервера (${throwable.code()})")
    }
}
