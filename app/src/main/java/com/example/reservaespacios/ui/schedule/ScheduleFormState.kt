package com.example.reservaespacios.ui.schedule

import com.example.reservaespacios.model.AcademicClass
import com.example.reservaespacios.model.Space
import java.time.DayOfWeek

enum class ScheduleField { SPACE, DAY, SUBJECT, TEACHER, START_TIME, END_TIME, STUDENTS }

data class ScheduleFormUiState(
    val spaces: List<Space> = emptyList(),
    val classes: List<AcademicClass> = emptyList(),
    val space: Space? = null,
    val day: DayOfWeek? = null,
    val subject: String = "",
    val teacher: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val students: String = "",
    val touched: Set<ScheduleField> = emptySet(),
    val errors: Map<ScheduleField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val feedback: String? = null
) {
    val isFormValid: Boolean get() = errors.isEmpty()

    fun errorOf(field: ScheduleField): String? = if (field in touched) errors[field] else null
    fun isFieldValid(field: ScheduleField): Boolean = field in touched && field !in errors
}

sealed interface ScheduleFormEvent {
    data class SpaceSelected(val space: Space) : ScheduleFormEvent
    data class DaySelected(val day: DayOfWeek) : ScheduleFormEvent
    data class SubjectChanged(val value: String) : ScheduleFormEvent
    data class TeacherChanged(val value: String) : ScheduleFormEvent
    data class StartTimeChanged(val value: String) : ScheduleFormEvent
    data class EndTimeChanged(val value: String) : ScheduleFormEvent
    data class StudentsChanged(val value: String) : ScheduleFormEvent
    data class FieldBlurred(val field: ScheduleField) : ScheduleFormEvent
    data class RemoveClass(val id: Int) : ScheduleFormEvent
    data object Submit : ScheduleFormEvent
    data object FeedbackShown : ScheduleFormEvent
}