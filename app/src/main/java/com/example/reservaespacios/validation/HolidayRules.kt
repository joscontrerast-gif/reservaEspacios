package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Holiday
import java.time.LocalDate

object HolidayRules {

    fun findHoliday(date: LocalDate, holidays: List<Holiday>): Holiday? =
        holidays.firstOrNull { it.date == date }

    fun validateNotHoliday(date: LocalDate?, holidays: List<Holiday>): String? {
        if (date == null) return null
        val holiday = findHoliday(date, holidays) ?: return null
        return "Es feriado (${holiday.name}): el instituto está cerrado"
    }
}
