package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ReservationRulesTest {

    private val today = LocalDate.of(2026, 10, 16)

    private fun reservation(status: ReservationStatus, date: LocalDate) = Reservation(
        id = 1, spaceId = 1, spaceName = "Teatro",
        requesterId = 2, requesterName = "Docente",
        activity = "Ensayo", attendees = 10,
        date = date, startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 0),
        status = status
    )

    @Test fun pending_in_the_future_can_be_cancelled() =
        assertTrue(ReservationRules.canCancel(reservation(ReservationStatus.PENDING, today.plusDays(1)), today))

    @Test fun approved_in_the_future_can_be_cancelled() =
        assertTrue(ReservationRules.canCancel(reservation(ReservationStatus.APPROVED, today.plusDays(1)), today))

    @Test fun reservation_for_today_can_be_cancelled() =
        assertTrue(ReservationRules.canCancel(reservation(ReservationStatus.PENDING, today), today))

    @Test fun past_reservation_cannot_be_cancelled() =
        assertFalse(ReservationRules.canCancel(reservation(ReservationStatus.APPROVED, today.minusDays(1)), today))

    @Test fun rejected_cannot_be_cancelled() =
        assertFalse(ReservationRules.canCancel(reservation(ReservationStatus.REJECTED, today.plusDays(1)), today))

    @Test fun already_cancelled_cannot_be_cancelled_again() =
        assertFalse(ReservationRules.canCancel(reservation(ReservationStatus.CANCELLED, today.plusDays(1)), today))
}