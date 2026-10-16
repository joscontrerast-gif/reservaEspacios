package com.example.reservaespacios.model

enum class UserProfile(val label: String) {
    STUDENT("Estudiante"),
    TEACHER("Docente"),
    STAFF("Administrativo"),
    ADMIN("Administrador");

    companion object {
        /** El administrador no se puede elegir al registrarse: lo crea la institución. */
        val selfRegistrable: List<UserProfile> = listOf(STUDENT, TEACHER, STAFF)
    }
}

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String, // TEMPORAL: al usar Room/backend se debe guardar un hash, no la contraseña
    val profile: UserProfile
)