package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Space
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek

class ScheduleValidatorsTest {

    private val room = Space(id = 1, name = "Taller", capacity = 30)

    @Test fun missing_space_is_invalid() = assertNotNull(ScheduleValidators.validateSpace(null))
    @Test fun missing_day_is_invalid() = assertNotNull(ScheduleValidators.validateDay(null))
    @Test fun selected_day_is_valid() = assertNull(ScheduleValidators.validateDay(DayOfWeek.FRIDAY))

    @Test fun blank_subject_is_invalid() = assertNotNull(ScheduleValidators.validateSubject("  "))
    @Test fun valid_subject_passes() = assertNull(ScheduleValidators.validateSubject("Diseño Gráfico I"))

    @Test fun blank_teacher_is_invalid() = assertNotNull(ScheduleValidators.validateTeacher(""))
    @Test fun valid_teacher_passes() = assertNull(ScheduleValidators.validateTeacher("Prof. Rojas"))

    @Test fun students_over_capacity_is_invalid() =
        assertEquals("Supera la capacidad del espacio (30)", ScheduleValidators.validateStudents("31", room))

    @Test fun students_within_capacity_passes() = assertNull(ScheduleValidators.validateStudents("30", room))
    @Test fun students_zero_is_invalid() = assertNotNull(ScheduleValidators.validateStudents("0", room))
    @Test fun students_text_is_invalid() =
        assertEquals("Ingresa solo números", ScheduleValidators.validateStudents("muchos", room))
}