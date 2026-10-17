package com.example.reservaespacios.data

import com.example.reservaespacios.model.AcademicClass
import com.example.reservaespacios.validation.ScheduleRules
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalTime

/** Repositorio en memoria. Se pierde al cerrar la app. */
class FakeScheduleRepository : ScheduleRepository {

    // Clases de ejemplo en espacios distintos del Teatro, para no interferir con las solicitudes de ejemplo.
    private val _classes = MutableStateFlow(
        listOf(
            AcademicClass(
                id = 1, subject = "Diseño Gráfico I", teacher = "Prof. Rojas",
                spaceId = 3, spaceName = "Taller de diseño",
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(8, 30), endTime = LocalTime.of(10, 0), students = 25
            ),
            AcademicClass(
                id = 2, subject = "Producción Audiovisual", teacher = "Prof. Soto",
                spaceId = 2, spaceName = "Estudio de grabación",
                dayOfWeek = DayOfWeek.WEDNESDAY,
                startTime = LocalTime.of(14, 0), endTime = LocalTime.of(16, 0), students = 12
            )
        )
    )
    override val classes: StateFlow<List<AcademicClass>> = _classes.asStateFlow()

    override suspend fun addClass(draft: ClassDraft): ScheduleSaveResult {
        delay(600) // simula latencia
        val overlap = ScheduleRules.findOverlappingClass(
            spaceId = draft.spaceId,
            day = draft.dayOfWeek,
            start = draft.startTime,
            end = draft.endTime,
            classes = _classes.value
        )
        if (overlap != null) {
            return ScheduleSaveResult.Failure(
                "${draft.spaceName} ya tiene la clase ${ScheduleRules.describe(overlap)} en ese horario"
            )
        }
        val academicClass = AcademicClass(
            id = (_classes.value.maxOfOrNull { it.id } ?: 0) + 1,
            subject = draft.subject,
            teacher = draft.teacher,
            spaceId = draft.spaceId,
            spaceName = draft.spaceName,
            dayOfWeek = draft.dayOfWeek,
            startTime = draft.startTime,
            endTime = draft.endTime,
            students = draft.students
        )
        _classes.update { it + academicClass }
        return ScheduleSaveResult.Success(academicClass)
    }

    override suspend fun removeClass(id: Int) {
        delay(300)
        _classes.update { list -> list.filterNot { it.id == id } }
    }
}