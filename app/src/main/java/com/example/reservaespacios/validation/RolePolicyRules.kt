package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import com.example.reservaespacios.model.RolePolicy
import com.example.reservaespacios.util.DateTimeFormats
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

/**
 * Reglas que dependen del perfil del usuario (funciones puras, sin Android).
 * Todas devuelven el mensaje de error o `null` si la solicitud cumple. Si no hay política
 * (no hay sesión), no restringen nada.
 */
object RolePolicyRules {

    /** Solicitudes del usuario que siguen "vivas": pendientes o aprobadas que aún no ocurren. */
    fun countActive(reservations: List<Reservation>, userId: Int, today: LocalDate): Int =
        reservations.count {
            it.requesterId == userId &&
                (it.status == ReservationStatus.PENDING || it.status == ReservationStatus.APPROVED) &&
                !it.date.isBefore(today)
        }

    fun validateDate(policy: RolePolicy?, date: LocalDate, today: LocalDate): String? {
        if (policy == null) return null
        val earliest = today.plusDays(policy.minAdvanceDays.toLong())
        if (date.isBefore(earliest)) {
            return "Como ${policy.profile.label} debes solicitar con al menos " +
                "${policy.minAdvanceDays} día(s) de anticipación"
        }
        val maxDays = policy.maxAdvanceDays ?: return null
        val latest = today.plusDays(maxDays.toLong())
        return if (date.isAfter(latest)) {
            "Como ${policy.profile.label} puedes reservar hasta $maxDays días antes " +
                "(máx. ${DateTimeFormats.formatDate(latest)})"
        } else null
    }

    fun validateDuration(policy: RolePolicy?, start: LocalTime, end: LocalTime): String? {
        if (policy == null) return null
        val max = policy.maxDurationMinutes ?: return null
        val minutes = Duration.between(start, end).toMinutes()
        return if (minutes > max) {
            "Como ${policy.profile.label} puedes reservar hasta ${RolePolicy.formatDuration(max)}"
        } else null
    }

    fun validateActiveRequests(policy: RolePolicy?, activeCount: Int): String? {
        if (policy == null) return null
        val max = policy.maxActiveRequests ?: return null
        return if (activeCount >= max) {
            "Ya tienes $activeCount solicitudes activas (máximo para ${policy.profile.label}: $max). " +
                "Cancela una o espera a que se resuelva."
        } else null
    }
}
