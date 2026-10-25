package com.example.reservaespacios.ui.spaces

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.Equipment
import com.example.reservaespacios.model.EquipmentStatus
import com.example.reservaespacios.model.Space
import com.example.reservaespacios.nativeres.PhotoStorage
import com.example.reservaespacios.ui.components.AuthButton
import com.example.reservaespacios.ui.components.FormErrorMessage
import com.example.reservaespacios.ui.components.LocalImage
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
    val context = LocalContext.current
    var showForm by rememberSaveable { mutableStateOf(state.registeredSpaces.isEmpty()) }

    // Recurso nativo: cámara. La foto queda en un archivo interno y se guarda su ruta.
    var pendingPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val path = pendingPhotoPath
        pendingPhotoPath = null
        if (saved && path != null) onEvent(SpaceFormEvent.PhotoTaken(path)) else PhotoStorage.delete(path)
    }

    fun launchCamera() {
        val file = PhotoStorage.newPhotoFile(context)
        pendingPhotoPath = file.absolutePath
        takePicture.launch(PhotoStorage.uriFor(context, file))
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Sin permiso de cámara no se puede tomar la foto.") }
        }
    }

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            onEvent(SpaceFormEvent.SubmissionHandled)
            showForm = false
            scope.launch { snackbarHostState.showSnackbar("Espacio registrado correctamente.") }
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
            Text(
                "Espacios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                "Espacios registrados (${state.registeredSpaces.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (state.registeredSpaces.isEmpty()) {
                Text(
                    "Aún no hay espacios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            state.registeredSpaces.forEach { space -> SpaceCard(space) }

            OutlinedButton(
                onClick = { showForm = !showForm },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(if (showForm) Icons.Filled.Close else Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (showForm) "Cerrar formulario" else "Registrar nuevo espacio")
            }

            AnimatedVisibility(visible = showForm) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                    SectionLabel("Foto del espacio (opcional)")
                    state.photoPath?.let { path ->
                        LocalImage(
                            path = path,
                            contentDescription = "Foto del espacio",
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp))
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            shape = RoundedCornerShape(50),
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                        PackageManager.PERMISSION_GRANTED
                                if (granted) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
                            }
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.photoPath == null) "Tomar foto" else "Repetir foto")
                        }
                        if (state.photoPath != null) {
                            OutlinedButton(
                                shape = RoundedCornerShape(50),
                                onClick = { onEvent(SpaceFormEvent.PhotoRemoved) }
                            ) { Text("Quitar foto") }
                        }
                    }

                    SectionLabel("Equipamiento (opcional)")

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
                        shape = RoundedCornerShape(50),
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

                    AuthButton(
                        text = "Registrar espacio",
                        onClick = { onEvent(SpaceFormEvent.Submit) },
                        isLoading = state.isSubmitting
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
private fun EquipmentRow(item: Equipment, onRemove: () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    item.status.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor(item.status)
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Quitar ${item.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun statusColor(status: EquipmentStatus) = when (status) {
    EquipmentStatus.AVAILABLE -> Color(0xFF2E7D32)
    EquipmentStatus.OUT_OF_SERVICE -> MaterialTheme.colorScheme.error
    EquipmentStatus.MAINTENANCE -> Color(0xFFB7791F)
}

@Composable
private fun SpaceCard(space: Space) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            space.photoPath?.let { path ->
                LocalImage(
                    path = path,
                    contentDescription = "Foto de ${space.name}",
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp))
                )
            }
            Text(
                "${space.code} · ${space.name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
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