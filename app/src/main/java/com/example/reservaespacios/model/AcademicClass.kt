package com.example.reservaespacios.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Una clase del semestre que ocupa un espacio todas las semanas, el mismo día y horario.
 * Supuesto: aún no hay fechas de inicio/término de semestre, así que se repite siempre.
 */
data class AcademicClass(
    val id: Int,
    val subject: String,
    val teacher: String,
    val spaceId: Int,
    val spaceName: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val students: Int
)

fun DayOfWeek.labelEs(): String = when (this) {
    DayOfWeek.MONDAY -> "Lunes"
    DayOfWeek.TUESDAY -> "Martes"
    DayOfWeek.WEDNESDAY -> "Miércoles"
    DayOfWeek.THURSDAY -> "Jueves"
    DayOfWeek.FRIDAY -> "Viernes"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}