package com.example.reservaespacios.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.ReservationActionResult
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.validation.ReviewValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reservationRepository: ReservationRepository = AppContainer.reservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reservationRepository.reservations.collect { reservations ->
                _uiState.update { it.copy(reservations = reservations) }
            }
        }
    }

    fun onEvent(event: ReviewEvent) {
        when (event) {
            is ReviewEvent.ApproveClicked -> approve(event.id)
            is ReviewEvent.OpenReject -> openReject(event.reservation)
            is ReviewEvent.ReasonChanged -> onReasonChanged(event.value)
            ReviewEvent.ReasonBlurred -> onReasonBlurred()
            ReviewEvent.ConfirmReject -> confirmReject()
            ReviewEvent.DismissReject -> dismissReject()
            ReviewEvent.FeedbackShown -> onFeedbackShown()
        }
    }

    private fun approve(id: Int) {
        if (_uiState.value.processingId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(processingId = id) }
            when (val result = reservationRepository.approve(id)) {
                is ReservationActionResult.Success -> {
                    _uiState.update {
                        it.copy(
                            processingId = null,
                            feedback = "Solicitud aprobada correctamente"
                        )
                    }
                }
                is ReservationActionResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            processingId = null,
                            feedback = result.message
                        )
                    }
                }
            }
        }
    }

    private fun openReject(reservation: Reservation) {
        val initialReason = ""
        val error = ReviewValidators.validateRejectionReason(initialReason)
        _uiState.update {
            it.copy(
                rejectingReservation = reservation,
                rejectReason = initialReason,
                rejectReasonTouched = false,
                rejectReasonError = error
            )
        }
    }

    private fun onReasonChanged(value: String) {
        val error = ReviewValidators.validateRejectionReason(value)
        _uiState.update {
            it.copy(
                rejectReason = value,
                rejectReasonError = error
            )
        }
    }

    private fun onReasonBlurred() {
        _uiState.update { it.copy(rejectReasonTouched = true) }
    }

    private fun confirmReject() {
        val state = _uiState.value
        val reservation = state.rejectingReservation ?: return
        if (state.processingId != null) return

        val error = ReviewValidators.validateRejectionReason(state.rejectReason)
        _uiState.update {
            it.copy(
                rejectReasonTouched = true,
                rejectReasonError = error
            )
        }

        if (error != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(processingId = reservation.id) }
            when (val result = reservationRepository.reject(reservation.id, state.rejectReason)) {
                is ReservationActionResult.Success -> {
                    _uiState.update {
                        it.copy(
                            processingId = null,
                            rejectingReservation = null,
                            rejectReason = "",
                            rejectReasonTouched = false,
                            rejectReasonError = null,
                            feedback = "Solicitud rechazada"
                        )
                    }
                }
                is ReservationActionResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            processingId = null,
                            feedback = result.message
                        )
                    }
                }
            }
        }
    }

    private fun dismissReject() {
        _uiState.update {
            it.copy(
                rejectingReservation = null,
                rejectReason = "",
                rejectReasonTouched = false,
                rejectReasonError = null
            )
        }
    }

    private fun onFeedbackShown() {
        _uiState.update { it.copy(feedback = null) }
    }
}
