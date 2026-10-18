package com.example.reservaespacios.data.local

import android.content.Context
import androidx.core.content.edit

/**
 * Almacenamiento local clave-valor (SharedPreferences).
 * Guarda datos pequeños que deben sobrevivir al cierre de la app:
 *  - la sesión activa (id del usuario), para no pedir login cada vez;
 *  - el último correo usado, para precargarlo en el login.
 * Nunca se guarda la contraseña.
 */
class AppPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var sessionUserId: Int?
        get() = prefs.getInt(KEY_SESSION_USER, NONE).takeIf { it != NONE }
        set(value) = prefs.edit {
            if (value == null) remove(KEY_SESSION_USER) else putInt(KEY_SESSION_USER, value)
        }

    var lastEmail: String
        get() = prefs.getString(KEY_LAST_EMAIL, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LAST_EMAIL, value.trim()) }

    private companion object {
        const val FILE_NAME = "reserva_prefs"
        const val KEY_SESSION_USER = "session_user_id"
        const val KEY_LAST_EMAIL = "last_email"
        const val NONE = -1
    }
}
