package com.example.reservaespacios

import android.content.Context
import com.example.reservaespacios.data.AuthRepository
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.data.ScheduleRepository
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.data.SpaceRepository
import com.example.reservaespacios.data.local.AppDatabaseHelper
import com.example.reservaespacios.data.local.AppPreferences
import com.example.reservaespacios.data.local.SqliteAuthRepository
import com.example.reservaespacios.data.local.SqliteReservationRepository
import com.example.reservaespacios.data.local.SqliteScheduleRepository
import com.example.reservaespacios.data.local.SqliteSpaceRepository
import com.example.reservaespacios.nativeres.ReservationNotifier

/**
 * Instancias compartidas (sin librería de inyección por ahora).
 * Se inicializa una sola vez en [ReservaApp.onCreate], antes de que exista cualquier pantalla.
 * Los repositorios SQLite reemplazan a los Fake*Repository, que quedan solo para las pruebas unitarias.
 */
object AppContainer {
    lateinit var preferences: AppPreferences
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var sessionStore: SessionStore
        private set
    lateinit var spaceRepository: SpaceRepository
        private set
    lateinit var scheduleRepository: ScheduleRepository
        private set
    lateinit var reservationRepository: ReservationRepository
        private set
    lateinit var notifier: ReservationNotifier
        private set

    fun init(context: Context) {
        val appContext = context.applicationContext
        val helper = AppDatabaseHelper(appContext)
        preferences = AppPreferences(appContext)
        sessionStore = SessionStore(preferences)
        authRepository = SqliteAuthRepository(helper)
        spaceRepository = SqliteSpaceRepository(helper)
        scheduleRepository = SqliteScheduleRepository(helper)
        reservationRepository = SqliteReservationRepository(helper)
        notifier = ReservationNotifier(appContext)
    }
}
