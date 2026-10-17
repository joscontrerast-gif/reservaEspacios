package com.example.reservaespacios.validation

/** Validaciones de la revisión de solicitudes. null = válido, texto = mensaje de error. */
object ReviewValidators {

    fun validateRejectionReason(value: String): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Indica el motivo del rechazo"
            text.length < 10 -> "Explica el motivo con al menos 10 caracteres"
            text.length > 200 -> "Máximo 200 caracteres"
            else -> null
        }
    }
}