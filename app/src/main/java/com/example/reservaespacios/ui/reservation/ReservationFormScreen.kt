package com.example.reservaespacios.ui.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.Space
import com.example.reservaespacios.ui.components.DatePickerField
import com.example.reservaespacios.ui.components.TimePickerField
import com.example.reservaespacios.ui.components.ValidatedTextField
import com.example.reservaespacios.util.DateTimeFormats
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/** Conecta el ViewModel con la pantalla. */
@Composable
fun ReservationFormRoute(viewModel: ReservationFormViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReservationFormScreen(state = state, onEvent = viewModel::onEvent)
}

/** Pantalla sin estado propio: recibe el estado y emite eventos. */
@Composable
fun ReservationFormScreen(
    state: ReservationFormUiState,
    onEvent: (ReservationFormEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            onEvent(ReservationFormEvent.SubmissionHandled)
            scope.launch {
                snackbarHostState.showSnackbar("Solicitud enviada. Queda pendiente de aprobación.")
            }
        }
    }

    // Al abrir el reloj de "Término", sugiere una hora después del inicio ya elegido.
    val suggestedEnd = DateTimeFormats.parseTime(state.startTime)
        ?.let { if (it.hour < 23) it.plusHours(1) else it }
        ?: LocalTime.of(10, 0)

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Solicitar espacio", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Tu solicitud quedará pendiente hasta que un administrador la apruebe.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SpaceDropdown(
                spaces = state.spaces,
                selected = state.space,
                error = state.errorOf(ReservationField.SPACE),
                onSelected = { onEvent(ReservationFormEvent.SpaceSelected(it)) },
                onDismiss = { onEvent(ReservationFormEvent.FieldBlurred(ReservationField.SPACE)) }
            )

            ValidatedTextField(
                value = state.activity,
                onValueChange = { onEvent(ReservationFormEvent.ActivityChanged(it)) },
                label = "Actividad",
                error = state.errorOf(ReservationField.ACTIVITY),
                isValid = state.isFieldValid(ReservationField.ACTIVITY),
                onBlur = { onEvent(ReservationFormEvent.FieldBlurred(ReservationField.ACTIVITY)) },
                leadingIcon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = state.attendees,
                onValueChange = { onEvent(ReservationFormEvent.AttendeesChanged(it)) },
                label = "Cantidad de asistentes",
                error = state.errorOf(ReservationField.ATTENDEES),
                isValid = state.isFieldValid(ReservationField.ATTENDEES),
                onBlur = { onEvent(ReservationFormEvent.FieldBlurred(ReservationField.ATTENDEES)) },
                leadingIcon = Icons.Filled.Group,
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                value = state.date,
                onValueChange = { onEvent(ReservationFormEvent.DateChanged(it)) },
                label = "Fecha",
                error = state.errorOf(ReservationField.DATE),
                isValid = state.isFieldValid(ReservationField.DATE),
                onBlur = { onEvent(ReservationFormEvent.FieldBlurred(ReservationField.DATE)) },
                minDate = LocalDate.now(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimePickerField(
                    value = state.startTime,
                    onValueChange = { onEvent(ReservationFormEvent.StartTimeChanged(it)) },
                    label = "Inicio",
                    error = state.errorOf(ReservationField.START_TIME),
                    isValid = state.isFieldValid(ReservationField.START_TIME),
                    onBlur = { onEvent(ReservationFormEvent.FieldBlurred(ReservationField.START_TIME)) },
                    modifier = Modifier.weight(1f)
                )
                TimePickerField(
                    value = state.endTime,
                    onValueChange = { onEvent(ReservationFormEvent.EndTimeChanged(it)) },
                    label = "Término",
                    error = state.errorOf(ReservationField.END_TIME),
                    isValid = state.isFieldValid(ReservationField.END_TIME),
                    onBlur = { onEvent(ReservationFormEvent.FieldBlurred(ReservationField.END_TIME)) },
                    defaultTime = suggestedEnd,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = { onEvent(ReservationFormEvent.Submit) },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Enviar solicitud")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceDropdown(
    spaces: List<Space>,
    selected: Space?,
    error: String?,
    onSelected: (Space) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { "${it.name} (cap. ${it.capacity})" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Espacio") },
            leadingIcon = {
                if (error != null) {
                    Icon(Icons.Filled.Error, "Campo con error", tint = MaterialTheme.colorScheme.error)
                } else {
                    Icon(Icons.Filled.Place, contentDescription = null)
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onDismiss()
            }
        ) {
            spaces.forEach { space ->
                DropdownMenuItem(
                    text = { Text("${space.name} (cap. ${space.capacity})") },
                    onClick = {
                        onSelected(space)
                        expanded = false
                    }
                )
            }
        }
    }
}