package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import java.time.LocalDate

/**
 * Reglas de negocio sobre reservas (funciones puras, sin Android).
 * Supuesto: las reglas de cancelación todavía están por definir con el cliente.
 * Mientras tanto: se puede cancelar una solicitud pendiente o una reserva aprobada que aún no ocurrió.
 */
object ReservationRules {

    fun canCancel(reservation: Reservation, today: LocalDate): Boolean {
        val cancellable = reservation.status == ReservationStatus.PENDING ||
                reservation.status == ReservationStatus.APPROVED
        return cancellable && !reservation.date.isBefore(today)
    }

    /** Agenda general: reservas aprobadas que aún no ocurren, ordenadas por fecha y hora. */
    fun upcomingApproved(reservations: List<Reservation>, today: LocalDate): List<Reservation> =
        reservations
            .filter { it.status == ReservationStatus.APPROVED && !it.date.isBefore(today) }
            .sortedWith(compareBy({ it.date }, { it.startTime }))
}