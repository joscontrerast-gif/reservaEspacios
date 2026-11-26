package com.example.reservaespacios.data.remote

import com.example.reservaespacios.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

// --- Auth DTOs ---
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val profile: String)
data class RecoverPasswordRequest(val email: String, val newPassword: String)
data class UpdateProfileRequest(val name: String, val password: String? = null)
data class MessageResponse(val message: String)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val password: String = "",
    val profile: String
) {
    fun toDomain() = User(id, name, email, "", UserProfile.valueOf(profile))
}

// --- Spaces & Schedules DTOs ---
data class SpaceDto(
    val id: Int = 0,
    val code: String,
    val name: String,
    val capacity: Int,
    val location: String = "",
    val equipment: List<Equipment> = emptyList(),
    val photoPath: String? = null
) {
    fun toDomain() = Space(id, name, capacity, code, location, equipment, photoPath)
}

data class AcademicClassDto(
    val id: Int = 0,
    val subject: String,
    val teacher: String,
    val spaceId: Int,
    val spaceName: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val students: Int
) {
    fun toDomain() = AcademicClass(
        id = id,
        subject = subject,
        teacher = teacher,
        spaceId = spaceId,
        spaceName = spaceName,
        dayOfWeek = DayOfWeek.of(dayOfWeek),
        startTime = LocalTime.parse(startTime),
        endTime = LocalTime.parse(endTime),
        students = students
    )
}

// --- Reservations DTOs ---
data class ReservationDto(
    val id: Int = 0,
    val spaceId: Int,
    val spaceName: String,
    val requesterId: Int,
    val requesterName: String,
    val activity: String,
    val attendees: Int,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String = "PENDING",
    val rejectionReason: String? = null
) {
    fun toDomain() = Reservation(
        id = id,
        spaceId = spaceId,
        spaceName = spaceName,
        requesterId = requesterId,
        requesterName = requesterName,
        activity = activity,
        attendees = attendees,
        date = LocalDate.parse(date),
        startTime = LocalTime.parse(startTime),
        endTime = LocalTime.parse(endTime),
        status = ReservationStatus.valueOf(status),
        rejectionReason = rejectionReason
    )
}

data class RejectRequest(val reason: String)

// --- API Externa (Feriados Públicos Chile) ---
data class PublicHolidayDto(
    val date: String,
    val localName: String,
    val name: String
)