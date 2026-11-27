package com.example.reservaespacios.data

import com.example.reservaespacios.data.remote.ExternalHolidayApiService
import com.example.reservaespacios.data.remote.PublicHolidayDto
import com.example.reservaespacios.model.Holiday
import com.google.gson.JsonParseException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeParseException

sealed interface HolidayResult {
    data class Success(val holidays: List<Holiday>) : HolidayResult
    data class Error(val message: String) : HolidayResult
}

interface HolidayRepository {
    suspend fun getUpcomingHolidays(today: LocalDate = LocalDate.now()): HolidayResult
}

class RemoteHolidayRepository(
    private val api: ExternalHolidayApiService
) : HolidayRepository {

    override suspend fun getUpcomingHolidays(today: LocalDate): HolidayResult {
        return try {
            val holidays = (fetchYear(today.year) + fetchYear(today.year + 1))
                .map { it.toDomain() }
                .filter { !it.date.isBefore(today) }
                .distinctBy { it.date }
                .sortedBy { it.date }
            HolidayResult.Success(holidays)
        } catch (e: IOException) {
            HolidayResult.Error("Sin conexión a internet. Revisa tu red e inténtalo de nuevo.")
        } catch (e: HolidayHttpException) {
            HolidayResult.Error("El servicio de feriados respondió con error ${e.code}.")
        } catch (e: DateTimeParseException) {
            HolidayResult.Error("Los datos recibidos tienen un formato inválido.")
        } catch (e: JsonParseException) {
            HolidayResult.Error("Los datos recibidos tienen un formato inválido.")
        }
    }

    private suspend fun fetchYear(year: Int): List<PublicHolidayDto> {
        val response = api.getChileHolidays(year)
        if (!response.isSuccessful) throw HolidayHttpException(response.code())
        return response.body().orEmpty()
    }

    private fun PublicHolidayDto.toDomain() = Holiday(
        date = LocalDate.parse(date),
        name = localName.ifBlank { name }
    )
}

class HolidayHttpException(val code: Int) : Exception("HTTP $code")
