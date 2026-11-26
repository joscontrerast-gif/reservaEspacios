package com.example.reservaespacios.validation

import com.example.reservaespacios.model.AdminPolicy
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import com.example.reservaespacios.model.RolePolicy
import com.example.reservaespacios.model.StaffPolicy
import com.example.reservaespacios.model.StudentPolicy
import com.example.reservaespacios.model.TeacherPolicy
import com.example.reservaespacios.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RolePolicyRulesTest {

    private val today = LocalDate.of(2026, 10, 16)

    private fun reservation(
        id: Int = 1,
        requesterId: Int = 2,
        status: ReservationStatus = ReservationStatus.PENDING,
        date: LocalDate = today.plusDays(1)
    ) = Reservation(
        id = id, spaceId = 1, spaceName = "Teatro",
        requesterId = requesterId, requesterName = "Usuario",
        activity = "Ensayo", attendees = 10,
        date = date, startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 0),
        status = status
    )

    // --- Política por perfil ---

    @Test fun each_profile_maps_to_its_own_policy() {
        assertEquals(StudentPolicy, RolePolicy.of(UserProfile.STUDENT))
        assertEquals(TeacherPolicy, RolePolicy.of(UserProfile.TEACHER))
        assertEquals(StaffPolicy, RolePolicy.of(UserProfile.STAFF))
        assertEquals(AdminPolicy, RolePolicy.of(UserProfile.ADMIN))
    }

    @Test fun limits_grow_with_the_role() {
        assertTrue(StudentPolicy.maxActiveRequests < TeacherPolicy.maxActiveRequests)
        assertTrue(TeacherPolicy.maxActiveRequests < StaffPolicy.maxActiveRequests)
        assertTrue(StudentPolicy.maxDurationMinutes < TeacherPolicy.maxDurationMinutes)
    }

    @Test fun only_staff_and_admin_see_the_agenda() {
        assertFalse(StudentPolicy.canViewAgenda)
        assertFalse(TeacherPolicy.canViewAgenda)
        assertTrue(StaffPolicy.canViewAgenda)
        assertTrue(AdminPolicy.canViewAgenda)
    }

    @Test fun only_admin_manages_and_does_not_request() {
        assertTrue(AdminPolicy.canManage)
        assertFalse(AdminPolicy.canRequestSpaces)
        listOf(StudentPolicy, TeacherPolicy, StaffPolicy).forEach {
            assertFalse(it.canManage)
            assertTrue(it.canRequestSpaces)
        }
    }

    @Test fun admin_has_no_limits() {
        assertNull(AdminPolicy.maxActiveRequests)
        assertNull(AdminPolicy.maxAdvanceDays)
        assertNull(AdminPolicy.maxDurationMinutes)
        assertTrue(AdminPolicy.summary().isEmpty())
    }

    @Test fun duration_is_formatted_for_people() {
        assertEquals("2 h", RolePolicy.formatDuration(120))
        assertEquals("1 h 30 min", RolePolicy.formatDuration(90))
        assertEquals("45 min", RolePolicy.formatDuration(45))
    }

    // --- Fecha (anticipación) ---

    @Test fun student_cannot_request_for_today() =
        assertNotNull(RolePolicyRules.validateDate(StudentPolicy, today, today))

    @Test fun teacher_can_request_for_today() =
        assertNull(RolePolicyRules.validateDate(TeacherPolicy, today, today))

    @Test fun student_can_request_for_tomorrow() =
        assertNull(RolePolicyRules.validateDate(StudentPolicy, today.plusDays(1), today))

    @Test fun student_cannot_request_beyond_14_days() =
        assertNotNull(RolePolicyRules.validateDate(StudentPolicy, today.plusDays(15), today))

    @Test fun student_can_request_exactly_14_days_ahead() =
        assertNull(RolePolicyRules.validateDate(StudentPolicy, today.plusDays(14), today))

    @Test fun same_far_date_is_fine_for_staff() =
        assertNull(RolePolicyRules.validateDate(StaffPolicy, today.plusDays(80), today))

    @Test fun no_policy_means_no_date_restriction() =
        assertNull(RolePolicyRules.validateDate(null, today.plusDays(500), today))

    // --- Duración ---

    @Test fun student_cannot_book_more_than_2_hours() =
        assertNotNull(RolePolicyRules.validateDuration(StudentPolicy, LocalTime.of(9, 0), LocalTime.of(11, 30)))

    @Test fun student_can_book_exactly_2_hours() =
        assertNull(RolePolicyRules.validateDuration(StudentPolicy, LocalTime.of(9, 0), LocalTime.of(11, 0)))

    @Test fun teacher_can_book_4_hours_but_not_5() {
        assertNull(RolePolicyRules.validateDuration(TeacherPolicy, LocalTime.of(9, 0), LocalTime.of(13, 0)))
        assertNotNull(RolePolicyRules.validateDuration(TeacherPolicy, LocalTime.of(9, 0), LocalTime.of(14, 0)))
    }

    @Test fun admin_has_no_duration_limit() =
        assertNull(RolePolicyRules.validateDuration(AdminPolicy, LocalTime.of(0, 0), LocalTime.of(23, 59)))

    // --- Cupo de solicitudes activas ---

    @Test fun counts_only_active_requests_of_that_user() {
        val list = listOf(
            reservation(id = 1, status = ReservationStatus.PENDING),
            reservation(id = 2, status = ReservationStatus.APPROVED),
            reservation(id = 3, status = ReservationStatus.REJECTED),
            reservation(id = 4, status = ReservationStatus.CANCELLED),
            reservation(id = 5, status = ReservationStatus.APPROVED, date = today.minusDays(3)),
            reservation(id = 6, requesterId = 99)
        )
        assertEquals(2, RolePolicyRules.countActive(list, userId = 2, today = today))
    }

    @Test fun student_is_blocked_at_the_limit() {
        assertNull(RolePolicyRules.validateActiveRequests(StudentPolicy, 1))
        assertNotNull(RolePolicyRules.validateActiveRequests(StudentPolicy, 2))
    }

    @Test fun same_count_is_fine_for_teacher() =
        assertNull(RolePolicyRules.validateActiveRequests(TeacherPolicy, 2))

    @Test fun admin_is_never_blocked_by_quota() =
        assertNull(RolePolicyRules.validateActiveRequests(AdminPolicy, 1000))

    // --- Agenda ---

    @Test fun agenda_shows_only_upcoming_approved_sorted() {
        val list = listOf(
            reservation(id = 1, status = ReservationStatus.APPROVED, date = today.plusDays(3)),
            reservation(id = 2, status = ReservationStatus.APPROVED, date = today.plusDays(1)),
            reservation(id = 3, status = ReservationStatus.PENDING, date = today.plusDays(2)),
            reservation(id = 4, status = ReservationStatus.APPROVED, date = today.minusDays(1))
        )
        val agenda = ReservationRules.upcomingApproved(list, today)
        assertEquals(listOf(2, 1), agenda.map { it.id })
    }
}
