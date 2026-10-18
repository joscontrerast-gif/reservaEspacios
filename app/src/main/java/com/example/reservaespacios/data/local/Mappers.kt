package com.example.reservaespacios.data.local

import android.database.Cursor
import com.example.reservaespacios.model.AcademicClass
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

private fun Cursor.int(name: String) = getInt(getColumnIndexOrThrow(name))
private fun Cursor.str(name: String) = getString(getColumnIndexOrThrow(name))
private fun Cursor.strOrNull(name: String): String? {
    val i = getColumnIndexOrThrow(name)
    return if (isNull(i)) null else getString(i)
}

/** La contraseña nunca sale de la base de datos: el modelo recibe un texto vacío. */
internal fun Cursor.toUser() = User(
    id = int("id"),
    name = str("name"),
    email = str("email"),
    password = "",
    profile = UserProfile.valueOf(str("profile"))
)

internal fun Cursor.toAcademicClass() = AcademicClass(
    id = int("id"),
    subject = str("subject"),
    teacher = str("teacher"),
    spaceId = int("space_id"),
    spaceName = str("space_name"),
    dayOfWeek = DayOfWeek.of(int("day_of_week")),
    startTime = LocalTime.parse(str("start_time")),
    endTime = LocalTime.parse(str("end_time")),
    students = int("students")
)

internal fun Cursor.toReservation() = Reservation(
    id = int("id"),
    spaceId = int("space_id"),
    spaceName = str("space_name"),
    requesterId = int("requester_id"),
    requesterName = str("requester_name"),
    activity = str("activity"),
    attendees = int("attendees"),
    date = LocalDate.parse(str("date")),
    startTime = LocalTime.parse(str("start_time")),
    endTime = LocalTime.parse(str("end_time")),
    status = ReservationStatus.valueOf(str("status")),
    rejectionReason = strOrNull("rejection_reason")
)
