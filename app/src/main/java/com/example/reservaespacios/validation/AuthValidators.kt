package com.example.reservaespacios.validation

import com.example.reservaespacios.model.UserProfile

/** Validaciones de login y registro. Funciones puras: null = válido, texto = mensaje de error. */
object AuthValidators {

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateName(value: String): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Ingresa tu nombre"
            text.length < 3 -> "Escribe al menos 3 caracteres"
            text.length > 60 -> "Máximo 60 caracteres"
            else -> null
        }
    }

    fun validateEmail(value: String): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Ingresa tu correo"
            !emailRegex.matches(text) -> "Correo inválido"
            else -> null
        }
    }

    /** En el login solo se exige que no esté vacía (las reglas de formato aplican al crearla). */
    fun validateLoginPassword(value: String): String? =
        if (value.isEmpty()) "Ingresa tu contraseña" else null

    fun validateNewPassword(value: String): String? = when {
        value.isEmpty() -> "Crea una contraseña"
        value.length < 8 -> "Mínimo 8 caracteres"
        value.none { it.isLetter() } -> "Debe incluir al menos una letra"
        value.none { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Repite la contraseña"
        confirm != password -> "Las contraseñas no coinciden"
        else -> null
    }

    fun validateProfile(profile: UserProfile?): String? =
        if (profile == null) "Selecciona tu perfil" else null
}