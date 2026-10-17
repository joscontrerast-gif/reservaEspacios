package com.example.reservaespacios.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/**
 * Conversiones entre los selectores de fecha/hora y el texto que guarda el estado del formulario.
 * Funciones puras (sin Android): así los validadores existentes siguen funcionando igual
 * y se pueden probar sin emulador.
 */
object DateTimeFormats {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val strictDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun formatTime(time: LocalTime): String = time.format(timeFormatter)

    fun parseDate(value: String): LocalDate? =
        try { LocalDate.parse(value.trim(), strictDateFormatter) } catch (e: DateTimeParseException) { null }

    fun parseTime(value: String): LocalTime? =
        try { LocalTime.parse(value.trim(), timeFormatter) } catch (e: DateTimeParseException) { null }

    /** El DatePicker de Material trabaja con milisegundos de la medianoche UTC del día elegido. */
    fun dateToPickerMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun pickerMillisToDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}