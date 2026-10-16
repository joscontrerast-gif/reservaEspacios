package com.example.reservaespacios.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SpaceValidatorsTest {

    private val existing = setOf("TEA-01", "SAL-01")

    @Test fun new_code_is_valid() = assertNull(SpaceValidators.validateCode("TEA-02", existing))

    @Test fun duplicate_code_is_invalid_ignoring_case() =
        assertEquals("Ya existe un espacio con este código", SpaceValidators.validateCode("tea-01", existing))

    @Test fun code_with_spaces_inside_is_invalid() =
        assertNotNull(SpaceValidators.validateCode("TEA 02", existing))

    @Test fun blank_code_is_invalid() = assertNotNull(SpaceValidators.validateCode("  ", existing))

    @Test fun short_name_is_invalid() = assertNotNull(SpaceValidators.validateName("Ab"))
    @Test fun valid_name_passes() = assertNull(SpaceValidators.validateName("Sala de ensayo"))

    @Test fun capacity_zero_is_invalid() = assertNotNull(SpaceValidators.validateCapacity("0"))
    @Test fun capacity_text_is_invalid() = assertEquals("Ingresa solo números", SpaceValidators.validateCapacity("diez"))
    @Test fun capacity_over_limit_is_invalid() = assertNotNull(SpaceValidators.validateCapacity("1001"))
    @Test fun capacity_valid_passes() = assertNull(SpaceValidators.validateCapacity("50"))

    @Test fun empty_location_is_allowed() = assertNull(SpaceValidators.validateLocation(""))

    @Test fun duplicate_equipment_is_invalid() =
        assertEquals(
            "Este equipo ya fue agregado",
            SpaceValidators.validateEquipmentName("proyector", listOf("Proyector"))
        )

    @Test fun blank_equipment_is_invalid() =
        assertNotNull(SpaceValidators.validateEquipmentName("", emptyList()))

    @Test fun new_equipment_is_valid() =
        assertNull(SpaceValidators.validateEquipmentName("Parlantes", listOf("Proyector")))
}