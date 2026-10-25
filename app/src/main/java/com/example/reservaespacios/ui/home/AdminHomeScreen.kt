package com.example.reservaespacios.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import com.example.reservaespacios.ui.review.ReviewEvent
import com.example.reservaespacios.ui.review.ReviewUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Ambar = Color(0xFFB7791F)
private val Verde = Color(0xFF2E7D32)
private val fechaFormato: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun AdminHomeScreen(
    state: ReviewUiState,
    onEvent: (ReviewEvent) -> Unit,
    onSeeRequests: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Mensaje al aprobar desde Inicio
    LaunchedEffect(state.feedback) {
        state.feedback?.let { message ->
            onEvent(ReviewEvent.FeedbackShown)
            snackbarHostState.showSnackbar(message)
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val today = LocalDate.now()
    val pending = state.pending
    val approved = state.resolved.filter { it.status == ReservationStatus.APPROVED }

    // Pendientes y confirmadas, ordenadas por fecha y luego por hora de inicio
    val active = (pending + approved).sortedWith(compareBy({ it.date }, { it.startTime }))
    val todayItems = active.filter { it.date == today }
    val upcomingItems = active.filter { it.date.isAfter(today) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 1. Cuadro "por aprobar": desaparece cuando no hay pendientes
            AnimatedVisibility(visible = pending.isNotEmpty()) {
                val shape = RoundedCornerShape(24.dp)
                Column(Modifier.padding(bottom = 16.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, shape)
                            .clip(shape)
                            .background(Brush.horizontalGradient(listOf(primary, secondary)))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${pending.size} por aprobar",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Esperan tu respuesta.",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onSeeRequests,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = primary),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Ver solicitudes", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 2. Para hoy
                Text("Para hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primary)
                if (todayItems.isEmpty()) {
                    Text(
                        "Nada para hoy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    todayItems.forEach { reservation ->
                        ReservationQuickCard(
                            reservation = reservation,
                            showDate = false,
                            isProcessing = state.processingId == reservation.id,
                            anyProcessing = state.processingId != null,
                            onApprove = { onEvent(ReviewEvent.ApproveClicked(reservation.id)) }
                        )
                    }
                }

                // 3. Resumen
                Text(
                    "Resumen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminSummaryCard("Pendientes", pending.size, Icons.Filled.Schedule, Ambar, Modifier.weight(1f))
                    AdminSummaryCard("Confirmadas", approved.size, Icons.Filled.CheckCircle, Verde, Modifier.weight(1f))
                }

                // 4. Próximas reservas, en orden de fecha y hora
                Text(
                    "Próximas reservas (${upcomingItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
                if (upcomingItems.isEmpty()) {
                    Text(
                        "Sin reservas próximas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    upcomingItems.forEach { reservation ->
                        ReservationQuickCard(
                            reservation = reservation,
                            showDate = true,
                            isProcessing = state.processingId == reservation.id,
                            anyProcessing = state.processingId != null,
                            onApprove = { onEvent(ReviewEvent.ApproveClicked(reservation.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSummaryCard(label: String, count: Int, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    val shown by animateIntAsState(count, tween(600), label = "count")
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "%02d".format(shown),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ReservationQuickCard(
    reservation: Reservation,
    showDate: Boolean,
    isProcessing: Boolean,
    anyProcessing: Boolean,
    onApprove: () -> Unit
) {
    val isPending = reservation.status == ReservationStatus.PENDING
    val chipColor = if (isPending) Ambar else Verde
    val whenText = (if (showDate) "${reservation.date.format(fechaFormato)} · " else "") +
            "${reservation.startTime}–${reservation.endTime}"

    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    reservation.spaceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = RoundedCornerShape(50), color = chipColor.copy(alpha = 0.12f)) {
                    Text(
                        if (isPending) "Pendiente" else "Confirmada",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = chipColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(reservation.activity, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$whenText · ${reservation.requesterName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onApprove, enabled = !anyProcessing, shape = RoundedCornerShape(50)) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Aprobar")
                        }
                    }
                }
            }
        }
    }
}