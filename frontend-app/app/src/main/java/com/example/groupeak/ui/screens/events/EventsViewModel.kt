package com.example.groupeak.ui.screens.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EventsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        _uiState.update {
            it.copy(events = EventsData.allEvents)
        }
    }

    fun selectEvent(event: EventUiModel) {
        _uiState.update {
            it.copy(selectedEvent = event)
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(selectedEvent = null)
        }
    }
}