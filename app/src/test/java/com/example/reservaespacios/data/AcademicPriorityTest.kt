package com.example.reservaespacios.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

/** Comprueba la regla "las clases tienen prioridad sobre las reservas". */
class AcademicPriorityTest {

    @Test
    fun a_class_blocks_approving_a_reservation_in_that_slot() {
        runBlocking {
            val schedule = FakeScheduleRepository()
            val reservations = FakeReservationRepository(schedule)

            // Solicitud 1: Teatro, mañana, 10:00-12:00. Se agrega una clase ese mismo día de la semana.
            val request = reservations.reservations.value.first { it.id == 1 }
            val added = schedule.addClass(
                ClassDraft(
                    spaceId = request.spaceId, spaceName = request.spaceName,
                    dayOfWeek = request.date.dayOfWeek,
                    subject = "Teatro I", teacher = "Prof. Muñoz",
                    startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 0), students = 20
                )
            )
            assertTrue(added is ScheduleSaveResult.Success)

            assertTrue(reservations.approve(1) is ReservationActionResult.Failure)
        }
    }

    @Test
    fun removing_the_class_frees_the_slot() {
        runBlocking {
            val schedule = FakeScheduleRepository()
            val reservations = FakeReservationRepository(schedule)

            val request = reservations.reservations.value.first { it.id == 1 }
            schedule.addClass(
                ClassDraft(
                    spaceId = request.spaceId, spaceName = request.spaceName,
                    dayOfWeek = request.date.dayOfWeek,
                    subject = "Teatro I", teacher = "Prof. Muñoz",
                    startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 0), students = 20
                )
            )
            val classId = schedule.classes.value.maxOf { it.id }
            schedule.removeClass(classId)

            assertTrue(reservations.approve(1) is ReservationActionResult.Success)
        }
    }

    @Test
    fun two_classes_cannot_overlap_in_the_same_space_and_day() {
        runBlocking {
            val schedule = FakeScheduleRepository()
            val existing = schedule.classes.value.first()

            val result = schedule.addClass(
                ClassDraft(
                    spaceId = existing.spaceId, spaceName = existing.spaceName,
                    dayOfWeek = existing.dayOfWeek,
                    subject = "Otra clase", teacher = "Prof. Díaz",
                    startTime = existing.startTime.plusMinutes(30), endTime = existing.endTime.plusHours(1),
                    students = 10
                )
            )
            assertTrue(result is ScheduleSaveResult.Failure)
        }
    }
}