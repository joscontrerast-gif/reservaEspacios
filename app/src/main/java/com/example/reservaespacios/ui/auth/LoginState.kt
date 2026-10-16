package com.example.reservaespacios.ui.auth

enum class LoginField { EMAIL, PASSWORD }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val touched: Set<LoginField> = emptySet(),
    val errors: Map<LoginField, String> = emptyMap(),
    val isLoading: Boolean = false,
    val submitError: String? = null
) {
    val isFormValid: Boolean get() = errors.isEmpty()

    fun errorOf(field: LoginField): String? = if (field in touched) errors[field] else null
    fun isFieldValid(field: LoginField): Boolean = field in touched && field !in errors
}

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data class FieldBlurred(val field: LoginField) : LoginEvent
    data object TogglePasswordVisibility : LoginEvent
    data object Submit : LoginEvent
}