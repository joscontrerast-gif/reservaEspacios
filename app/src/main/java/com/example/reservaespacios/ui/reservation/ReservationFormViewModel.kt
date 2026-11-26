package com.example.reservaespacios.ui.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.ReservationDraft
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.data.ScheduleRepository
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.data.SpaceRepository
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.RolePolicy
import com.example.reservaespacios.model.User
import com.example.reservaespacios.ui.reservation.ReservationField.*
import com.example.reservaespacios.validation.ReservationValidators
import com.example.reservaespacios.validation.RolePolicyRules
import com.example.reservaespacios.validation.ScheduleRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReservationFormViewModel(
    private val spaceRepository: SpaceRepository = AppContainer.spaceRepository,
    private val scheduleRepository: ScheduleRepository = AppContainer.scheduleRepository,
    private val reservationRepository: ReservationRepository = AppContainer.reservationRepository,
    private val sessionStore: SessionStore = AppContainer.sessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<ReservationFormUiState> = _uiState.asStateFlow()

    init {
        // La lista de espacios viene del repositorio: si el administrador crea uno, aparece aquí.
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
        // Los horarios académicos también: si el administrador agrega una clase, el formulario lo sabe.
        viewModelScope.launch {
            scheduleRepository.classes.collect { classes ->
                update { it.copy(academicClasses = classes) }
            }
        }
        // Cupo del perfil: se recalcula cada vez que cambian las reservas (enviar, aprobar, cancelar...).
        viewModelScope.launch {
            reservationRepository.reservations.collect { all ->
                update { it.copy(activeRequests = activeFor(sessionStore.currentUser.value, all)) }
            }
        }
    }

    fun onEvent(event: ReservationFormEvent) {
        when (event) {
            is ReservationFormEvent.SpaceSelected ->
                update { it.copy(space = event.space, touched = it.touched + SPACE) }
            is ReservationFormEvent.ActivityChanged -> update { it.copy(activity = event.value) }
            is ReservationFormEvent.AttendeesChanged -> update { it.copy(attendees = event.value) }
            is ReservationFormEvent.DateChanged -> update { it.copy(date = event.value) }
            is ReservationFormEvent.StartTimeChanged -> update { it.copy(startTime = event.value) }
            is ReservationFormEvent.EndTimeChanged -> update { it.copy(endTime = event.value) }
            is ReservationFormEvent.FieldBlurred -> update { it.copy(touched = it.touched + event.field) }
            ReservationFormEvent.Submit -> submit()
            ReservationFormEvent.SubmissionHandled -> _uiState.value = initialState()
        }
    }

    private fun submit() {
        if (_uiState.value.isSubmitting) return
        // Al enviar, se marcan todos los campos para mostrar todos los errores pendientes.
        update { it.copy(touched = ReservationField.entries.toSet()) }
        val state = _uiState.value
        if (!state.isFormValid) return

        // El formulario ya está validado: estos valores existen. Si algo faltara, simplemente no se envía.
        val user = sessionStore.currentUser.value ?: return
        val space = state.space ?: return
        val date = ReservationValidators.parseDate(state.date) ?: return
        val start = ReservationValidators.parseTime(state.startTime) ?: return
        val end = ReservationValidators.parseTime(state.endTime) ?: return
        val attendees = state.attendees.trim().toIntOrNull() ?: return

        viewModelScope.launch {
            update { it.copy(isSubmitting = true) }
            reservationRepository.submit(
                ReservationDraft(
                    spaceId = space.id,
                    spaceName = space.name,
                    requesterId = user.id,
                    requesterName = user.name,
                    activity = state.activity.trim(),
                    attendees = attendees,
                    date = date,
                    startTime = start,
                    endTime = end
                )
            )
            update { it.copy(isSubmitting = false, isSubmitted = true) }
        }
    }

    private fun initialState(): ReservationFormUiState {
        val user = sessionStore.currentUser.value
        return validate(
            ReservationFormUiState(
                spaces = spaceRepository.spaces.value,
                academicClasses = scheduleRepository.classes.value,
                policy = user?.let { RolePolicy.of(it.profile) },
                activeRequests = activeFor(user, reservationRepository.reservations.value)
            )
        )
    }

    private fun activeFor(user: User?, reservations: List<Reservation>): Int =
        if (user == null) 0 else RolePolicyRules.countActive(reservations, user.id, LocalDate.now())

    /** Aplica el cambio y recalcula los errores de TODOS los campos (hay reglas cruzadas). */
    private fun update(transform: (ReservationFormUiState) -> ReservationFormUiState) {
        _uiState.update { validate(transform(it)) }
    }

    private fun validate(state: ReservationFormUiState): ReservationFormUiState {
        // Prioridad académica: no se puede solicitar un horario ocupado por una clase.
        val space = state.space
        val date = ReservationValidators.parseDate(state.date)
        val start = ReservationValidators.parseTime(state.startTime)
        val end = ReservationValidators.parseTime(state.endTime)
        val classConflict =
            if (space != null && date != null && start != null && end != null && end.isAfter(start)) {
                ScheduleRules.findClassConflict(space.id, date, start, end, state.academicClasses)
                    ?.let { "Choca con la clase ${ScheduleRules.describe(it)}" }
            } else null

        // Privilegios del perfil: anticipación y duración (solo se evalúan si el valor ya es válido).
        val today = LocalDate.now()
        val policyDateError =
            if (date != null && !date.isBefore(today)) RolePolicyRules.validateDate(state.policy, date, today) else null
        val policyDurationError =
            if (start != null && end != null && end.isAfter(start)) RolePolicyRules.validateDuration(state.policy, start, end) else null

        val errors = buildMap {
            ReservationValidators.validateSpace(state.space)?.let { put(SPACE, it) }
            ReservationValidators.validateActivity(state.activity)?.let { put(ACTIVITY, it) }
            ReservationValidators.validateAttendees(state.attendees, state.space)?.let { put(ATTENDEES, it) }
            (ReservationValidators.validateDate(state.date, today) ?: policyDateError)
                ?.let { put(DATE, it) }
            ReservationValidators.validateStartTime(state.startTime)?.let { put(START_TIME, it) }
            (ReservationValidators.validateEndTime(state.startTime, state.endTime) ?: policyDurationError ?: classConflict)
                ?.let { put(END_TIME, it) }
        }
        return state.copy(
            errors = errors,
            limitError = RolePolicyRules.validateActiveRequests(state.policy, state.activeRequests)
        )
    }
}