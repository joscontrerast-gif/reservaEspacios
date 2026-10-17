package com.example.reservaespacios.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.ClassDraft
import com.example.reservaespacios.data.ScheduleRepository
import com.example.reservaespacios.data.ScheduleSaveResult
import com.example.reservaespacios.data.SpaceRepository
import com.example.reservaespacios.validation.ReservationValidators
import com.example.reservaespacios.validation.ScheduleRules
import com.example.reservaespacios.validation.ScheduleValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleFormViewModel(
    private val spaceRepository: SpaceRepository = AppContainer.spaceRepository,
    private val scheduleRepository: ScheduleRepository = AppContainer.scheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<ScheduleFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            spaceRepository.spaces.collect { spaces ->
                update { state ->
                    state.copy(
                        spaces = spaces,
                        space = state.space?.let { selected -> spaces.find { it.id == selected.id } }
                    )
                }
            }
        }
        viewModelScope.launch {
            scheduleRepository.classes.collect { classes -> update { it.copy(classes = classes) } }
        }
    }

    fun onEvent(event: ScheduleFormEvent) {
        when (event) {
            is ScheduleFormEvent.SpaceSelected ->
                update { it.copy(space = event.space, touched = it.touched + ScheduleField.SPACE, submitError = null) }
            is ScheduleFormEvent.DaySelected ->
                update { it.copy(day = event.day, touched = it.touched + ScheduleField.DAY, submitError = null) }
            is ScheduleFormEvent.SubjectChanged -> update { it.copy(subject = event.value) }
            is ScheduleFormEvent.TeacherChanged -> update { it.copy(teacher = event.value) }
            is ScheduleFormEvent.StartTimeChanged -> update { it.copy(startTime = event.value, submitError = null) }
            is ScheduleFormEvent.EndTimeChanged -> update { it.copy(endTime = event.value, submitError = null) }
            is ScheduleFormEvent.StudentsChanged -> update { it.copy(students = event.value) }
            is ScheduleFormEvent.FieldBlurred -> update { it.copy(touched = it.touched + event.field) }
            is ScheduleFormEvent.RemoveClass -> removeClass(event.id)
            ScheduleFormEvent.Submit -> submit()
            ScheduleFormEvent.FeedbackShown -> update { it.copy(feedback = null) }
        }
    }

    private fun submit() {
        if (_uiState.value.isSubmitting) return
        update { it.copy(touched = ScheduleField.entries.toSet()) }
        val state = _uiState.value
        if (!state.isFormValid) return

        // El formulario ya está validado: estos valores existen.
        val space = state.space ?: return
        val day = state.day ?: return
        val start = ReservationValidators.parseTime(state.startTime) ?: return
        val end = ReservationValidators.parseTime(state.endTime) ?: return
        val students = state.students.trim().toIntOrNull() ?: return

        viewModelScope.launch {
            update { it.copy(isSubmitting = true, submitError = null) }
            val result = scheduleRepository.addClass(
                ClassDraft(
                    spaceId = space.id,
                    spaceName = space.name,
                    dayOfWeek = day,
                    subject = state.subject.trim(),
                    teacher = state.teacher.trim(),
                    startTime = start,
                    endTime = end,
                    students = students
                )
            )
            when (result) {
                is ScheduleSaveResult.Success ->
                    _uiState.value = initialState().copy(feedback = "Clase registrada. El horario quedó bloqueado.")
                is ScheduleSaveResult.Failure ->
                    update { it.copy(isSubmitting = false, submitError = result.message) }
            }
        }
    }

    private fun removeClass(id: Int) {
        viewModelScope.launch {
            scheduleRepository.removeClass(id)
            update { it.copy(feedback = "Clase eliminada. El horario volvió a estar disponible.") }
        }
    }

    private fun initialState() = validate(
        ScheduleFormUiState(
            spaces = spaceRepository.spaces.value,
            classes = scheduleRepository.classes.value
        )
    )

    private fun update(transform: (ScheduleFormUiState) -> ScheduleFormUiState) {
        _uiState.update { validate(transform(it)) }
    }

    /** Recalcula todos los errores: capacidad y choques de horario dependen de varios campos a la vez. */
    private fun validate(state: ScheduleFormUiState): ScheduleFormUiState {
        val space = state.space
        val day = state.day
        val start = ReservationValidators.parseTime(state.startTime)
        val end = ReservationValidators.parseTime(state.endTime)

        val overlapError =
            if (space != null && day != null && start != null && end != null && end.isAfter(start)) {
                ScheduleRules.findOverlappingClass(space.id, day, start, end, state.classes)
                    ?.let { "Se solapa con la clase ${ScheduleRules.describe(it)}" }
            } else null

        val errors = buildMap {
            ScheduleValidators.validateSpace(state.space)?.let { put(ScheduleField.SPACE, it) }
            ScheduleValidators.validateDay(state.day)?.let { put(ScheduleField.DAY, it) }
            ScheduleValidators.validateSubject(state.subject)?.let { put(ScheduleField.SUBJECT, it) }
            ScheduleValidators.validateTeacher(state.teacher)?.let { put(ScheduleField.TEACHER, it) }
            ReservationValidators.validateStartTime(state.startTime)?.let { put(ScheduleField.START_TIME, it) }
            (ReservationValidators.validateEndTime(state.startTime, state.endTime) ?: overlapError)
                ?.let { put(ScheduleField.END_TIME, it) }
            ScheduleValidators.validateStudents(state.students, state.space)?.let { put(ScheduleField.STUDENTS, it) }
        }
        return state.copy(errors = errors)
    }
}