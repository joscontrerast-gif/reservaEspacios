package com.example.reservaespacios.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    LaunchedEffect(state.feedback) {
        state.feedback?.let { message ->
            onEvent(ReviewEvent.FeedbackShown)
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    val pending = state.pending
    val resolved = state.resolved

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Solicitudes de reserva", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Una solicitud no bloquea el espacio hasta que sea aprobada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { SectionTitle("Pendientes (${pending.size})") }
            if (pending.isEmpty()) {
                item { EmptyText("No hay solicitudes pendientes.") }
            }
            items(pending, key = { it.id }) { reservation ->
                PendingCard(
                    reservation = reservation,
                    isProcessing = state.processingId == reservation.id,
                    anyProcessing = state.processingId != null,
                    onApprove = { onEvent(ReviewEvent.ApproveClicked(reservation.id)) },
                    onReject = { onEvent(ReviewEvent.OpenReject(reservation)) }
                )
            }

            item { SectionTitle("Resueltas (${resolved.size})") }
            if (resolved.isEmpty()) {
                item { EmptyText("Aún no has resuelto ninguna solicitud.") }
            }
            items(resolved, key = { it.id }) { reservation -> ResolvedCard(reservation) }
        }
    }

    state.rejectingReservation?.let { reservation ->
        RejectDialog(state = state, reservation = reservation, onEvent = onEvent)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
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
private fun PendingCard(
    reservation: Reservation,
    isProcessing: Boolean,
    anyProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(reservation.spaceName, style = MaterialTheme.typography.titleMedium)
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
                OutlinedButton(onClick = onReject, enabled = !anyProcessing) { Text("Rechazar") }
                Button(onClick = onApprove, enabled = !anyProcessing) {
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
private fun ResolvedCard(reservation: Reservation) {
    val approved = reservation.status == ReservationStatus.APPROVED
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(reservation.spaceName, style = MaterialTheme.typography.titleMedium)
            Text(
                reservation.status.label,
                style = MaterialTheme.typography.labelLarge,
                color = if (approved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
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