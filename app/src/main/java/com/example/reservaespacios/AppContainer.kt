package com.example.reservaespacios

import com.example.reservaespacios.data.AuthRepository
import com.example.reservaespacios.data.FakeAuthRepository
import com.example.reservaespacios.data.FakeReservationRepository
import com.example.reservaespacios.data.FakeScheduleRepository
import com.example.reservaespacios.data.FakeSpaceRepository
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.data.ScheduleRepository
import com.example.reservaespacios.data.SessionStore
import com.example.reservaespacios.data.SpaceRepository

/** Instancias compartidas (sin librería de inyección por ahora). */
object AppContainer {
    val authRepository: AuthRepository = FakeAuthRepository()
    val sessionStore: SessionStore = SessionStore()
    val spaceRepository: SpaceRepository = FakeSpaceRepository()
    val scheduleRepository: ScheduleRepository = FakeScheduleRepository()

    // Las reservas consultan los horarios académicos para respetar la prioridad de las clases.
    val reservationRepository: ReservationRepository = FakeReservationRepository(scheduleRepository)
}