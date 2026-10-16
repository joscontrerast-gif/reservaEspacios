package com.example.reservaespacios.validation

/** Validaciones del registro de espacios. Funciones puras: null = válido, texto = mensaje de error. */
object SpaceValidators {

    private val codeRegex = Regex("^[A-Za-z0-9-]{2,10}$")

    /** [existingCodes] debe venir en mayúsculas y sin espacios. */
    fun validateCode(value: String, existingCodes: Set<String>): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Ingresa el código del espacio"
            !codeRegex.matches(text) -> "Usa de 2 a 10 letras, números o guiones"
            text.uppercase() in existingCodes -> "Ya existe un espacio con este código"
            else -> null
        }
    }

    fun validateName(value: String): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Ingresa el nombre del espacio"
            text.length < 3 -> "Escribe al menos 3 caracteres"
            text.length > 50 -> "Máximo 50 caracteres"
            else -> null
        }
    }

    fun validateCapacity(value: String): String? {
        val number = value.trim().toIntOrNull()
        return when {
            value.isBlank() -> "Indica la capacidad máxima"
            number == null -> "Ingresa solo números"
            number < 1 -> "Debe ser al menos 1"
            number > 1000 -> "Máximo 1000 personas"
            else -> null
        }
    }

    /** Ubicación opcional: solo se limita el largo. */
    fun validateLocation(value: String): String? =
        if (value.trim().length > 60) "Máximo 60 caracteres" else null

    fun validateEquipmentName(value: String, existingNames: List<String>): String? {
        val text = value.trim()
        return when {
            text.isEmpty() -> "Escribe el nombre del equipo"
            text.length < 2 -> "Escribe al menos 2 caracteres"
            text.length > 40 -> "Máximo 40 caracteres"
            existingNames.any { it.equals(text, ignoreCase = true) } -> "Este equipo ya fue agregado"
            else -> null
        }
    }
}