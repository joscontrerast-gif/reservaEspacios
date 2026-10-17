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
}