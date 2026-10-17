package com.example.reservaespacios.ui.schedule

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.reservaespacios.model.AcademicClass
import com.example.reservaespacios.model.labelEs
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
            Text("Planificación académica", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Las clases tienen prioridad: bloquean el espacio todas las semanas en ese día y horario.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimePickerField(
                    value = state.startTime,
                    onValueChange = { onEvent(ScheduleFormEvent.StartTimeChanged(it)) },
                    label = "Inicio",
                    error = state.errorOf(ScheduleField.START_TIME),
                    isValid = state.isFieldValid(ScheduleField.START_TIME),
                    onBlur = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.START_TIME)) },
                    modifier = Modifier.weight(1f)
                )
                TimePickerField(
                    value = state.endTime,
                    onValueChange = { onEvent(ScheduleFormEvent.EndTimeChanged(it)) },
                    label = "Término",
                    error = state.errorOf(ScheduleField.END_TIME),
                    isValid = state.isFieldValid(ScheduleField.END_TIME),
                    onBlur = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.END_TIME)) },
                    modifier = Modifier.weight(1f)
                )
            }

            ValidatedTextField(
                value = state.students,
                onValueChange = { onEvent(ScheduleFormEvent.StudentsChanged(it)) },
                label = "Cantidad de estudiantes",
                error = state.errorOf(ScheduleField.STUDENTS),
                isValid = state.isFieldValid(ScheduleField.STUDENTS),
                onBlur = { onEvent(ScheduleFormEvent.FieldBlurred(ScheduleField.STUDENTS)) },
                leadingIcon = Icons.Filled.Group,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )

            FormErrorMessage(state.submitError)

            Button(
                onClick = { onEvent(ScheduleFormEvent.Submit) },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registrar clase")
                }
            }

            Text(
                "Clases registradas (${sortedClasses.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            sortedClasses.forEach { academicClass ->
                ClassCard(
                    academicClass = academicClass,
                    onRemove = { onEvent(ScheduleFormEvent.RemoveClass(academicClass.id)) }
                )
            }
        }
    }
}

@Composable
private fun ClassCard(academicClass: AcademicClass, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(academicClass.subject, style = MaterialTheme.typography.titleSmall)
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
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar ${academicClass.subject}")
            }
        }
    }
}