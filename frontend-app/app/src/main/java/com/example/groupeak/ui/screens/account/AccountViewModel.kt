package com.example.groupeak.ui.screens.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AccountViewModel : ViewModel() {

    var uiState by mutableStateOf(AccountUiState())
        private set

    // Пока логики нет
}