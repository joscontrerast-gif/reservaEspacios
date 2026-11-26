package com.example.reservaespacios.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.validation.ReservationRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Agenda general (solo lectura). Se actualiza sola cuando el administrador aprueba o se cancela una reserva. */
class AgendaViewModel(
    private val reservationRepository: ReservationRepository = AppContainer.reservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reservationRepository.reservations.collect { all ->
                _uiState.value = AgendaUiState(ReservationRules.upcomingApproved(all, LocalDate.now()))
            }
        }
    }
}
