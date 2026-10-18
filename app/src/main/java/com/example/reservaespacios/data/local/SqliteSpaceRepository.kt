package com.example.reservaespacios.data.local

import androidx.core.content.contentValuesOf
import com.example.reservaespacios.data.SpaceRepository
import com.example.reservaespacios.data.SpaceSaveResult
import com.example.reservaespacios.model.Equipment
import com.example.reservaespacios.model.EquipmentStatus
import com.example.reservaespacios.model.Space
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Espacios y equipamiento guardados en SQLite. La lista observable se recarga tras cada cambio. */
class SqliteSpaceRepository(private val helper: AppDatabaseHelper) : SpaceRepository {

    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    override val spaces: StateFlow<List<Space>> = _spaces.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { _spaces.value = loadAll() }
    }

    override suspend fun addSpace(
        code: String,
        name: String,
        capacity: Int,
        location: String,
        equipment: List<Equipment>,
        photoPath: String?
    ): SpaceSaveResult = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        val normalizedCode = code.trim().uppercase()
        val exists = db.rawQuery("SELECT 1 FROM spaces WHERE UPPER(code) = ?", arrayOf(normalizedCode))
            .use { it.moveToFirst() }
        if (exists) return@withContext SpaceSaveResult.Failure("Ya existe un espacio con ese código")

        var newId = -1L
        db.beginTransaction()
        try {
            newId = db.insert(
                "spaces", null,
                contentValuesOf(
                    "code" to normalizedCode,
                    "name" to name.trim(),
                    "capacity" to capacity,
                    "location" to location.trim(),
                    "photo_path" to photoPath
                )
            )
            equipment.forEach {
                db.insert(
                    "equipment", null,
                    contentValuesOf("space_id" to newId, "name" to it.name, "status" to it.status.name)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        if (newId == -1L) return@withContext SpaceSaveResult.Failure("No se pudo guardar el espacio")

        val all = loadAll()
        _spaces.value = all
        SpaceSaveResult.Success(all.first { it.id == newId.toInt() })
    }

    private fun loadAll(): List<Space> {
        val db = helper.readableDatabase
        val equipmentBySpace = mutableMapOf<Int, MutableList<Equipment>>()
        db.rawQuery("SELECT space_id, name, status FROM equipment ORDER BY id", null).use { c ->
            while (c.moveToNext()) {
                equipmentBySpace.getOrPut(c.getInt(0)) { mutableListOf() }
                    .add(Equipment(c.getString(1), EquipmentStatus.valueOf(c.getString(2))))
            }
        }
        val result = mutableListOf<Space>()
        db.rawQuery("SELECT * FROM spaces ORDER BY id", null).use { c ->
            val iId = c.getColumnIndexOrThrow("id")
            val iPhoto = c.getColumnIndexOrThrow("photo_path")
            while (c.moveToNext()) {
                val id = c.getInt(iId)
                result += Space(
                    id = id,
                    name = c.getString(c.getColumnIndexOrThrow("name")),
                    capacity = c.getInt(c.getColumnIndexOrThrow("capacity")),
                    code = c.getString(c.getColumnIndexOrThrow("code")),
                    location = c.getString(c.getColumnIndexOrThrow("location")),
                    equipment = equipmentBySpace[id].orEmpty(),
                    photoPath = if (c.isNull(iPhoto)) null else c.getString(iPhoto)
                )
            }
        }
        return result
    }
}
