package com.example.reservaespacios.nativeres

import android.content.Intent
import android.provider.CalendarContract
import com.example.reservaespacios.model.Reservation
import java.time.ZoneId

/**
 * Recurso nativo: apps del teléfono mediante Intents.
 * No necesitan permisos: el usuario confirma la acción dentro de la app de Calendario / el selector de compartir.
 */
object ReservationIntents {

    /** Abre el Calendario del teléfono con el evento ya rellenado para que el usuario lo guarde. */
    fun addToCalendar(reservation: Reservation): Intent {
        val zone = ZoneId.systemDefault()
        val begin = reservation.date.atTime(reservation.startTime).atZone(zone).toInstant().toEpochMilli()
        val end = reservation.date.atTime(reservation.endTime).atZone(zone).toInstant().toEpochMilli()
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "${reservation.activity} · ${reservation.spaceName}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, reservation.spaceName)
            putExtra(
                CalendarContract.Events.DESCRIPTION,
                "Reserva aprobada. Responsable: ${reservation.requesterName}. Asistentes: ${reservation.attendees}."
            )
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
        }
    }

    /** Selector del sistema para compartir el detalle de la reserva (WhatsApp, correo, etc.). */
    fun share(reservation: Reservation): Intent {
        val text = "Reserva de ${reservation.spaceName}\n" +
                "Actividad: ${reservation.activity}\n" +
                "Fecha: ${reservation.date} de ${reservation.startTime} a ${reservation.endTime}\n" +
                "Asistentes: ${reservation.attendees}\n" +
                "Responsable: ${reservation.requesterName}"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Reserva de ${reservation.spaceName}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        return Intent.createChooser(send, "Compartir reserva")
    }
}
