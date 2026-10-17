package com.example.reservaespacios.model

import java.time.LocalDate
import java.time.LocalTime

enum class ReservationStatus(val label: String) {
    PENDING("Pendiente"),
    APPROVED("Aprobada"),
    REJECTED("Rechazada")
}

data class Reservation(
    val id: Int,
    val spaceId: Int,
    val spaceName: String,
    val requesterId: Int,
    val requesterName: String,
    val activity: String,
    val attendees: Int,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: ReservationStatus = ReservationStatus.PENDING,
    val rejectionReason: String? = null
)