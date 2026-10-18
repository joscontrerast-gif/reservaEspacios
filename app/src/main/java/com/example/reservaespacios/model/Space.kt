package com.example.reservaespacios.model

/**
 * Los tres primeros campos son los mismos de antes, así que el código existente sigue compilando.
 */
data class Space(
    val id: Int,
    val name: String,
    val capacity: Int,
    val code: String = "",
    val location: String = "",
    val equipment: List<Equipment> = emptyList(),
    /** Ruta de la foto tomada con la cámara (archivo en el almacenamiento interno de la app). */
    val photoPath: String? = null
)