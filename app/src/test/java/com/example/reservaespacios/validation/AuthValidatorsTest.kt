package com.example.reservaespacios.validation

import com.example.reservaespacios.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorsTest {

    @Test fun valid_email_passes() = assertNull(AuthValidators.validateEmail("ana@instituto.cl"))
    @Test fun email_without_at_fails() = assertNotNull(AuthValidators.validateEmail("ana.instituto.cl"))
    @Test fun email_with_spaces_around_is_trimmed() =
        assertNull(AuthValidators.validateEmail("  ana@instituto.cl  "))
    @Test fun empty_email_fails() = assertEquals("Ingresa tu correo", AuthValidators.validateEmail(""))

    @Test fun short_password_fails() = assertEquals("Mínimo 8 caracteres", AuthValidators.validateNewPassword("abc123"))
    @Test fun password_without_digit_fails() = assertNotNull(AuthValidators.validateNewPassword("abcdefgh"))
    @Test fun password_without_letter_fails() = assertNotNull(AuthValidators.validateNewPassword("12345678"))
    @Test fun strong_password_passes() = assertNull(AuthValidators.validateNewPassword("Clave1234"))

    @Test fun confirm_must_match() =
        assertEquals("Las contraseñas no coinciden", AuthValidators.validateConfirmPassword("Clave1234", "Clave12345"))
    @Test fun confirm_matching_passes() =
        assertNull(AuthValidators.validateConfirmPassword("Clave1234", "Clave1234"))

    @Test fun name_too_short_fails() = assertNotNull(AuthValidators.validateName("Al"))
    @Test fun missing_profile_fails() = assertNotNull(AuthValidators.validateProfile(null))
    @Test fun selected_profile_passes() = assertNull(AuthValidators.validateProfile(UserProfile.TEACHER))
}