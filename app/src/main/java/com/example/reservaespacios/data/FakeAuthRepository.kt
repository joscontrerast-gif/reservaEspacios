package com.example.reservaespacios.data

import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile
import kotlinx.coroutines.delay

/** Repositorio en memoria para poder probar el flujo sin base de datos. Se pierde al cerrar la app. */
class FakeAuthRepository : AuthRepository {

    // Cuentas de prueba
    private val users = mutableListOf(
        User(1, "Coordinador de espacios", "admin@instituto.cl", "Admin1234", UserProfile.ADMIN),
        User(2, "Docente de prueba", "docente@instituto.cl", "Docente123", UserProfile.TEACHER)
    )

    override suspend fun login(email: String, password: String): AuthResult {
        delay(600) // simula latencia
        val user = users.firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
        }
        return if (user != null) AuthResult.Success(user)
        else AuthResult.Failure("Correo o contraseña incorrectos")
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        profile: UserProfile
    ): AuthResult {
        delay(600)
        if (users.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return AuthResult.Failure("Este correo ya está registrado")
        }
        val user = User(
            id = (users.maxOfOrNull { it.id } ?: 0) + 1,
            name = name.trim(),
            email = email.trim(),
            password = password,
            profile = profile
        )
        users += user
        return AuthResult.Success(user)
    }
}