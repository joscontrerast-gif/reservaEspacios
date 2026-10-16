package com.example.reservaespacios.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile
import com.example.reservaespacios.ui.reservation.ReservationFormRoute
import com.example.reservaespacios.ui.spaces.SpaceFormRoute

@Composable
fun HomeScreen(
    user: User,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Hola, ${user.name}", style = MaterialTheme.typography.titleMedium)
                Text(
                    user.profile.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSignOut) { Text("Cerrar sesión") }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (user.profile == UserProfile.ADMIN) {
                // Por ahora el administrador entra directo al registro de espacios.
                // Cuando exista aprobar/rechazar, aquí irá la navegación entre ambas pantallas.
                SpaceFormRoute()
            } else {
                // key por usuario: si otra persona inicia sesión, el formulario parte limpio
                ReservationFormRoute(viewModel = viewModel(key = "reservation-${user.id}"))
            }
        }
    }
}