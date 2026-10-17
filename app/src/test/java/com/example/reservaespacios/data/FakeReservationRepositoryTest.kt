package com.example.reservaespacios.data

import com.example.reservaespacios.model.ReservationStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Usa los datos de ejemplo: las solicitudes 1 y 2 piden el mismo espacio y se solapan. */
class FakeReservationRepositoryTest {

    @Test
    fun approving_an_overlapping_request_is_rejected() {
        runBlocking {
            val repo = FakeReservationRepository()

            assertTrue(repo.approve(1) is ReservationActionResult.Success)
            assertTrue(repo.approve(2) is ReservationActionResult.Failure)

            val second = repo.reservations.value.first { it.id == 2 }
            assertEquals(ReservationStatus.PENDING, second.status) // sigue pendiente, no se bloqueó
        }
    }

    @Test
    fun rejecting_stores_the_reason_and_frees_the_slot() {
        runBlocking {
            val repo = FakeReservationRepository()

            assertTrue(repo.reject(1, "  Espacio en mantenimiento  ") is ReservationActionResult.Success)
            val first = repo.reservations.value.first { it.id == 1 }
            assertEquals(ReservationStatus.REJECTED, first.status)
            assertEquals("Espacio en mantenimiento", first.rejectionReason)

            // al estar rechazada, ya no bloquea el horario: la solicitud 2 sí se puede aprobar
            assertTrue(repo.approve(2) is ReservationActionResult.Success)
        }
    }

    @Test
    fun an_already_resolved_request_cannot_be_resolved_again() {
        runBlocking {
            val repo = FakeReservationRepository()
            repo.approve(1)
            assertTrue(repo.reject(1, "Motivo cualquiera largo") is ReservationActionResult.Failure)
        }
    }
}