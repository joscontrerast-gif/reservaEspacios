package com.example.reservaespacios.ui.reservation

import com.example.reservaespacios.model.Space

enum class ReservationField { SPACE, ACTIVITY, ATTENDEES, DATE, START_TIME, END_TIME }

data class ReservationFormUiState(
    val spaces: List<Space> = emptyList(),
    val space: Space? = null,
    val activity: String = "",
    val attendees: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val touched: Set<ReservationField> = emptySet(),
    val errors: Map<ReservationField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false
) {
    val isFormValid: Boolean get() = errors.isEmpty()

    /** El error solo se muestra cuando el usuario ya interactuó con el campo. */
    fun errorOf(field: ReservationField): String? =
        if (field in touched) errors[field] else null

    fun isFieldValid(field: ReservationField): Boolean =
        field in touched && field !in errors
}

sealed interface ReservationFormEvent {
    data class SpaceSelected(val space: Space) : ReservationFormEvent
    data class ActivityChanged(val value: String) : ReservationFormEvent
    data class AttendeesChanged(val value: String) : ReservationFormEvent
    data class DateChanged(val value: String) : ReservationFormEvent
    data class StartTimeChanged(val value: String) : ReservationFormEvent
    data class EndTimeChanged(val value: String) : ReservationFormEvent
    data class FieldBlurred(val field: ReservationField) : ReservationFormEvent
    data object Submit : ReservationFormEvent
    data object SubmissionHandled : ReservationFormEvent
}