package com.example.reservaespacios.ui.myreservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.ReservationActionResult
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.validation.ReservationRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MyReservationsViewModel(
    private val reservationRepository: ReservationRepository = AppContainer.reservationRepository,
    private val sessionStore: SessionStore = AppContainer.sessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState.asStateFlow()

    init {
        // Combina DOS fuentes de estado: todas las reservas + quién tiene la sesión iniciada.
        // Cuando el administrador aprueba o rechaza, esta lista se actualiza sola.
        viewModelScope.launch {
            combine(reservationRepository.reservations, sessionStore.currentUser) { all, user ->
                if (user == null) emptyList()
                else all.filter { it.requesterId == user.id }.sortedByDescending { it.id }
            }.collect { mine ->
                _uiState.update { it.copy(reservations = mine) }
            }
        }
    }

    fun onEvent(event: MyReservationsEvent) {
        when (event) {
            is MyReservationsEvent.CancelClicked -> {
                if (ReservationRules.canCancel(event.reservation, LocalDate.now())) {
                    _uiState.update { it.copy(cancelling = event.reservation) }
                } else {
                    _uiState.update { it.copy(feedback = "Esta solicitud ya no se puede cancelar.") }
                }
            }
            MyReservationsEvent.ConfirmCancel -> confirmCancel()
            MyReservationsEvent.DismissCancel -> _uiState.update { it.copy(cancelling = null) }
            MyReservationsEvent.FeedbackShown -> _uiState.update { it.copy(feedback = null) }
        }
    }

    private fun confirmCancel() {
        val target = _uiState.value.cancelling ?: return
        if (_uiState.value.processingId != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(processingId = target.id) }
            val message = when (val result = reservationRepository.cancel(target.id)) {
                ReservationActionResult.Success -> "Solicitud cancelada."
                is ReservationActionResult.Failure -> result.message
            }
            _uiState.update { it.copy(processingId = null, cancelling = null, feedback = message) }
        }
    }
}