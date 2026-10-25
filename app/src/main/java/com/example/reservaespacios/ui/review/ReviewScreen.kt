package com.example.reservaespacios.ui.review

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import com.example.reservaespacios.ui.components.ValidatedTextField
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun Reservation.whenText(): String =
    "${date.format(dateFormatter)} · $startTime–$endTime · $attendees asistentes"

/** Filtros del historial (las pendientes siempre se ven arriba, sin filtro). */
private enum class ReviewFilter(val label: String, val status: ReservationStatus?) {
    ALL("Todas", null),
    APPROVED("Aprobadas", ReservationStatus.APPROVED),
    REJECTED("Rechazadas", ReservationStatus.REJECTED),
    CANCELLED("Canceladas", ReservationStatus.CANCELLED)
}

@Composable
fun ReviewRoute(viewModel: ReviewViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onEvent: (ReviewEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var filter by rememberSaveable { mutableStateOf(ReviewFilter.ALL) }

    LaunchedEffect(state.feedback) {
        state.feedback?.let { message ->
            onEvent(ReviewEvent.FeedbackShown)
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    // Las pendientes siempre se ven completas; el filtro solo afecta al historial
    val pending = state.pending
    val history = if (filter == ReviewFilter.ALL) state.resolved
    else state.resolved.filter { it.status == filter.status }

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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Solicitudes de reserva",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Una solicitud no bloquea el espacio hasta que sea aprobada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Pendientes: lo primero que ve el administrador
            item { SectionTitle("Pendientes (${pending.size})") }
            if (pending.isEmpty()) {
                item { EmptyText("Todo al día.") }
            }
            items(pending, key = { it.id }) { reservation ->
                PendingCard(
                    reservation = reservation,
                    isProcessing = state.processingId == reservation.id,
                    anyProcessing = state.processingId != null,
                    onApprove = { onEvent(ReviewEvent.ApproveClicked(reservation.id)) },
                    onReject = { onEvent(ReviewEvent.OpenReject(reservation)) },
                    modifier = Modifier.animateItem()
                )
            }

            // 2. Historial con filtros
            item { SectionTitle("Historial (${state.resolved.size})") }
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReviewFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(option.label) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            if (history.isEmpty()) {
                item {
                    EmptyText(
                        if (filter == ReviewFilter.ALL) "Aún no hay historial."
                        else "No hay solicitudes ${filter.label.lowercase()}."
                    )
                }
            }
            items(history, key = { it.id }) { reservation ->
                ResolvedCard(reservation, modifier = Modifier.animateItem())
            }
        }
    }

    state.rejectingReservation?.let { reservation ->
        RejectDialog(state = state, reservation = reservation, onEvent = onEvent)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PendingCard(
    reservation: Reservation,
    isProcessing: Boolean,
    anyProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    reservation.spaceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip("Pendiente", Color(0xFFB7791F))
            }
            Text(reservation.activity, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Solicitante: ${reservation.requesterName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                reservation.whenText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = !anyProcessing,
                    shape = RoundedCornerShape(50)
                ) { Text("Rechazar") }
                Button(
                    onClick = onApprove,
                    enabled = !anyProcessing,
                    shape = RoundedCornerShape(50)
                ) {
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

@Composable
private fun ResolvedCard(reservation: Reservation, modifier: Modifier = Modifier) {
    val statusColor = when (reservation.status) {
        ReservationStatus.APPROVED -> Color(0xFF2E7D32)
        ReservationStatus.REJECTED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    reservation.spaceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(reservation.status.label, statusColor)
            }
            Text(reservation.activity, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Solicitante: ${reservation.requesterName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                reservation.whenText(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            reservation.rejectionReason?.let { reason ->
                Text(
                    "Motivo: $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RejectDialog(
    state: ReviewUiState,
    reservation: Reservation,
    onEvent: (ReviewEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(ReviewEvent.DismissReject) },
        title = { Text("Rechazar solicitud") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${reservation.spaceName} · ${reservation.activity}",
                    style = MaterialTheme.typography.bodyMedium
                )
                ValidatedTextField(
                    value = state.rejectReason,
                    onValueChange = { onEvent(ReviewEvent.ReasonChanged(it)) },
                    label = "Motivo del rechazo",
                    error = state.visibleReasonError,
                    isValid = state.rejectReasonTouched && state.rejectReasonError == null,
                    onBlur = { onEvent(ReviewEvent.ReasonBlurred) },
                    leadingIcon = Icons.Filled.Edit,
                    singleLine = false,
                    imeAction = ImeAction.Done,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(ReviewEvent.ConfirmReject) },
                enabled = state.processingId == null
            ) {
                if (state.processingId != null) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Rechazar solicitud")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(ReviewEvent.DismissReject) }) { Text("Cancelar") }
        }
    )
}