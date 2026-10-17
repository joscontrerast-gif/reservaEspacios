package com.example.reservaespacios.validation

import com.example.reservaespacios.model.AcademicClass
import com.example.reservaespacios.model.labelEs
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** Reglas de negocio sobre horarios académicos (funciones puras, sin Android). */
object ScheduleRules {

    /** Dos rangos se solapan si cada uno empieza antes de que el otro termine. */
    fun timesOverlap(aStart: LocalTime, aEnd: LocalTime, bStart: LocalTime, bEnd: LocalTime): Boolean =
        aStart < bEnd && bStart < aEnd

    /** Clase que ocupa el espacio el mismo día de la semana y se solapa con un rango dado, si existe. */
    fun findOverlappingClass(
        spaceId: Int,
        day: DayOfWeek,
        start: LocalTime,
        end: LocalTime,
        classes: List<AcademicClass>
    ): AcademicClass? = classes.firstOrNull {
        it.spaceId == spaceId && it.dayOfWeek == day && timesOverlap(start, end, it.startTime, it.endTime)
    }

    /** Prioridad académica: clase que bloquea una reserva en una fecha concreta, si existe. */
    fun findClassConflict(
        spaceId: Int,
        date: LocalDate,
        start: LocalTime,
        end: LocalTime,
        classes: List<AcademicClass>
    ): AcademicClass? = findOverlappingClass(spaceId, date.dayOfWeek, start, end, classes)

    fun describe(academicClass: AcademicClass): String =
        "\"${academicClass.subject}\" (${academicClass.dayOfWeek.labelEs()} " +
                "${academicClass.startTime}–${academicClass.endTime})"
}