package com.example.groupeak.ui.screens.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groupeak.data.local.SessionManager
import com.example.groupeak.domain.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    var uiState by mutableStateOf(AccountUiState())
        private set

    init {
        // Подписка на user из сессии
        viewModelScope.launch {
            sessionManager.user.collectLatest { user ->
                if (user != null) {
                    uiState = uiState.copy(
                        userName = formatNameSurname(user.fullName),
                        email = user.email,
                    )
                }
            }
        }

        // Обновляем профиль с бэка
        refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            userRepository.getProfile()
        }
    }
}

private fun formatNameSurname(fullName: String): String {
    val parts = fullName.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) return ""
    if (parts.size == 1) return parts[0]

    // 1) Убираем отчество (если оно есть) — и русское, и в транслите
    val noPatronymic = parts.filterNot { isPatronymic(it) }

    val core = when {
        noPatronymic.size >= 2 -> noPatronymic
        else -> parts // если вдруг всё выкинулось, берём оригинал
    }

    val a = core[0]
    val b = core[1]

    // 2) Пытаемся понять где фамилия. Типовой ввод RU: "Фамилия Имя ..."
    return when {
        looksLikeSurname(a) && !looksLikeSurname(b) -> "$b $a" // Фамилия Имя -> Имя Фамилия
        !looksLikeSurname(a) && looksLikeSurname(b) -> "$a $b" // Имя Фамилия -> Имя Фамилия
        else -> "$b $a" // если не смогли понять — чаще всего вводят Фамилия Имя, поэтому меняем
    }
}

private fun isPatronymic(s: String): Boolean {
    val x = s.trim().lowercase()

    // русские окончания
    val ru = x.endsWith("вич") || x.endsWith("вна") ||
            x.endsWith("ович") || x.endsWith("овна") ||
            x.endsWith("евич") || x.endsWith("евна") ||
            x.endsWith("ыныч") || x.endsWith("ич") ||
            x.endsWith("кызы") || x.endsWith("оглы")

    // транслит
    val en = x.endsWith("vich") || x.endsWith("vna") ||
            x.endsWith("ovich") || x.endsWith("ovna") ||
            x.endsWith("evich") || x.endsWith("evna") ||
            x.endsWith("ych") || x.endsWith("ich") ||
            x.endsWith("kyzy") || x.endsWith("ogly") || x.endsWith("oglu")

    return ru || en
}

private fun looksLikeSurname(s: String): Boolean {
    val x = s.trim().lowercase()

    // Частые русские фамильные окончания
    val ruEndings = listOf(
        "ов", "ев", "ин", "ын",
        "ский", "цкий", "ской",
        "ова", "ева", "ина", "ына",
        "ская", "цкая", "ская",
        "енко", "ко", "ук", "юк", "як",
        "ич"
    )

    // Частые окончания фамилий в транслите/EN
    val enEndings = listOf(
        "ov", "ev", "in",
        "sky", "ski", "skiy", "skyy", "skii",
        "enko", "ko", "uk", "yuk", "yak"
    )

    return ruEndings.any { x.endsWith(it) } || enEndings.any { x.endsWith(it) }
}
