package com.example.reservaespacios.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import java.time.LocalDate

/**
 * Base de datos SQLite local de la app (archivo reserva_espacios.db en el almacenamiento interno).
 * Se crea la primera vez que se abre y se llena con los datos de ejemplo (usuarios, espacios, clases y solicitudes).
 */
class AppDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE COLLATE NOCASE,
                password_hash TEXT NOT NULL,
                salt TEXT NOT NULL,
                profile TEXT NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE spaces (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT NOT NULL UNIQUE,
                name TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                location TEXT NOT NULL DEFAULT '',
                photo_path TEXT
            )"""
        )
        db.execSQL(
            """CREATE TABLE equipment (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                space_id INTEGER NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
                name TEXT NOT NULL,
                status TEXT NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE academic_classes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                subject TEXT NOT NULL,
                teacher TEXT NOT NULL,
                space_id INTEGER NOT NULL,
                space_name TEXT NOT NULL,
                day_of_week INTEGER NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                students INTEGER NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE reservations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                space_id INTEGER NOT NULL,
                space_name TEXT NOT NULL,
                requester_id INTEGER NOT NULL,
                requester_name TEXT NOT NULL,
                activity TEXT NOT NULL,
                attendees INTEGER NOT NULL,
                date TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                status TEXT NOT NULL,
                rejection_reason TEXT,
                status_notified INTEGER NOT NULL DEFAULT 1
            )"""
        )
        seed(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Versión 1: aún no hay migraciones. Cuando cambie el esquema, subir DB_VERSION y migrar aquí.
    }

    private fun seed(db: SQLiteDatabase) {
        fun user(name: String, email: String, password: String, profile: String) {
            val salt = PasswordHasher.newSalt()
            db.insert(
                "users", null,
                contentValuesOf(
                    "name" to name, "email" to email,
                    "password_hash" to PasswordHasher.hash(password, salt),
                    "salt" to salt, "profile" to profile
                )
            )
        }
        user("Coordinador de espacios", "admin@instituto.cl", "Admin1234", "ADMIN")
        user("Docente de prueba", "docente@instituto.cl", "Docente123", "TEACHER")

        fun space(code: String, name: String, capacity: Int, location: String = ""): Long =
            db.insert(
                "spaces", null,
                contentValuesOf("code" to code, "name" to name, "capacity" to capacity, "location" to location)
            )

        val teatro = space("TEA-01", "Teatro", 120, "Edificio principal")
        space("EST-01", "Estudio de grabación", 15)
        space("TAL-01", "Taller de diseño", 30)
        space("SAL-01", "Sala común", 40)

        db.insert("equipment", null, contentValuesOf("space_id" to teatro, "name" to "Proyector", "status" to "AVAILABLE"))
        db.insert("equipment", null, contentValuesOf("space_id" to teatro, "name" to "Micrófonos", "status" to "AVAILABLE"))

        // Clases de ejemplo (en espacios distintos del Teatro)
        db.insert(
            "academic_classes", null,
            contentValuesOf(
                "subject" to "Diseño Gráfico I", "teacher" to "Prof. Rojas",
                "space_id" to 3, "space_name" to "Taller de diseño",
                "day_of_week" to 1, "start_time" to "08:30", "end_time" to "10:00", "students" to 25
            )
        )
        db.insert(
            "academic_classes", null,
            contentValuesOf(
                "subject" to "Producción Audiovisual", "teacher" to "Prof. Soto",
                "space_id" to 2, "space_name" to "Estudio de grabación",
                "day_of_week" to 3, "start_time" to "14:00", "end_time" to "16:00", "students" to 12
            )
        )

        // Dos solicitudes que se solapan en el Teatro (11:00–12:00) para probar la regla de aprobación
        val tomorrow = LocalDate.now().plusDays(1).toString()
        fun reservation(requesterId: Int, requester: String, activity: String, attendees: Int, start: String, end: String) {
            db.insert(
                "reservations", null,
                contentValuesOf(
                    "space_id" to teatro, "space_name" to "Teatro",
                    "requester_id" to requesterId, "requester_name" to requester,
                    "activity" to activity, "attendees" to attendees,
                    "date" to tomorrow, "start_time" to start, "end_time" to end,
                    "status" to "PENDING", "status_notified" to 1
                )
            )
        }
        reservation(2, "Docente de prueba", "Ensayo de obra", 30, "10:00", "12:00")
        reservation(90, "Ana Pérez (Administrativo)", "Charla de bienvenida", 80, "11:00", "13:00")
    }

    companion object {
        const val DB_NAME = "reserva_espacios.db"
        const val DB_VERSION = 1
    }
}
