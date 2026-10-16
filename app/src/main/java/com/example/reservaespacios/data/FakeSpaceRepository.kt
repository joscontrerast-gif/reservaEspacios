package com.example.reservaespacios.data

import com.example.reservaespacios.model.Equipment
import com.example.reservaespacios.model.EquipmentStatus
import com.example.reservaespacios.model.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Repositorio en memoria. Se pierde al cerrar la app. */
class FakeSpaceRepository : SpaceRepository {

    private val _spaces = MutableStateFlow(
        listOf(
            Space(
                id = 1, name = "Teatro", capacity = 120, code = "TEA-01",
                location = "Edificio principal",
                equipment = listOf(
                    Equipment("Proyector", EquipmentStatus.AVAILABLE),
                    Equipment("Micrófonos", EquipmentStatus.AVAILABLE)
                )
            ),
            Space(id = 2, name = "Estudio de grabación", capacity = 15, code = "EST-01"),
            Space(id = 3, name = "Taller de diseño", capacity = 30, code = "TAL-01"),
            Space(id = 4, name = "Sala común", capacity = 40, code = "SAL-01")
        )
    )
    override val spaces: StateFlow<List<Space>> = _spaces.asStateFlow()

    override suspend fun addSpace(
        code: String,
        name: String,
        capacity: Int,
        location: String,
        equipment: List<Equipment>
    ): SpaceSaveResult {
        delay(600) // simula latencia
        val normalizedCode = code.trim().uppercase()
        if (_spaces.value.any { it.code.equals(normalizedCode, ignoreCase = true) }) {
            return SpaceSaveResult.Failure("Ya existe un espacio con ese código")
        }
        val space = Space(
            id = (_spaces.value.maxOfOrNull { it.id } ?: 0) + 1,
            name = name.trim(),
            capacity = capacity,
            code = normalizedCode,
            location = location.trim(),
            equipment = equipment
        )
        _spaces.update { it + space }
        return SpaceSaveResult.Success(space)
    }
}