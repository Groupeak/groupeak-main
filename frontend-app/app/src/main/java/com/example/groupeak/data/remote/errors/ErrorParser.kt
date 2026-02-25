package com.example.groupeak.data.remote.errors

import org.json.JSONObject
import retrofit2.HttpException

fun parseApiError(throwable: Throwable): Throwable {
    if (throwable !is HttpException) return throwable

    return try {
        val errorBody = throwable.response()?.errorBody()?.string()

        if (errorBody.isNullOrEmpty()) {
            return Exception("Ошибка сервера (${throwable.code()})")
        }

        // 1) Пытаемся распарсить JSON {error:"..."} / {message:"..."}
        try {
            val json = JSONObject(errorBody)
            val message = json.optString("error")
                .ifEmpty { json.optString("message") }
                .ifEmpty { "Ошибка сервера (${throwable.code()})" }
            Exception(message)
        } catch (_: Exception) {
            // 2) Если тело не JSON (например, http.Error на бэке) — показываем как текст
            val trimmed = errorBody.trim()
            if (trimmed.isNotEmpty()) Exception(trimmed) else Exception("Ошибка сервера (${throwable.code()})")
        }
    } catch (_: Exception) {
        Exception("Ошибка сервера (${throwable.code()})")
    }
}
