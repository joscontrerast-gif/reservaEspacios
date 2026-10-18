package com.example.reservaespacios.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.ui.auth.LoginRoute
import com.example.reservaespacios.ui.auth.RegisterRoute
import com.example.reservaespacios.ui.home.HomeScreen

/**
 * Raíz de la app: decide qué mostrar según el estado de sesión.
 * Restaurando sesión -> cargando. Sin sesión -> login/registro. Con sesión -> pantalla principal según el perfil.
 * (Si tu pareja arma un NavHost, esta lógica se traslada ahí.)
 */
@Composable
fun AppRoot(sessionViewModel: SessionViewModel = viewModel()) {
    val user by sessionViewModel.currentUser.collectAsStateWithLifecycle()
    val restoring by sessionViewModel.restoring.collectAsStateWithLifecycle()
    val currentUser = user

    when {
        restoring -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        currentUser == null -> AuthFlow()
        else -> HomeScreen(user = currentUser, onSignOut = sessionViewModel::signOut)
    }
}

@Composable
private fun AuthFlow() {
    var showRegister by rememberSaveable { mutableStateOf(false) }
    if (showRegister) {
        RegisterRoute(onNavigateToLogin = { showRegister = false })
    } else {
        LoginRoute(onNavigateToRegister = { showRegister = true })
    }
}
