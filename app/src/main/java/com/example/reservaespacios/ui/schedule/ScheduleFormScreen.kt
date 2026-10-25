package com.example.reservaespacios.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservaespacios.model.AcademicClass
import com.example.reservaespacios.model.labelEs
import com.example.reservaespacios.ui.components.AuthButton
import com.example.reservaespacios.ui.components.FormErrorMessage
import com.example.reservaespacios.ui.components.TimePickerField
import com.example.reservaespacios.ui.components.ValidatedDropdown
import com.example.reservaespacios.ui.components.ValidatedTextField
import kotlinx.coroutines.launch
import java.time.DayOfWeek

private val weekDays: List<DayOfWeek> = DayOfWeek.values().toList()

@Composable
fun ScheduleFormRoute(viewModel: ScheduleFormViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScheduleFormScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun ScheduleFormScreen(
    state: ScheduleFormUiState,
    onEvent: (ScheduleFormEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.feedback) {
        state.feedback?.let { message ->
            onEvent(ScheduleFormEvent.FeedbackShown)
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    val sortedClasses = state.classes.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))
    var showForm by rememberSaveable { mutableStateOf(state.classes.isEmpty()) }

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
                "Horarios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Las clases bloquean el espacio cada semana.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                "Clases registradas (${sortedClasses.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (sortedClasses.isEmpty()) {
                Text(
                    "Aún no hay clases registradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            sortedClasses.forEach { academicClass ->
                ClassCard(
                    academicClass = academicClass,
                    onRemove = { onEvent(ScheduleFormEvent.RemoveClass(academicClass.id)) }
                )
            }

            OutlinedButton(
                onClick = { showForm = !showForm },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(if (showForm) Icons.Filled.Close else Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (showForm) "Cerrar formulario" else "Registrar nueva clase")
            }

            AnimatedVisibility(visible = showForm) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ValidatedDropdown(
                        label = "Espacio",
                        options = state.spaces,
                        selected = state.space,
                        optionLabel = { "${it.name} (cap. ${it.capacity})" },
                        error = state.errorOf(ScheduleField.SPACE),
                        onSelected = { onEvent(ScheduleFormEvent.SpaceSelected(it)) },
                        onDismiss = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.SPACE)) },
                        leadingIcon = Icons.Filled.Place,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ValidatedTextField(
                        value = state.subject,
                        onValueChange = { onEvent(ScheduleFormEvent.SubjectChanged(it)) },
                        label = "Asignatura",
                        error = state.errorOf(ScheduleField.SUBJECT),
                        isValid = state.isFieldValid(ScheduleField.SUBJECT),
                        onBlur = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.SUBJECT)) },
                        leadingIcon = Icons.Filled.Edit,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ValidatedTextField(
                        value = state.teacher,
                        onValueChange = { onEvent(ScheduleFormEvent.TeacherChanged(it)) },
                        label = "Docente",
                        error = state.errorOf(ScheduleField.TEACHER),
                        isValid = state.isFieldValid(ScheduleField.TEACHER),
                        onBlur = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.TEACHER)) },
                        leadingIcon = Icons.Filled.Person,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ValidatedTextField(
                        value = state.students,
                        onValueChange = { onEvent(ScheduleFormEvent.StudentsChanged(it)) },
                        label = "Cantidad de estudiantes",
                        error = state.errorOf(ScheduleField.STUDENTS),
                        isValid = state.isFieldValid(ScheduleField.STUDENTS),
                        onBlur = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.STUDENTS)) },
                        leadingIcon = Icons.Filled.Group,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Horario de la clase",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    ValidatedDropdown(
                        label = "Día de la semana",
                        options = weekDays,
                        selected = state.day,
                        optionLabel = { it.labelEs() },
                        error = state.errorOf(ScheduleField.DAY),
                        onSelected = { onEvent(ScheduleFormEvent.DaySelected(it)) },
                        onDismiss = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.DAY)) },
                        leadingIcon = Icons.Filled.DateRange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TimePickerField(
                        value = state.startTime,
                        onValueChange = { onEvent(ScheduleFormEvent.StartTimeChanged(it)) },
                        label = "Inicio",
                        error = state.errorOf(ScheduleField.START_TIME),
                        isValid = state.isFieldValid(ScheduleField.START_TIME),
                        onBlur = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.START_TIME)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TimePickerField(
                        value = state.endTime,
                        onValueChange = { onEvent(ScheduleFormEvent.EndTimeChanged(it)) },
                        label = "Término",
                        error = state.errorOf(ScheduleField.END_TIME),
                        isValid = state.isFieldValid(ScheduleField.END_TIME),
                        onBlur = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.END_TIME)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    FormErrorMessage(state.submitError)

                    AuthButton(
                        text = "Registrar clase",
                        onClick = { onEvent(ScheduleFormEvent.Submit) },
                        isLoading = state.isSubmitting
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassCard(academicClass: AcademicClass, onRemove: () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    academicClass.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${academicClass.dayOfWeek.labelEs()} ${academicClass.startTime}–${academicClass.endTime}" +
                            " · ${academicClass.spaceName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${academicClass.teacher} · ${academicClass.students} estudiantes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar ${academicClass.subject}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}