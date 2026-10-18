package com.example.reservaespacios.data

import com.example.reservaespacios.data.local.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun correct_password_is_verified() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("Admin1234", salt)
        assertTrue(PasswordHasher.verify("Admin1234", salt, hash))
    }

    @Test
    fun wrong_password_is_rejected() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("Admin1234", salt)
        assertFalse(PasswordHasher.verify("admin1234", salt, hash))
    }

    @Test
    fun hash_never_contains_the_plain_password_and_depends_on_the_salt() {
        val hashA = PasswordHasher.hash("Docente123", PasswordHasher.newSalt())
        val hashB = PasswordHasher.hash("Docente123", PasswordHasher.newSalt())
        assertFalse(hashA.contains("Docente123"))
        assertNotEquals(hashA, hashB) // misma clave, sal distinta -> hash distinto
        assertEquals(64, hashA.length) // 256 bits en hexadecimal
    }
}
