package com.example.reservaespacios.nativeres

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.model.User
import kotlinx.coroutines.launch

/**
 * Pide el permiso de notificaciones (Android 13+) y, una vez concedido, muestra los avisos del usuario.
 * Se ejecuta cada vez que cambia el usuario con sesión iniciada.
 */
@Composable
fun NotificationsOnLogin(user: User) {
    val scope = rememberCoroutineScope()
    val currentUser = rememberUpdatedState(user)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch {
                AppContainer.notifier.notifyOnLogin(currentUser.value, AppContainer.reservationRepository)
            }
        }
    }

    LaunchedEffect(user.id) {
        val notifier = AppContainer.notifier
        if (notifier.hasPermission()) {
            notifier.notifyOnLogin(user, AppContainer.reservationRepository)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
