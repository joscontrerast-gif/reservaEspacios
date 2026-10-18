package com.example.reservaespacios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.AuthRepository
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.model.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel(
    private val sessionStore: SessionStore = AppContainer.sessionStore,
    authRepository: AuthRepository = AppContainer.authRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = sessionStore.currentUser
    val restoring: StateFlow<Boolean> = sessionStore.restoring

    init {
        // Almacenamiento local: recupera la sesión guardada en SharedPreferences.
        viewModelScope.launch { sessionStore.restore(authRepository) }
    }

    fun signOut() = sessionStore.signOut()
}
