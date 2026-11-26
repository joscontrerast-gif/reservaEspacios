package com.example.reservaespacios.ui.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.labelEs
import com.example.reservaespacios.util.DateTimeFormats
import java.time.LocalDate

@Composable
fun AgendaRoute(viewModel: AgendaViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AgendaScreen(state = state)
}

/** Pantalla sin estado propio: solo muestra las reservas aprobadas agrupadas por día. */
@Composable
fun AgendaScreen(state: AgendaUiState, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val byDate = state.reservations.groupBy { it.date }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "title") {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Agenda de espacios",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Reservas aprobadas de todos los espacios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.reservations.isEmpty()) {
            item(key = "empty") {
                Text(
                    "No hay reservas aprobadas próximas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        byDate.forEach { (date, reservations) ->
            item(key = "date-$date") {
                val suffix = if (date == today) " · Hoy" else ""
                Text(
                    "${date.dayOfWeek.labelEs()} ${DateTimeFormats.formatDate(date)}$suffix",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.animateItem()
                )
            }
            items(reservations, key = { it.id }) { reservation ->
                AgendaCard(reservation, Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun AgendaCard(reservation: Reservation, modifier: Modifier = Modifier) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${reservation.spaceName} · ${reservation.startTime}–${reservation.endTime}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(reservation.activity, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Solicitante: ${reservation.requesterName} · ${reservation.attendees} asistentes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
