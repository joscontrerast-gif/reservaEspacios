package com.example.reservaespacios.data.local

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Las contraseñas se guardan como hash PBKDF2 + sal aleatoria; nunca en texto plano. */
object PasswordHasher {
    private const val ITERATIONS = 10_000
    private const val KEY_BITS = 256

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return toHex(bytes)
    }

    fun hash(password: String, salt: String): String {
        val spec = PBEKeySpec(password.toCharArray(), fromHex(salt), ITERATIONS, KEY_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return toHex(factory.generateSecret(spec).encoded)
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(hash(password, salt).toByteArray(), expectedHash.toByteArray())

    private fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    private fun fromHex(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
