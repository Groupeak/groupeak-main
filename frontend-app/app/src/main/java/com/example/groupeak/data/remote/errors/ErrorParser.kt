package com.example.groupeak.data.remote.errors

import org.json.JSONObject
import retrofit2.HttpException

fun parseApiError(
    throwable: Throwable,
    onUnauthorized: (() -> Unit)? = null,
): Throwable {
    if (throwable !is HttpException) return throwable

    if (throwable.code() == 401) {
        onUnauthorized?.invoke()
        return Exception("Сессия истекла. Войдите снова.")
    }

    return try {
        val errorBody = throwable.response()?.errorBody()?.string()

        if (errorBody.isNullOrEmpty()) {
            return Exception("Ошибка сервера (${throwable.code()})")
        }

        try {
            val json = JSONObject(errorBody)
            val message = json.optString("error")
                .ifEmpty { json.optString("message") }
                .ifEmpty { "Ошибка сервера (${throwable.code()})" }
            Exception(message)
        } catch (_: Exception) {
            val trimmed = errorBody.trim()
            if (trimmed.isNotEmpty()) {
                Exception(trimmed)
            } else {
                Exception("Ошибка сервера (${throwable.code()})")
            }
        }
    } catch (_: Exception) {
        Exception("Ошибка сервера (${throwable.code()})")
    }
}