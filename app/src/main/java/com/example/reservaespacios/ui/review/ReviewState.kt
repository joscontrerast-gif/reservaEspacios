package com.example.reservaespacios.ui.review

import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus

data class ReviewUiState(
    val reservations: List<Reservation> = emptyList(),
    // Formulario de rechazo (diálogo)
    val rejectingReservation: Reservation? = null,
    val rejectReason: String = "",
    val rejectReasonTouched: Boolean = false,
    val rejectReasonError: String? = null,
    // Estado general
    val processingId: Int? = null,
    val feedback: String? = null
) {
    val pending: List<Reservation>
        get() = reservations
            .filter { it.status == ReservationStatus.PENDING }
            .sortedWith(compareBy({ it.date }, { it.startTime }))

    val resolved: List<Reservation>
        get() = reservations
            .filter { it.status != ReservationStatus.PENDING }
            .sortedByDescending { it.id }

    /** El error solo se muestra cuando el usuario ya interactuó con el campo. */
    val visibleReasonError: String? get() = if (rejectReasonTouched) rejectReasonError else null
}

sealed interface ReviewEvent {
    data class ApproveClicked(val id: Int) : ReviewEvent
    data class OpenReject(val reservation: Reservation) : ReviewEvent
    data class ReasonChanged(val value: String) : ReviewEvent
    data object ReasonBlurred : ReviewEvent
    data object ConfirmReject : ReviewEvent
    data object DismissReject : ReviewEvent
    data object FeedbackShown : ReviewEvent
}