package com.example.reservaespacios.util

import com.example.reservaespacios.validation.ReservationValidators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class DateTimeFormatsTest {

    @Test fun formats_date_with_leading_zeros() =
        assertEquals("05/10/2026", DateTimeFormats.formatDate(LocalDate.of(2026, 10, 5)))

    @Test fun formats_time_in_24_hours() {
        assertEquals("09:05", DateTimeFormats.formatTime(LocalTime.of(9, 5)))
        assertEquals("18:30", DateTimeFormats.formatTime(LocalTime.of(18, 30)))
    }

    @Test fun parses_valid_date_and_rejects_impossible_ones() {
        assertEquals(LocalDate.of(2026, 10, 5), DateTimeFormats.parseDate("05/10/2026"))
        assertNull(DateTimeFormats.parseDate("31/02/2026"))
    }

    @Test fun parses_valid_time_and_rejects_invalid_ones() {
        assertEquals(LocalTime.of(9, 5), DateTimeFormats.parseTime("09:05"))
        assertNull(DateTimeFormats.parseTime("25:00"))
    }

    @Test fun picker_millis_round_trip_keeps_the_same_day() {
        val date = LocalDate.of(2026, 12, 31)
        assertEquals(date, DateTimeFormats.pickerMillisToDate(DateTimeFormats.dateToPickerMillis(date)))
    }

    @Test fun picker_output_always_satisfies_the_existing_validators() {
        val today = LocalDate.of(2026, 10, 16)
        val pickedDate = DateTimeFormats.formatDate(today.plusDays(3))
        assertNull(ReservationValidators.validateDate(pickedDate, today))

        val start = DateTimeFormats.formatTime(LocalTime.of(10, 0))
        val end = DateTimeFormats.formatTime(LocalTime.of(11, 30))
        assertNull(ReservationValidators.validateStartTime(start))
        assertNull(ReservationValidators.validateEndTime(start, end))
    }
}