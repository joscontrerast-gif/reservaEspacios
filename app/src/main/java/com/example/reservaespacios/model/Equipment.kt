package com.example.reservaespacios.model

enum class EquipmentStatus(val label: String) {
    AVAILABLE("Disponible"),
    OUT_OF_SERVICE("Fuera de servicio"),
    MAINTENANCE("En mantenimiento")
}

data class Equipment(
    val name: String,
    val status: EquipmentStatus
)