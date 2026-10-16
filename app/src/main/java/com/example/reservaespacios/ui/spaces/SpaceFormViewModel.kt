package com.example.reservaespacios.ui.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservaespacios.AppContainer
import com.example.reservaespacios.data.SpaceRepository
import com.example.reservaespacios.data.SpaceSaveResult
import com.example.reservaespacios.model.Equipment
import com.example.reservaespacios.model.EquipmentStatus
import com.example.reservaespacios.validation.SpaceValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SpaceFormViewModel(
    private val spaceRepository: SpaceRepository = AppContainer.spaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<SpaceFormUiState> = _uiState.asStateFlow()

    init {
        // Mantiene al día el listado de espacios registrados (y la validación de código duplicado).
        viewModelScope.launch {
            spaceRepository.spaces.collect { spaces ->
                update { it.copy(registeredSpaces = spaces) }
            }
        }
    }

    fun onEvent(event: SpaceFormEvent) {
        when (event) {
            is SpaceFormEvent.CodeChanged -> update { it.copy(code = event.value, submitError = null) }
            is SpaceFormEvent.NameChanged -> update { it.copy(name = event.value) }
            is SpaceFormEvent.CapacityChanged -> update { it.copy(capacity = event.value) }
            is SpaceFormEvent.LocationChanged -> update { it.copy(location = event.value) }
            is SpaceFormEvent.FieldBlurred -> update { it.copy(touched = it.touched + event.field) }
            is SpaceFormEvent.EquipmentNameChanged ->
                update { it.copy(equipmentName = event.value, equipmentError = null) }
            is SpaceFormEvent.EquipmentStatusSelected -> update { it.copy(equipmentStatus = event.status) }
            SpaceFormEvent.AddEquipment -> addEquipment()
            is SpaceFormEvent.RemoveEquipment ->
                update { state -> state.copy(equipment = state.equipment.filterIndexed { i, _ -> i != event.index }) }
            SpaceFormEvent.Submit -> submit()
            SpaceFormEvent.SubmissionHandled -> update { it.copy(isSubmitted = false) }
        }
    }

    private fun addEquipment() {
        val state = _uiState.value
        val error = SpaceValidators.validateEquipmentName(
            state.equipmentName,
            state.equipment.map { it.name }
        )
        if (error != null) {
            update { it.copy(equipmentError = error) }
            return
        }
        update {
            it.copy(
                equipment = it.equipment + Equipment(it.equipmentName.trim(), it.equipmentStatus),
                equipmentName = "",
                equipmentStatus = EquipmentStatus.AVAILABLE,
                equipmentError = null
            )
        }
    }

    private fun submit() {
        if (_uiState.value.isSubmitting) return
        update { it.copy(touched = SpaceField.entries.toSet()) }
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            update { it.copy(isSubmitting = true, submitError = null) }
            val result = spaceRepository.addSpace(
                code = state.code,
                name = state.name,
                capacity = state.capacity.trim().toInt(), // ya validado
                location = state.location,
                equipment = state.equipment
            )
            when (result) {
                // Formulario limpio + aviso de éxito (la pantalla muestra el mensaje y baja la bandera)
                is SpaceSaveResult.Success -> _uiState.value = initialState().copy(isSubmitted = true)
                is SpaceSaveResult.Failure ->
                    update { it.copy(isSubmitting = false, submitError = result.message) }
            }
        }
    }

    private fun initialState() =
        validate(SpaceFormUiState(registeredSpaces = spaceRepository.spaces.value))

    private fun update(transform: (SpaceFormUiState) -> SpaceFormUiState) {
        _uiState.update { validate(transform(it)) }
    }

    private fun validate(state: SpaceFormUiState): SpaceFormUiState {
        val existingCodes = state.registeredSpaces.map { it.code.trim().uppercase() }.toSet()
        val errors = buildMap {
            SpaceValidators.validateCode(state.code, existingCodes)?.let { put(SpaceField.CODE, it) }
            SpaceValidators.validateName(state.name)?.let { put(SpaceField.NAME, it) }
            SpaceValidators.validateCapacity(state.capacity)?.let { put(SpaceField.CAPACITY, it) }
            SpaceValidators.validateLocation(state.location)?.let { put(SpaceField.LOCATION, it) }
        }
        return state.copy(errors = errors)
    }
}