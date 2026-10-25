package com.example.reservaespacios.ui.myreservations

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import com.example.reservaespacios.nativeres.ReservationIntents
import com.example.reservaespacios.validation.ReservationRules
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun Reservation.whenText(): String =
    "${date.format(dateFormatter)} · $startTime–$endTime · $attendees asistentes"

@Composable
fun MyReservationsRoute(viewModel: MyReservationsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MyReservationsScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun MyReservationsScreen(
    state: MyReservationsUiState,
    onEvent: (MyReservationsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Recurso nativo: abre otra app del teléfono (Calendario, WhatsApp, correo...) mediante un Intent
    fun launchIntent(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar("No hay una app disponible para esta acción.") }
        }
    }

    LaunchedEffect(state.feedback) {
        state.feedback?.let { message ->
            onEvent(MyReservationsEvent.FeedbackShown)
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    val today = LocalDate.now()

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Mis solicitudes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (state.reservations.isEmpty()) {
                item {
                    Text(
                        "Aún no has enviado solicitudes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.reservations, key = { it.id }) { reservation ->
                ReservationCard(
                    reservation = reservation,
                    canCancel = ReservationRules.canCancel(reservation, today),
                    anyProcessing = state.processingId != null,
                    onCancel = { onEvent(MyReservationsEvent.CancelClicked(reservation)) },
                    onAddToCalendar = { launchIntent(ReservationIntents.addToCalendar(reservation)) },
                    onShare = { launchIntent(ReservationIntents.share(reservation)) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    state.cancelling?.let { reservation ->
        CancelDialog(
            reservation = reservation,
            isProcessing = state.processingId != null,
            onConfirm = { onEvent(MyReservationsEvent.ConfirmCancel) },
            onDismiss = { onEvent(MyReservationsEvent.DismissCancel) }
        )
    }
}

@Composable
private fun statusColor(status: ReservationStatus): Color = when (status) {
    ReservationStatus.PENDING -> Color(0xFFB7791F)
    ReservationStatus.APPROVED -> Color(0xFF2E7D32)
    ReservationStatus.REJECTED -> MaterialTheme.colorScheme.error
    ReservationStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    canCancel: Boolean,
    anyProcessing: Boolean,
    onCancel: () -> Unit,
    onAddToCalendar: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor(reservation.status).copy(alpha = 0.12f)
                ) {
                    Text(
                        reservation.status.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor(reservation.status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Text(reservation.activity, style = MaterialTheme.typography.bodyMedium)
            Text(
                reservation.whenText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            reservation.rejectionReason?.let { reason ->
                Text(
                    "Motivo del rechazo: $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (reservation.status == ReservationStatus.APPROVED) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onAddToCalendar) { Text("Agregar al calendario") }
                    TextButton(onClick = onShare) { Text("Compartir") }
                }
            }
            if (canCancel) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !anyProcessing,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Cancelar solicitud")
                }
            }
        }
    }
}

@Composable
private fun CancelDialog(
    reservation: Reservation,
    isProcessing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cancelar la solicitud?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${reservation.spaceName} · ${reservation.activity}")
                Text(reservation.whenText(), style = MaterialTheme.typography.bodySmall)
                if (reservation.status == ReservationStatus.APPROVED) {
                    Text(
                        "La reserva ya estaba aprobada: al cancelarla, el horario quedará libre.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isProcessing) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Sí, cancelar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) { Text("Volver") }
        }
    )
}