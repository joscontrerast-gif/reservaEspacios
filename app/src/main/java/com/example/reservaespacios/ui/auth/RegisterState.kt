package com.example.reservaespacios.ui.auth

import com.example.reservaespacios.model.UserProfile

enum class RegisterField { NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD, PROFILE }

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val profile: UserProfile? = null,
    val passwordVisible: Boolean = false,
    val touched: Set<RegisterField> = emptySet(),
    val errors: Map<RegisterField, String> = emptyMap(),
    val isLoading: Boolean = false,
    val submitError: String? = null
) {
    val isFormValid: Boolean get() = errors.isEmpty()

    fun errorOf(field: RegisterField): String? = if (field in touched) errors[field] else null
    fun isFieldValid(field: RegisterField): Boolean = field in touched && field !in errors
}

sealed interface RegisterEvent {
    data class NameChanged(val value: String) : RegisterEvent
    data class EmailChanged(val value: String) : RegisterEvent
    data class PasswordChanged(val value: String) : RegisterEvent
    data class ConfirmPasswordChanged(val value: String) : RegisterEvent
    data class ProfileSelected(val profile: UserProfile) : RegisterEvent
    data class FieldBlurred(val field: RegisterField) : RegisterEvent
    data object TogglePasswordVisibility : RegisterEvent
    data object Submit : RegisterEvent
}