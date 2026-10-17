package com.example.reservaespacios.ui.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.ReservationDraft
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.data.SpaceRepository
import com.example.reservaespacios.ui.reservation.ReservationField.*
import com.example.reservaespacios.validation.ReservationValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReservationFormViewModel(
    private val spaceRepository: SpaceRepository = AppContainer.spaceRepository,
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

    private fun initialState() =
        validate(ReservationFormUiState(spaces = spaceRepository.spaces.value))

    /** Aplica el cambio y recalcula los errores de TODOS los campos (hay reglas cruzadas). */
    private fun update(transform: (ReservationFormUiState) -> ReservationFormUiState) {
        _uiState.update { validate(transform(it)) }
    }

    private fun validate(state: ReservationFormUiState): ReservationFormUiState {
        val errors = buildMap {
            ReservationValidators.validateSpace(state.space)?.let { put(SPACE, it) }
            ReservationValidators.validateActivity(state.activity)?.let { put(ACTIVITY, it) }
            ReservationValidators.validateAttendees(state.attendees, state.space)?.let { put(ATTENDEES, it) }
            ReservationValidators.validateDate(state.date, LocalDate.now())?.let { put(DATE, it) }
            ReservationValidators.validateStartTime(state.startTime)?.let { put(START_TIME, it) }
            ReservationValidators.validateEndTime(state.startTime, state.endTime)?.let { put(END_TIME, it) }
        }
        return state.copy(errors = errors)
    }
}