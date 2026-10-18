package com.example.reservaespacios.data

import com.example.reservaespacios.model.Reservation
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime

/** Datos que envía quien solicita; el repositorio les asigna id y estado "pendiente". */
data class ReservationDraft(
    val spaceId: Int,
    val spaceName: String,
    val requesterId: Int,
    val requesterName: String,
    val activity: String,
    val attendees: Int,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)

sealed interface ReservationActionResult {
    data object Success : ReservationActionResult
    data class Failure(val message: String) : ReservationActionResult
}

/** Contrato para las reservas. Implementaciones: FakeReservationRepository (pruebas) y SqliteReservationRepository (app real). */
interface ReservationRepository {
    val reservations: StateFlow<List<Reservation>>

    suspend fun submit(draft: ReservationDraft): Reservation

    /** Aprueba una solicitud pendiente. Falla si el horario choca con una reserva ya aprobada. */
    suspend fun approve(id: Int): ReservationActionResult

    suspend fun reject(id: Int, reason: String): ReservationActionResult

    /** Cancela una solicitud pendiente o una reserva aprobada. Si estaba aprobada, libera el horario. */
    suspend fun cancel(id: Int): ReservationActionResult

    /** Reservas de este usuario cuyo cambio de estado (aprobada/rechazada) aún no se le notificó. */
    suspend fun unnotifiedFor(userId: Int): List<Reservation> = emptyList()

    /** Marca esas reservas como ya notificadas. */
    suspend fun markNotified(ids: List<Int>) {}

    /** Cantidad de solicitudes pendientes de revisión (para avisar al administrador). */
    suspend fun pendingCount(): Int = 0
}