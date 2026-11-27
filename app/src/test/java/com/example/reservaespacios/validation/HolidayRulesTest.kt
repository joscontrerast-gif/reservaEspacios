package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Holiday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HolidayRulesTest {

    private val holidays = listOf(
        Holiday(LocalDate.of(2026, 12, 8), "Inmaculada Concepción"),
        Holiday(LocalDate.of(2026, 12, 25), "Navidad")
    )

    @Test
    fun holiday_date_returns_error_with_name() {
        val error = HolidayRules.validateNotHoliday(LocalDate.of(2026, 12, 8), holidays)
        assertTrue(error!!.contains("Inmaculada Concepción"))
    }

    @Test
    fun normal_date_has_no_error() {
        assertNull(HolidayRules.validateNotHoliday(LocalDate.of(2026, 12, 9), holidays))
    }

    @Test
    fun null_date_has_no_error() {
        assertNull(HolidayRules.validateNotHoliday(null, holidays))
    }

    @Test
    fun without_loaded_holidays_nothing_is_blocked() {
        assertNull(HolidayRules.validateNotHoliday(LocalDate.of(2026, 12, 25), emptyList()))
    }

    @Test
    fun find_holiday_returns_the_matching_one() {
        assertEquals("Navidad", HolidayRules.findHoliday(LocalDate.of(2026, 12, 25), holidays)?.name)
    }
}
