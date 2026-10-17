package com.example.reservaespacios.ui.myreservations

import com.example.reservaespacios.model.Reservation

data class MyReservationsUiState(
    val reservations: List<Reservation> = emptyList(),
    val cancelling: Reservation? = null, // solicitud para la que se pide confirmación
    val processingId: Int? = null,
    val feedback: String? = null
)

sealed interface MyReservationsEvent {
    data class CancelClicked(val reservation: Reservation) : MyReservationsEvent
    data object ConfirmCancel : MyReservationsEvent
    data object DismissCancel : MyReservationsEvent
    data object FeedbackShown : MyReservationsEvent
}