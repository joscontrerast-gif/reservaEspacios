package com.example.reservaespacios.ui.spaces

import com.example.reservaespacios.model.Equipment
import com.example.reservaespacios.model.EquipmentStatus
import com.example.reservaespacios.model.Space

enum class SpaceField { CODE, NAME, CAPACITY, LOCATION }

data class SpaceFormUiState(
    val code: String = "",
    val name: String = "",
    val capacity: String = "",
    val location: String = "",
    // Mini-formulario de equipamiento
    val equipment: List<Equipment> = emptyList(),
    val equipmentName: String = "",
    val equipmentStatus: EquipmentStatus = EquipmentStatus.AVAILABLE,
    val equipmentError: String? = null,
    // Estado general
    val registeredSpaces: List<Space> = emptyList(),
    val touched: Set<SpaceField> = emptySet(),
    val errors: Map<SpaceField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val submitError: String? = null
) {
    val isFormValid: Boolean get() = errors.isEmpty()

    fun errorOf(field: SpaceField): String? = if (field in touched) errors[field] else null
    fun isFieldValid(field: SpaceField): Boolean = field in touched && field !in errors
}

sealed interface SpaceFormEvent {
    data class CodeChanged(val value: String) : SpaceFormEvent
    data class NameChanged(val value: String) : SpaceFormEvent
    data class CapacityChanged(val value: String) : SpaceFormEvent
    data class LocationChanged(val value: String) : SpaceFormEvent
    data class FieldBlurred(val field: SpaceField) : SpaceFormEvent
    data class EquipmentNameChanged(val value: String) : SpaceFormEvent
    data class EquipmentStatusSelected(val status: EquipmentStatus) : SpaceFormEvent
    data object AddEquipment : SpaceFormEvent
    data class RemoveEquipment(val index: Int) : SpaceFormEvent
    data object Submit : SpaceFormEvent
    data object SubmissionHandled : SpaceFormEvent
}