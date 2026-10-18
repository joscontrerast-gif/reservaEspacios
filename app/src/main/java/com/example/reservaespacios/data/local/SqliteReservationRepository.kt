package com.example.reservaespacios.data.local

import android.content.ContentValues
import androidx.core.content.contentValuesOf
import com.example.reservaespacios.data.ReservationActionResult
import com.example.reservaespacios.data.ReservationDraft
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import com.example.reservaespacios.validation.ScheduleRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Reservas guardadas en SQLite. Mantiene las mismas reglas que la versión en memoria:
 * prioridad académica y verificación de choques AL APROBAR.
 *
 * status_notified: 1 = el solicitante ya fue avisado del estado actual; 0 = falta avisarle.
 * Al aprobar/rechazar pasa a 0; la app muestra la notificación cuando el solicitante inicia sesión.
 */
class SqliteReservationRepository(private val helper: AppDatabaseHelper) : ReservationRepository {

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    override val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { _reservations.value = loadAll() }
    }

    override suspend fun submit(draft: ReservationDraft): Reservation = withContext(Dispatchers.IO) {
        val id = helper.writableDatabase.insert(
            "reservations", null,
            contentValuesOf(
                "space_id" to draft.spaceId,
                "space_name" to draft.spaceName,
                "requester_id" to draft.requesterId,
                "requester_name" to draft.requesterName,
                "activity" to draft.activity,
                "attendees" to draft.attendees,
                "date" to draft.date.toString(),
                "start_time" to draft.startTime.toString(),
                "end_time" to draft.endTime.toString(),
                "status" to ReservationStatus.PENDING.name,
                "status_notified" to 1
            )
        )
        val all = loadAll()
        _reservations.value = all
        all.first { it.id == id.toInt() }
    }

    override suspend fun approve(id: Int): ReservationActionResult = withContext(Dispatchers.IO) {
        val target = find(id) ?: return@withContext ReservationActionResult.Failure("La solicitud ya no existe")
        if (target.status != ReservationStatus.PENDING) {
            return@withContext ReservationActionResult.Failure("Esta solicitud ya fue resuelta")
        }

        // Regla 1 — prioridad académica
        val classes = helper.readableDatabase.rawQuery("SELECT * FROM academic_classes", null).use { c ->
            buildList { while (c.moveToNext()) add(c.toAcademicClass()) }
        }
        val classConflict = ScheduleRules.findClassConflict(
            spaceId = target.spaceId,
            date = target.date,
            start = target.startTime,
            end = target.endTime,
            classes = classes
        )
        if (classConflict != null) {
            return@withContext ReservationActionResult.Failure(
                "No se puede aprobar: choca con la clase ${ScheduleRules.describe(classConflict)}"
            )
        }

        // Regla 2 — disponibilidad, verificada al aprobar
        val conflict = loadAll().firstOrNull {
            it.id != target.id &&
                    it.status == ReservationStatus.APPROVED &&
                    it.spaceId == target.spaceId &&
                    it.date == target.date &&
                    target.startTime < it.endTime && it.startTime < target.endTime
        }
        if (conflict != null) {
            return@withContext ReservationActionResult.Failure(
                "No se puede aprobar: ${conflict.spaceName} ya está reservado de " +
                        "${conflict.startTime} a ${conflict.endTime} (${conflict.activity})"
            )
        }

        update(
            id,
            contentValuesOf(
                "status" to ReservationStatus.APPROVED.name,
                "rejection_reason" to null,
                "status_notified" to 0
            )
        )
        ReservationActionResult.Success
    }

    override suspend fun reject(id: Int, reason: String): ReservationActionResult = withContext(Dispatchers.IO) {
        val target = find(id) ?: return@withContext ReservationActionResult.Failure("La solicitud ya no existe")
        if (target.status != ReservationStatus.PENDING) {
            return@withContext ReservationActionResult.Failure("Esta solicitud ya fue resuelta")
        }
        update(
            id,
            contentValuesOf(
                "status" to ReservationStatus.REJECTED.name,
                "rejection_reason" to reason.trim(),
                "status_notified" to 0
            )
        )
        ReservationActionResult.Success
    }

    override suspend fun cancel(id: Int): ReservationActionResult = withContext(Dispatchers.IO) {
        val target = find(id) ?: return@withContext ReservationActionResult.Failure("La solicitud ya no existe")
        if (target.status != ReservationStatus.PENDING && target.status != ReservationStatus.APPROVED) {
            return@withContext ReservationActionResult.Failure("Esta solicitud ya no se puede cancelar")
        }
        // CANCELLED ya no cuenta como aprobada: el horario queda libre. Quien cancela ya sabe el resultado.
        update(id, contentValuesOf("status" to ReservationStatus.CANCELLED.name, "status_notified" to 1))
        ReservationActionResult.Success
    }

    override suspend fun unnotifiedFor(userId: Int): List<Reservation> = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            "SELECT * FROM reservations WHERE requester_id = ? AND status_notified = 0 ORDER BY id",
            arrayOf(userId.toString())
        ).use { c -> buildList { while (c.moveToNext()) add(c.toReservation()) } }
    }

    override suspend fun markNotified(ids: List<Int>) {
        if (ids.isEmpty()) return
        withContext(Dispatchers.IO) {
            val db = helper.writableDatabase
            db.beginTransaction()
            try {
                ids.forEach { db.update("reservations", contentValuesOf("status_notified" to 1), "id = ?", arrayOf(it.toString())) }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    override suspend fun pendingCount(): Int = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM reservations WHERE status = ?",
            arrayOf(ReservationStatus.PENDING.name)
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    // --- internos ---

    private fun loadAll(): List<Reservation> =
        helper.readableDatabase.rawQuery("SELECT * FROM reservations ORDER BY id", null).use { c ->
            buildList { while (c.moveToNext()) add(c.toReservation()) }
        }

    private fun find(id: Int): Reservation? =
        helper.readableDatabase.rawQuery("SELECT * FROM reservations WHERE id = ?", arrayOf(id.toString()))
            .use { c -> if (c.moveToFirst()) c.toReservation() else null }

    private fun update(id: Int, values: ContentValues) {
        helper.writableDatabase.update("reservations", values, "id = ?", arrayOf(id.toString()))
        _reservations.value = loadAll()
    }
}
