package com.example.reservaespacios.ui.home

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile
import com.example.reservaespacios.nativeres.NotificationsOnLogin
import com.example.reservaespacios.ui.myreservations.MyReservationsRoute
import com.example.reservaespacios.ui.myreservations.MyReservationsViewModel
import com.example.reservaespacios.ui.reservation.ReservationFormRoute
import com.example.reservaespacios.ui.review.ReviewRoute
import com.example.reservaespacios.ui.review.ReviewViewModel
import com.example.reservaespacios.ui.schedule.ScheduleFormRoute
import com.example.reservaespacios.ui.spaces.SpaceFormRoute

private data class NavTab(val label: String, val icon: ImageVector)

private val adminTabs = listOf(
    NavTab("Inicio", Icons.Filled.Home),
    NavTab("Solicitudes", Icons.Filled.Notifications),
    NavTab("Espacios", Icons.Filled.MeetingRoom),
    NavTab("Horarios", Icons.Filled.Schedule)
)
private val userTabs = listOf(
    NavTab("Inicio", Icons.Filled.Home),
    NavTab("Solicitar", Icons.Filled.Add),
    NavTab("Mis solicitudes", Icons.Filled.DateRange)
)

@Composable
fun HomeScreen(
    user: User,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    NotificationsOnLogin(user)

    val isAdmin = user.profile == UserProfile.ADMIN
    val tabs = if (isAdmin) adminTabs else userTabs
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { controller?.isAppearanceLightStatusBars = previous ?: true }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.name.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "¡Hola, ${user.name}!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    user.profile.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Más opciones",
                        tint = Color.White
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        },
                        onClick = {
                            menuOpen = false
                            onSignOut()
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                // La barra inferior ya maneja el espacio de la navegación del sistema
                .consumeWindowInsets(WindowInsets.navigationBars)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 12 * dir }) togetherWith
                            (fadeOut(tween(120)) + slideOutHorizontally(tween(200)) { -it / 12 * dir })
                },
                label = "tabs"
            ) { tab ->
                when {
                    isAdmin && tab == 0 -> {
                        val review = viewModel<ReviewViewModel>(key = "review")
                        val reviewState by review.uiState.collectAsStateWithLifecycle()
                        AdminHomeScreen(
                            state = reviewState,
                            onEvent = review::onEvent,
                            onSeeRequests = { selectedTab = 1 }
                        )
                    }
                    isAdmin && tab == 1 -> ReviewRoute(viewModel = viewModel(key = "review"))
                    isAdmin && tab == 2 -> SpaceFormRoute()
                    isAdmin -> ScheduleFormRoute()
                    tab == 0 -> {
                        val mine = viewModel<MyReservationsViewModel>(key = "mine-${user.id}")
                        val mineState by mine.uiState.collectAsStateWithLifecycle()
                        UserHomeScreen(
                            user = user,
                            reservations = mineState.reservations,
                            onNewRequest = { selectedTab = 1 },
                            onSeeAll = { selectedTab = 2 },
                            onSignOut = onSignOut
                        )
                    }
                    tab == 1 -> ReservationFormRoute(viewModel = viewModel(key = "reservation-${user.id}"))
                    else -> MyReservationsRoute(viewModel = viewModel(key = "mine-${user.id}"))
                }
            }
        }

        NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
            tabs.forEachIndexed { index, tab ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}