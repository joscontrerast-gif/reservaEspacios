package com.example.reservaespacios.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.AuthRepository
import com.example.reservaespacios.data.AuthResult
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.validation.AuthValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val sessionStore: SessionStore = AppContainer.sessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.NameChanged -> update { it.copy(name = event.value, submitError = null) }
            is RegisterEvent.EmailChanged -> update { it.copy(email = event.value, submitError = null) }
            is RegisterEvent.PasswordChanged -> update { it.copy(password = event.value) }
            is RegisterEvent.ConfirmPasswordChanged -> update { it.copy(confirmPassword = event.value) }
            is RegisterEvent.ProfileSelected ->
                update { it.copy(profile = event.profile, touched = it.touched + RegisterField.PROFILE) }
            is RegisterEvent.FieldBlurred -> update { it.copy(touched = it.touched + event.field) }
            RegisterEvent.TogglePasswordVisibility -> update { it.copy(passwordVisible = !it.passwordVisible) }
            RegisterEvent.Submit -> submit()
        }
    }

    private fun submit() {
        if (_uiState.value.isLoading) return
        update { it.copy(touched = RegisterField.entries.toSet()) }
        val state = _uiState.value
        val profile = state.profile
        if (!state.isFormValid || profile == null) return

        viewModelScope.launch {
            update { it.copy(isLoading = true, submitError = null) }
            val result = authRepository.register(state.name, state.email, state.password, profile)
            when (result) {
                is AuthResult.Success -> {
                    sessionStore.signIn(result.user) // tras registrarse, inicia sesión automáticamente
                    _uiState.value = initialState()
                }
                is AuthResult.Failure -> update { it.copy(isLoading = false, submitError = result.message) }
            }
        }
    }

    private fun initialState() = validate(RegisterUiState())

    private fun update(transform: (RegisterUiState) -> RegisterUiState) {
        _uiState.update { validate(transform(it)) }
    }

    /** Recalcula todos los errores: confirmar contraseña depende de la contraseña. */
    private fun validate(state: RegisterUiState): RegisterUiState {
        val errors = buildMap {
            AuthValidators.validateName(state.name)?.let { put(RegisterField.NAME, it) }
            AuthValidators.validateEmail(state.email)?.let { put(RegisterField.EMAIL, it) }
            AuthValidators.validateNewPassword(state.password)?.let { put(RegisterField.PASSWORD, it) }
            AuthValidators.validateConfirmPassword(state.password, state.confirmPassword)
                ?.let { put(RegisterField.CONFIRM_PASSWORD, it) }
            AuthValidators.validateProfile(state.profile)?.let { put(RegisterField.PROFILE, it) }
        }
        return state.copy(errors = errors)
    }
}