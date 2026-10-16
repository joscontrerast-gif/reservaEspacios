package com.example.reservaespacios.data

import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Failure(val message: String) : AuthResult
}

/** Contrato que usan los ViewModels. Hoy lo implementa FakeAuthRepository; mañana, Room. */
interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(name: String, email: String, password: String, profile: UserProfile): AuthResult
}