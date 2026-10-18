package com.example.reservaespacios.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.AuthRepository
import com.example.reservaespacios.data.AuthResult
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.data.local.AppPreferences
import com.example.reservaespacios.validation.AuthValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AppContainer.authRepository,
    private val sessionStore: SessionStore = AppContainer.sessionStore,
    private val preferences: AppPreferences = AppContainer.preferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> update { it.copy(email = event.value, submitError = null) }
            is LoginEvent.PasswordChanged -> update { it.copy(password = event.value, submitError = null) }
            is LoginEvent.FieldBlurred -> update { it.copy(touched = it.touched + event.field) }
            LoginEvent.TogglePasswordVisibility -> update { it.copy(passwordVisible = !it.passwordVisible) }
            LoginEvent.Submit -> submit()
        }
    }

    private fun submit() {
        if (_uiState.value.isLoading) return
        update { it.copy(touched = LoginField.entries.toSet()) }
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            update { it.copy(isLoading = true, submitError = null) }
            when (val result = authRepository.login(state.email, state.password)) {
                is AuthResult.Success -> {
                    preferences.lastEmail = state.email // se precarga la próxima vez
                    sessionStore.signIn(result.user)
                    _uiState.value = initialState() // limpia el formulario (no deja la clave en memoria)
                }
                is AuthResult.Failure -> update { it.copy(isLoading = false, submitError = result.message) }
            }
        }
    }

    private fun initialState() = validate(LoginUiState(email = preferences.lastEmail))

    private fun update(transform: (LoginUiState) -> LoginUiState) {
        _uiState.update { validate(transform(it)) }
    }

    private fun validate(state: LoginUiState): LoginUiState {
        val errors = buildMap {
            AuthValidators.validateEmail(state.email)?.let { put(LoginField.EMAIL, it) }
            AuthValidators.validateLoginPassword(state.password)?.let { put(LoginField.PASSWORD, it) }
        }
        return state.copy(errors = errors)
    }
}