package com.example.reservaespacios.nativeres

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.reservaespacios.MainActivity
import com.example.reservaespacios.R
import com.example.reservaespacios.data.ReservationRepository
import com.example.reservaespacios.model.Reservation
import com.example.reservaespacios.model.ReservationStatus
import com.example.reservaespacios.model.User
import com.example.reservaespacios.model.UserProfile

/**
 * Recurso nativo: notificaciones locales (NotificationManager + permiso POST_NOTIFICATIONS en Android 13+).
 *
 * - Solicitante: al iniciar sesión se le avisa de las solicitudes que el administrador aprobó o rechazó.
 * - Administrador: al iniciar sesión se le avisa cuántas solicitudes tiene pendientes.
 */
class ReservationNotifier(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    init {
        // Desde Android 8 toda notificación necesita un canal.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Estado de solicitudes", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Avisos sobre solicitudes de espacios" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    /** Revisa en la base de datos qué hay que avisarle a este usuario y lo notifica. */
    suspend fun notifyOnLogin(user: User, repository: ReservationRepository) {
        if (!hasPermission()) return
        if (user.profile == UserProfile.ADMIN) {
            val pending = repository.pendingCount()
            if (pending > 0) showPendingSummary(pending)
        } else {
            val changes = repository.unnotifiedFor(user.id)
            changes.forEach { showStatusChange(it) }
            repository.markNotified(changes.map { it.id })
        }
    }

    @SuppressLint("MissingPermission") // se verifica en hasPermission()
    private fun showStatusChange(reservation: Reservation) {
        val approved = reservation.status == ReservationStatus.APPROVED
        val title = if (approved) "Solicitud aprobada" else "Solicitud rechazada"
        val text = buildString {
            append("${reservation.spaceName} · ${reservation.activity} (${reservation.date} ${reservation.startTime})")
            if (!approved && !reservation.rejectionReason.isNullOrBlank()) {
                append(". Motivo: ${reservation.rejectionReason}")
            }
        }
        show(NOTIFICATION_BASE + reservation.id, title, text)
    }

    @SuppressLint("MissingPermission")
    private fun showPendingSummary(count: Int) {
        val text = if (count == 1) "Hay 1 solicitud esperando tu revisión." else "Hay $count solicitudes esperando tu revisión."
        show(NOTIFICATION_PENDING, "Solicitudes pendientes", text)
    }

    @SuppressLint("MissingPermission")
    private fun show(id: Int, title: String, text: String) {
        val openApp = PendingIntent.getActivity(
            context, id,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    private companion object {
        const val CHANNEL_ID = "reservas_estado"
        const val NOTIFICATION_PENDING = 1
        const val NOTIFICATION_BASE = 1000
    }
}
