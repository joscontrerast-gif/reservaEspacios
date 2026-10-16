package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Space
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ReservationValidatorsTest {

    private val today = LocalDate.of(2026, 10, 16)
    private val room = Space(id = 1, name = "Teatro", capacity = 100)

    @Test fun attendees_within_capacity_is_valid() =
        assertNull(ReservationValidators.validateAttendees("50", room))

    @Test fun attendees_over_capacity_is_invalid() =
        assertEquals(
            "Supera la capacidad del espacio (100)",
            ReservationValidators.validateAttendees("101", room)
        )

    @Test fun attendees_zero_is_invalid() =
        assertNotNull(ReservationValidators.validateAttendees("0", room))

    @Test fun past_date_is_invalid() =
        assertNotNull(ReservationValidators.validateDate("15/10/2026", today))

    @Test fun impossible_date_is_invalid() =
        assertNotNull(ReservationValidators.validateDate("31/02/2026", today))

    @Test fun today_is_valid() =
        assertNull(ReservationValidators.validateDate("16/10/2026", today))

    @Test fun end_before_start_is_invalid() =
        assertNotNull(ReservationValidators.validateEndTime("10:00", "09:30"))

    @Test fun end_after_start_is_valid() =
        assertNull(ReservationValidators.validateEndTime("10:00", "11:30"))
}