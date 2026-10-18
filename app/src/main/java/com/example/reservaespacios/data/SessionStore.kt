package com.example.reservaespacios.data

import com.example.reservaespacios.data.local.AppPreferences
import com.example.reservaespacios.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Única fuente de verdad de "quién está logueado". Toda la app lee de aquí.
 * Si recibe [AppPreferences], la sesión se guarda en el teléfono y se restaura al abrir la app.
 */
class SessionStore(private val preferences: AppPreferences? = null) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // true mientras se busca en la base de datos al usuario de la sesión guardada
    private val _restoring = MutableStateFlow(preferences?.sessionUserId != null)
    val restoring: StateFlow<Boolean> = _restoring.asStateFlow()

    fun signIn(user: User) {
        _currentUser.value = user
        preferences?.sessionUserId = user.id
    }

    fun signOut() {
        _currentUser.value = null
        preferences?.sessionUserId = null
    }

    /** Recupera la sesión guardada. Si el usuario ya no existe, la descarta. */
    suspend fun restore(authRepository: AuthRepository) {
        val savedId = preferences?.sessionUserId
        if (savedId != null && _currentUser.value == null) {
            val user = authRepository.findById(savedId)
            if (user != null) _currentUser.value = user else preferences?.sessionUserId = null
        }
        _restoring.value = false
    }
}
