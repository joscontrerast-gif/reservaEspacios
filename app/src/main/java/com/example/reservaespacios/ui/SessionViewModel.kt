package com.example.reservaespacios.ui

import androidx.lifecycle.ViewModel
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.model.User
import kotlinx.coroutines.flow.StateFlow

class SessionViewModel(
    private val sessionStore: SessionStore = AppContainer.sessionStore
) : ViewModel() {

    val currentUser: StateFlow<User?> = sessionStore.currentUser

    fun signOut() = sessionStore.signOut()
}