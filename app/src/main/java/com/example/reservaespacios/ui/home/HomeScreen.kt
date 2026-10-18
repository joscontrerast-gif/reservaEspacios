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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile
import com.example.reservaespacios.nativeres.NotificationsOnLogin
import com.example.reservaespacios.ui.myreservations.MyReservationsRoute
import com.example.reservaespacios.ui.reservation.ReservationFormRoute
import com.example.reservaespacios.ui.review.ReviewRoute
import com.example.reservaespacios.ui.schedule.ScheduleFormRoute
import com.example.reservaespacios.ui.spaces.SpaceFormRoute

private data class NavTab(val label: String, val icon: ImageVector)

private val adminTabs = listOf(
    NavTab("Solicitudes", Icons.Filled.Notifications),
    NavTab("Espacios", Icons.Filled.MeetingRoom),
    NavTab("Horarios", Icons.Filled.Schedule)
)
private val userTabs = listOf(
    NavTab("Solicitar", Icons.Filled.Add),
    NavTab("Mis solicitudes", Icons.Filled.DateRange)
)

@Composable
fun HomeScreen(
    user: User,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Recurso nativo: permiso + notificaciones locales según el usuario con sesión iniciada
    NotificationsOnLogin(user)

    val isAdmin = user.profile == UserProfile.ADMIN
    val tabs = if (isAdmin) adminTabs else userTabs
    var selectedTab by rememberSaveable { mutableStateOf(0) }

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
                .consumeWindowInsets(WindowInsets.navigationBars)
        ) {
            // key por usuario: si otra persona inicia sesión, las pantallas parten limpias
            when {
                isAdmin && selectedTab == 0 -> ReviewRoute()
                isAdmin && selectedTab == 1 -> SpaceFormRoute()
                isAdmin -> ScheduleFormRoute()
                selectedTab == 0 -> ReservationFormRoute(viewModel = viewModel(key = "reservation-${user.id}"))
                else -> MyReservationsRoute(viewModel = viewModel(key = "mine-${user.id}"))
            }
        }

        NavigationBar {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = { Text(tab.label) }
                )
            }
        }
    }
}