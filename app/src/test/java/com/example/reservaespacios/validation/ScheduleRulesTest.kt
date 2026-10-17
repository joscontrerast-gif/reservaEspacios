package com.example.reservaespacios.validation

import com.example.reservaespacios.model.AcademicClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ScheduleRulesTest {

    // 19/10/2026 es lunes
    private val monday = LocalDate.of(2026, 10, 19)

    private val mondayClass = AcademicClass(
        id = 1, subject = "Diseño", teacher = "Prof. Rojas",
        spaceId = 3, spaceName = "Taller",
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(8, 30), endTime = LocalTime.of(10, 0), students = 20
    )

    @Test fun overlapping_ranges_are_detected() =
        assertTrue(ScheduleRules.timesOverlap(LocalTime.of(9, 0), LocalTime.of(11, 0), LocalTime.of(10, 0), LocalTime.of(12, 0)))

    @Test fun back_to_back_ranges_do_not_overlap() =
        assertFalse(ScheduleRules.timesOverlap(LocalTime.of(8, 0), LocalTime.of(10, 0), LocalTime.of(10, 0), LocalTime.of(11, 0)))

    @Test fun reservation_on_the_same_weekday_and_space_conflicts() {
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
        val conflict = ScheduleRules.findClassConflict(
            3, monday, LocalTime.of(9, 0), LocalTime.of(10, 30), listOf(mondayClass)
        )
        assertNotNull(conflict)
    }

    @Test fun reservation_on_another_weekday_is_free() =
        assertNull(
            ScheduleRules.findClassConflict(3, monday.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), listOf(mondayClass))
        )

    @Test fun reservation_in_another_space_is_free() =
        assertNull(
            ScheduleRules.findClassConflict(1, monday, LocalTime.of(9, 0), LocalTime.of(10, 0), listOf(mondayClass))
        )

    @Test fun reservation_right_after_the_class_is_free() =
        assertNull(
            ScheduleRules.findClassConflict(3, monday, LocalTime.of(10, 0), LocalTime.of(11, 0), listOf(mondayClass))
        )

    @Test fun description_names_subject_day_and_time() =
        assertEquals("\"Diseño\" (Lunes 08:30–10:00)", ScheduleRules.describe(mondayClass))
}