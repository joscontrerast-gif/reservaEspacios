package com.example.reservaespacios.ui.holidays

import com.example.reservaespacios.model.Holiday

data class HolidaysUiState(
    val isLoading: Boolean = false,
    val holidays: List<Holiday> = emptyList(),
    val error: String? = null
) {
    val nextHoliday: Holiday? get() = holidays.firstOrNull()
    val otherHolidays: List<Holiday> get() = holidays.drop(1)
}

sealed interface HolidaysEvent {
    data object Retry : HolidaysEvent
}
