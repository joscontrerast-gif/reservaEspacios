package com.example.reservaespacios.model

/**
 * Privilegios de cada perfil de usuario.
 *
 * Clase base sellada con una subclase por perfil: cada una redefine (override) sus propios
 * límites y la app le pregunta a la política qué puede hacer el usuario, en lugar de repetir
 * "si es estudiante... si es docente..." en cada pantalla (polimorfismo).
 *
 * Para los límites numéricos, `null` significa "sin límite".
 */
sealed class RolePolicy(val profile: UserProfile) {

    /** Solicitudes activas (pendientes o aprobadas que aún no ocurren) permitidas a la vez. */
    abstract val maxActiveRequests: Int?

    /** Días mínimos de anticipación para solicitar (0 = se puede pedir para hoy). */
    abstract val minAdvanceDays: Int

    /** Días máximos de anticipación para solicitar. */
    abstract val maxAdvanceDays: Int?

    /** Duración máxima de una reserva, en minutos. */
    abstract val maxDurationMinutes: Int?

    /** Puede enviar solicitudes de espacios. */
    open val canRequestSpaces: Boolean get() = true

    /** Puede ver la agenda general (reservas aprobadas de todos los espacios). */
    open val canViewAgenda: Boolean get() = false

    /** Puede aprobar/rechazar solicitudes y administrar espacios y horarios. */
    open val canManage: Boolean get() = false

    /** Lista de límites en texto, para mostrarle al usuario qué permite su perfil. */
    fun summary(): List<String> = buildList {
        maxActiveRequests?.let { add("Hasta $it solicitudes activas a la vez") }
        if (minAdvanceDays > 0) add("Solicitar con al menos $minAdvanceDays día(s) de anticipación")
        maxAdvanceDays?.let { add("Reservar hasta $it días antes") }
        maxDurationMinutes?.let { add("Reservas de hasta ${formatDuration(it)}") }
    }

    companion object {
        /** Devuelve la política que corresponde a un perfil. */
        fun of(profile: UserProfile): RolePolicy = when (profile) {
            UserProfile.STUDENT -> StudentPolicy
            UserProfile.TEACHER -> TeacherPolicy
            UserProfile.STAFF -> StaffPolicy
            UserProfile.ADMIN -> AdminPolicy
        }

        fun formatDuration(minutes: Int): String {
            val h = minutes / 60
            val m = minutes % 60
            return when {
                h == 0 -> "$m min"
                m == 0 -> "$h h"
                else -> "$h h $m min"
            }
        }
    }
}

/** Estudiante: el más acotado. Pocas solicitudes, con anticipación y reservas cortas. */
object StudentPolicy : RolePolicy(UserProfile.STUDENT) {
    override val maxActiveRequests = 2
    override val minAdvanceDays = 1
    override val maxAdvanceDays = 14
    override val maxDurationMinutes = 120
}

/** Docente: puede planificar con más tiempo, pedir para el mismo día y reservar bloques más largos. */
object TeacherPolicy : RolePolicy(UserProfile.TEACHER) {
    override val maxActiveRequests = 5
    override val minAdvanceDays = 0
    override val maxAdvanceDays = 60
    override val maxDurationMinutes = 240
}

/** Administrativo: coordina actividades de la institución. Más cupo y acceso a la agenda general. */
object StaffPolicy : RolePolicy(UserProfile.STAFF) {
    override val maxActiveRequests = 10
    override val minAdvanceDays = 0
    override val maxAdvanceDays = 90
    override val maxDurationMinutes = 480
    override val canViewAgenda get() = true
}

/** Administrador: sin límites. No solicita espacios: los gestiona y resuelve las solicitudes. */
object AdminPolicy : RolePolicy(UserProfile.ADMIN) {
    override val maxActiveRequests: Int? = null
    override val minAdvanceDays = 0
    override val maxAdvanceDays: Int? = null
    override val maxDurationMinutes: Int? = null
    override val canRequestSpaces get() = false
    override val canViewAgenda get() = true
    override val canManage get() = true
}
