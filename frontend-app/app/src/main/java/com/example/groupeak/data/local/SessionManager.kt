package com.example.groupeak.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.groupeak.data.remote.dto.UserDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(
    context: Context,
) {
    private val appContext = context.applicationContext

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    init {
        // Восстанавливаем токен из DataStore
        scope.launch {
            appContext.dataStore.data
                .map { prefs -> prefs[KEY_TOKEN] }
                .distinctUntilChanged()
                .collect { storedToken ->
                    _token.value = storedToken
                }
        }
    }

    suspend fun saveToken(token: String) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
        _token.value = token
    }

    fun updateUser(user: UserDto?) {
        _user.value = user
    }

    fun clear() {
        _token.value = null
        _user.value = null
        scope.launch {
            appContext.dataStore.edit { prefs ->
                prefs.remove(KEY_TOKEN)
            }
        }
    }

    private companion object {
        val KEY_TOKEN: Preferences.Key<String> = stringPreferencesKey("jwt_token")
    }
}
