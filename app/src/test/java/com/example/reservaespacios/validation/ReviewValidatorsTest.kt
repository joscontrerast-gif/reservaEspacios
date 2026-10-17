package com.example.reservaespacios.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewValidatorsTest {

    @Test fun empty_reason_is_invalid() =
        assertEquals("Indica el motivo del rechazo", ReviewValidators.validateRejectionReason("   "))

    @Test fun short_reason_is_invalid() =
        assertNotNull(ReviewValidators.validateRejectionReason("no"))

    @Test fun long_reason_is_invalid() =
        assertNotNull(ReviewValidators.validateRejectionReason("a".repeat(201)))

    @Test fun proper_reason_is_valid() =
        assertNull(ReviewValidators.validateRejectionReason("El espacio estará en mantenimiento ese día"))
}