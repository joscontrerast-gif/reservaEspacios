package com.example.reservaespacios.validation

import com.example.reservaespacios.model.Space
import java.time.DayOfWeek

/** Validaciones del formulario de planificación. null = válido, texto = mensaje de error. */
object ScheduleValidators {

    fun validateSpace(space: Space?): String? =
        if (space == null) "Selecciona un espacio" else null

    fun validateDay(day: DayOfWeek?): String? =
        if (day == null) "Selecciona el día de la semana" else null

    fun validateSubject(value: String): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Ingresa la asignatura"
            text.length < 3 -> "Escribe al menos 3 caracteres"
            text.length > 60 -> "Máximo 60 caracteres"
            else -> null
        }
    }

    fun validateTeacher(value: String): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Ingresa el docente"
            text.length < 3 -> "Escribe al menos 3 caracteres"
            text.length > 60 -> "Máximo 60 caracteres"
            else -> null
        }
    }

    fun validateStudents(value: String, space: Space?): String? {
        val number = value.trim().toIntOrNull()
        return when {
            value.isBlank() -> "Indica la cantidad de estudiantes"
            number == null -> "Ingresa solo números"
            number < 1 -> "Debe haber al menos 1 estudiante"
            space != null && number > space.capacity ->
                "Supera la capacidad del espacio (${space.capacity})"
            else -> null
        }
    }
}