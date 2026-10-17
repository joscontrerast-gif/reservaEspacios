package com.example.reservaespacios.data

import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime

/** Repositorio en memoria. Se pierde al cerrar la app. */
class FakeReservationRepository : ReservationRepository {

    // Datos de ejemplo: las solicitudes 1 y 2 piden el Teatro mañana y SE SOLAPAN (11:00-12:00),
    // para poder probar la regla de no aprobar dos reservas en el mismo horario.
    private val _reservations = MutableStateFlow(
        listOf(
            Reservation(
                id = 1, spaceId = 1, spaceName = "Teatro",
                requesterId = 2, requesterName = "Docente de prueba",
                activity = "Ensayo de obra", attendees = 30,
                date = LocalDate.now().plusDays(1),
                startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0)
            ),
            Reservation(
                id = 2, spaceId = 1, spaceName = "Teatro",
                requesterId = 90, requesterName = "Ana Pérez (Administrativo)",
                activity = "Charla de bienvenida", attendees = 80,
                date = LocalDate.now().plusDays(1),
                startTime = LocalTime.of(11, 0), endTime = LocalTime.of(13, 0)
            )
        )
    )
    override val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    override suspend fun submit(draft: ReservationDraft): Reservation {
        delay(600) // simula latencia
        val reservation = Reservation(
            id = (_reservations.value.maxOfOrNull { it.id } ?: 0) + 1,
            spaceId = draft.spaceId,
            spaceName = draft.spaceName,
            requesterId = draft.requesterId,
            requesterName = draft.requesterName,
            activity = draft.activity,
            attendees = draft.attendees,
            date = draft.date,
            startTime = draft.startTime,
            endTime = draft.endTime
        )
        _reservations.update { it + reservation }
        return reservation
    }

    override suspend fun approve(id: Int): ReservationActionResult {
        delay(500)
        val current = _reservations.value
        val target = current.firstOrNull { it.id == id }
            ?: return ReservationActionResult.Failure("La solicitud ya no existe")
        if (target.status != ReservationStatus.PENDING) {
            return ReservationActionResult.Failure("Esta solicitud ya fue resuelta")
        }

        // Regla de negocio: se verifica la disponibilidad AL APROBAR, no solo al solicitar.
        val conflict = current.firstOrNull {
            it.id != target.id &&
                    it.status == ReservationStatus.APPROVED &&
                    it.spaceId == target.spaceId &&
                    it.date == target.date &&
                    target.startTime < it.endTime && it.startTime < target.endTime
        }
        if (conflict != null) {
            return ReservationActionResult.Failure(
                "No se puede aprobar: ${conflict.spaceName} ya está reservado de " +
                        "${conflict.startTime} a ${conflict.endTime} (${conflict.activity})"
            )
        }

        replace(id) { it.copy(status = ReservationStatus.APPROVED, rejectionReason = null) }
        return ReservationActionResult.Success
    }

    override suspend fun reject(id: Int, reason: String): ReservationActionResult {
        delay(500)
        val target = _reservations.value.firstOrNull { it.id == id }
            ?: return ReservationActionResult.Failure("La solicitud ya no existe")
        if (target.status != ReservationStatus.PENDING) {
            return ReservationActionResult.Failure("Esta solicitud ya fue resuelta")
        }
        replace(id) { it.copy(status = ReservationStatus.REJECTED, rejectionReason = reason.trim()) }
        return ReservationActionResult.Success
    }

    override suspend fun cancel(id: Int): ReservationActionResult {
        delay(500)
        val target = _reservations.value.firstOrNull { it.id == id }
            ?: return ReservationActionResult.Failure("La solicitud ya no existe")
        if (target.status != ReservationStatus.PENDING && target.status != ReservationStatus.APPROVED) {
            return ReservationActionResult.Failure("Esta solicitud ya no se puede cancelar")
        }
        // Al pasar a CANCELLED deja de contar como "aprobada", así que el horario queda libre.
        replace(id) { it.copy(status = ReservationStatus.CANCELLED) }
        return ReservationActionResult.Success
    }

    private fun replace(id: Int, transform: (Reservation) -> Reservation) {
        _reservations.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }
}