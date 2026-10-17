package com.example.reservaespacios.data

import com.example.reservaespacios.model.AcademicClass
import kotlinx.coroutines.flow.StateFlow
import java.time.DayOfWeek
import java.time.LocalTime

data class ClassDraft(
    val spaceId: Int,
    val spaceName: String,
    val dayOfWeek: DayOfWeek,
    val subject: String,
    val teacher: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val students: Int
)

sealed interface ScheduleSaveResult {
    data class Success(val academicClass: AcademicClass) : ScheduleSaveResult
    data class Failure(val message: String) : ScheduleSaveResult
}

/** Contrato para la planificación académica. Hoy: FakeScheduleRepository; mañana, Room. */
interface ScheduleRepository {
    val classes: StateFlow<List<AcademicClass>>

    /** Falla si el espacio ya tiene una clase que se solapa ese día. */
    suspend fun addClass(draft: ClassDraft): ScheduleSaveResult

    suspend fun removeClass(id: Int)
}