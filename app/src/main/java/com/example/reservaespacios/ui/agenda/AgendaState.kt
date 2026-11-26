package com.example.reservaespacios.ui.agenda

import com.example.reservaespacios.model.Reservation

/** Estado de la agenda general: reservas aprobadas que aún no ocurren, ordenadas por fecha y hora. */
data class AgendaUiState(
    val reservations: List<Reservation> = emptyList()
)
