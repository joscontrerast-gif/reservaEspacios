package com.example.reservaespacios.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile
import com.example.reservaespacios.ui.reservation.ReservationFormRoute
import com.example.reservaespacios.ui.review.ReviewRoute
import com.example.reservaespacios.ui.spaces.SpaceFormRoute

@Composable
fun HomeScreen(
    user: User,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = user.profile == UserProfile.ADMIN
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0 = Solicitudes, 1 = Espacios

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

        Box(
            modifier = Modifier
                .weight(1f)
                // La barra inferior ya maneja el espacio de la navegación del sistema
                .then(if (isAdmin) Modifier.consumeWindowInsets(WindowInsets.navigationBars) else Modifier)
        ) {
            when {
                // key por usuario: si otra persona inicia sesión, el formulario parte limpio
                !isAdmin -> ReservationFormRoute(viewModel = viewModel(key = "reservation-${user.id}"))
                selectedTab == 0 -> ReviewRoute()
                else -> SpaceFormRoute()
            }
        }

        if (isAdmin) {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                    label = { Text("Solicitudes") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.MeetingRoom, contentDescription = null) },
                    label = { Text("Espacios") }
                )
            }
        }
    }
}