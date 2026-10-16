package com.example.reservaespacios.ui.spaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.Equipment
import com.example.reservaespacios.model.EquipmentStatus
import com.example.reservaespacios.model.Space
import com.example.reservaespacios.ui.components.FormErrorMessage
import com.example.reservaespacios.ui.components.ValidatedDropdown
import com.example.reservaespacios.ui.components.ValidatedTextField
import kotlinx.coroutines.launch

@Composable
fun SpaceFormRoute(viewModel: SpaceFormViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SpaceFormScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun SpaceFormScreen(
    state: SpaceFormUiState,
    onEvent: (SpaceFormEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            onEvent(SpaceFormEvent.SubmissionHandled)
            scope.launch { snackbarHostState.showSnackbar("Espacio registrado correctamente.") }
        }
    }

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
            Text("Registrar espacio", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Agrega un espacio y su equipamiento. Quedará disponible para recibir solicitudes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ValidatedTextField(
                value = state.code,
                onValueChange = { onEvent(SpaceFormEvent.CodeChanged(it)) },
                label = "Código",
                placeholder = "Ej: TEA-02",
                error = state.errorOf(SpaceField.CODE),
                isValid = state.isFieldValid(SpaceField.CODE),
                onBlur = { onEvent(SpaceFormEvent.FieldBlurred(SpaceField.CODE)) },
                leadingIcon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = state.name,
                onValueChange = { onEvent(SpaceFormEvent.NameChanged(it)) },
                label = "Nombre del espacio",
                error = state.errorOf(SpaceField.NAME),
                isValid = state.isFieldValid(SpaceField.NAME),
                onBlur = { onEvent(SpaceFormEvent.FieldBlurred(SpaceField.NAME)) },
                leadingIcon = Icons.Filled.MeetingRoom,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = state.capacity,
                onValueChange = { onEvent(SpaceFormEvent.CapacityChanged(it)) },
                label = "Capacidad máxima",
                error = state.errorOf(SpaceField.CAPACITY),
                isValid = state.isFieldValid(SpaceField.CAPACITY),
                onBlur = { onEvent(SpaceFormEvent.FieldBlurred(SpaceField.CAPACITY)) },
                leadingIcon = Icons.Filled.Group,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = state.location,
                onValueChange = { onEvent(SpaceFormEvent.LocationChanged(it)) },
                label = "Ubicación (opcional)",
                error = state.errorOf(SpaceField.LOCATION),
                isValid = state.location.isNotBlank() && state.isFieldValid(SpaceField.LOCATION),
                onBlur = { onEvent(SpaceFormEvent.FieldBlurred(SpaceField.LOCATION)) },
                leadingIcon = Icons.Filled.LocationOn,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Equipamiento (opcional)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            ValidatedTextField(
                value = state.equipmentName,
                onValueChange = { onEvent(SpaceFormEvent.EquipmentNameChanged(it)) },
                label = "Nombre del equipo",
                placeholder = "Ej: Proyector",
                error = state.equipmentError,
                isValid = false,
                onBlur = {},
                leadingIcon = Icons.Filled.Build,
                imeAction = ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedDropdown(
                label = "Estado del equipo",
                options = EquipmentStatus.entries,
                selected = state.equipmentStatus,
                optionLabel = { it.label },
                error = null,
                onSelected = { onEvent(SpaceFormEvent.EquipmentStatusSelected(it)) },
                onDismiss = {},
                leadingIcon = Icons.Filled.Info,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { onEvent(SpaceFormEvent.AddEquipment) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar equipo")
            }

            state.equipment.forEachIndexed { index, item ->
                EquipmentRow(item = item, onRemove = { onEvent(SpaceFormEvent.RemoveEquipment(index)) })
            }

            FormErrorMessage(state.submitError)

            Button(
                onClick = { onEvent(SpaceFormEvent.Submit) },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registrar espacio")
                }
            }

            Text(
                "Espacios registrados (${state.registeredSpaces.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            state.registeredSpaces.forEach { space -> SpaceCard(space) }
        }
    }
}

@Composable
private fun EquipmentRow(item: Equipment, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                item.status.label,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor(item.status)
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Delete, contentDescription = "Quitar ${item.name}")
        }
    }
}

@Composable
private fun statusColor(status: EquipmentStatus) = when (status) {
    EquipmentStatus.AVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    EquipmentStatus.OUT_OF_SERVICE -> MaterialTheme.colorScheme.error
    EquipmentStatus.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
}

@Composable
private fun SpaceCard(space: Space) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${space.code} · ${space.name}", style = MaterialTheme.typography.titleSmall)
            Text(
                "Capacidad: ${space.capacity} · Equipos: ${space.equipment.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (space.location.isNotBlank()) {
                Text(
                    space.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}