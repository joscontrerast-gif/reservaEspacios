package com.example.reservaespacios.ui.holidays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.HolidayRepository
import com.example.reservaespacios.data.HolidayResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HolidaysViewModel(
    private val holidayRepository: HolidayRepository = AppContainer.holidayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HolidaysUiState())
    val uiState: StateFlow<HolidaysUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: HolidaysEvent) {
        when (event) {
            HolidaysEvent.Retry -> load()
        }
    }

    private fun load() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = holidayRepository.getUpcomingHolidays()) {
                is HolidayResult.Success -> _uiState.update {
                    it.copy(isLoading = false, holidays = result.holidays)
                }
                is HolidayResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
