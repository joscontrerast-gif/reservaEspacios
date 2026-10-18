package com.example.reservaespacios.data.local

import androidx.core.content.contentValuesOf
import com.example.reservaespacios.data.AuthRepository
import com.example.reservaespacios.data.AuthResult
import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqliteAuthRepository(private val helper: AppDatabaseHelper) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val db = helper.readableDatabase
        db.rawQuery("SELECT * FROM users WHERE email = ?", arrayOf(email.trim())).use { c ->
            if (c.moveToFirst()) {
                val salt = c.getString(c.getColumnIndexOrThrow("salt"))
                val hash = c.getString(c.getColumnIndexOrThrow("password_hash"))
                if (PasswordHasher.verify(password, salt, hash)) {
                    return@withContext AuthResult.Success(c.toUser())
                }
            }
        }
        AuthResult.Failure("Correo o contraseña incorrectos")
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        profile: UserProfile
    ): AuthResult = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        val exists = db.rawQuery("SELECT 1 FROM users WHERE email = ?", arrayOf(email.trim()))
            .use { it.moveToFirst() }
        if (exists) return@withContext AuthResult.Failure("Este correo ya está registrado")

        val salt = PasswordHasher.newSalt()
        val id = db.insert(
            "users", null,
            contentValuesOf(
                "name" to name.trim(),
                "email" to email.trim(),
                "password_hash" to PasswordHasher.hash(password, salt),
                "salt" to salt,
                "profile" to profile.name
            )
        )
        if (id == -1L) return@withContext AuthResult.Failure("No se pudo guardar el usuario")
        AuthResult.Success(User(id.toInt(), name.trim(), email.trim(), "", profile))
    }

    override suspend fun findById(id: Int): User? = withContext(Dispatchers.IO) {
        helper.readableDatabase.rawQuery("SELECT * FROM users WHERE id = ?", arrayOf(id.toString()))
            .use { c -> if (c.moveToFirst()) c.toUser() else null }
    }
}
