package com.example.reservaespacios.data

import com.example.reservaespacios.model.Equipment
import com.example.reservaespacios.model.Space
import kotlinx.coroutines.flow.StateFlow

sealed interface SpaceSaveResult {
    data class Success(val space: Space) : SpaceSaveResult
    data class Failure(val message: String) : SpaceSaveResult
}

/** Contrato para los espacios. Implementaciones: FakeSpaceRepository (pruebas) y SqliteSpaceRepository (app real). */
interface SpaceRepository {
    /** Lista observable: cualquier pantalla que la lea se actualiza sola cuando cambia. */
    val spaces: StateFlow<List<Space>>

    suspend fun addSpace(
        code: String,
        name: String,
        capacity: Int,
        location: String,
        equipment: List<Equipment>,
        photoPath: String? = null
    ): SpaceSaveResult
}