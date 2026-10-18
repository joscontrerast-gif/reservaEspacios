package com.example.reservaespacios.data.local

import androidx.core.content.contentValuesOf
import com.example.reservaespacios.data.ClassDraft
import com.example.reservaespacios.data.ScheduleRepository
import com.example.reservaespacios.data.ScheduleSaveResult
import com.example.reservaespacios.model.AcademicClass
import com.example.reservaespacios.validation.ScheduleRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Planificación académica (clases del semestre) guardada en SQLite. */
class SqliteScheduleRepository(private val helper: AppDatabaseHelper) : ScheduleRepository {

    private val _classes = MutableStateFlow<List<AcademicClass>>(emptyList())
    override val classes: StateFlow<List<AcademicClass>> = _classes.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { _classes.value = loadAll() }
    }

    override suspend fun addClass(draft: ClassDraft): ScheduleSaveResult = withContext(Dispatchers.IO) {
        val overlap = ScheduleRules.findOverlappingClass(
            spaceId = draft.spaceId,
            day = draft.dayOfWeek,
            start = draft.startTime,
            end = draft.endTime,
            classes = loadAll()
        )
        if (overlap != null) {
            return@withContext ScheduleSaveResult.Failure(
                "${draft.spaceName} ya tiene la clase ${ScheduleRules.describe(overlap)} en ese horario"
            )
        }
        val id = helper.writableDatabase.insert(
            "academic_classes", null,
            contentValuesOf(
                "subject" to draft.subject,
                "teacher" to draft.teacher,
                "space_id" to draft.spaceId,
                "space_name" to draft.spaceName,
                "day_of_week" to draft.dayOfWeek.value,
                "start_time" to draft.startTime.toString(),
                "end_time" to draft.endTime.toString(),
                "students" to draft.students
            )
        )
        if (id == -1L) return@withContext ScheduleSaveResult.Failure("No se pudo guardar la clase")
        val all = loadAll()
        _classes.value = all
        ScheduleSaveResult.Success(all.first { it.id == id.toInt() })
    }

    override suspend fun removeClass(id: Int) {
        withContext(Dispatchers.IO) {
            helper.writableDatabase.delete("academic_classes", "id = ?", arrayOf(id.toString()))
            _classes.value = loadAll()
        }
    }

    private fun loadAll(): List<AcademicClass> =
        helper.readableDatabase.rawQuery("SELECT * FROM academic_classes ORDER BY id", null).use { c ->
            buildList { while (c.moveToNext()) add(c.toAcademicClass()) }
        }
}
