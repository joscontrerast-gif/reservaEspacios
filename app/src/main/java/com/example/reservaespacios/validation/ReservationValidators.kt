package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Space
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object ReservationValidators {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun parseDate(value: String): LocalDate? =
        try { LocalDate.parse(value.trim(), dateFormatter) } catch (e: DateTimeParseException) { null }

    fun parseTime(value: String): LocalTime? =
        try { LocalTime.parse(value.trim(), timeFormatter) } catch (e: DateTimeParseException) { null }

    fun validateSpace(space: Space?): String? =
        if (space == null) "Selecciona un espacio" else null

    fun validateActivity(value: String): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Describe la actividad"
            text.length < 5 -> "Escribe al menos 5 caracteres"
            text.length > 100 -> "Máximo 100 caracteres"
            else -> null
        }
    }

    fun validateAttendees(value: String, space: Space?): String? {
        val number = value.trim().toIntOrNull()
        return when {
            value.isBlank() -> "Indica la cantidad de asistentes"
            number == null -> "Ingresa solo números"
            number <= 0 -> "Debe haber al menos 1 asistente"
            space != null && number > space.capacity ->
                "Supera la capacidad del espacio (${space.capacity})"
            else -> null
        }
    }

    fun validateDate(value: String, today: LocalDate): String? {
        if (value.isBlank()) return "Ingresa la fecha"
        val date = parseDate(value) ?: return "Formato inválido (dd/MM/aaaa)"
        return if (date.isBefore(today)) "La fecha no puede ser pasada" else null
    }

    fun validateStartTime(value: String): String? {
        if (value.isBlank()) return "Ingresa la hora de inicio"
        return if (parseTime(value) == null) "Formato inválido (HH:mm)" else null
    }

    fun validateEndTime(start: String, end: String): String? {
        if (end.isBlank()) return "Ingresa la hora de término"
        val endTime = parseTime(end) ?: return "Formato inválido (HH:mm)"
        val startTime = parseTime(start) ?: return null // el error de inicio se muestra en su campo
        return if (!endTime.isAfter(startTime)) "Debe ser posterior a la hora de inicio" else null
    }
}