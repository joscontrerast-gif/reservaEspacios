package com.example.reservaespacios.data

import com.example.reservaespacios.data.remote.ExternalHolidayApiService
import com.example.reservaespacios.data.remote.PublicHolidayDto
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate

class RemoteHolidayRepositoryTest {

    private val today = LocalDate.of(2026, 11, 26)

    private class FakeHolidayApi(
        private val answer: (Int) -> Response<List<PublicHolidayDto>>
    ) : ExternalHolidayApiService {
        override suspend fun getChileHolidays(year: Int): Response<List<PublicHolidayDto>> = answer(year)
    }

    @Test
    fun returns_only_upcoming_holidays_sorted_by_date() {
        runBlocking {
            val api = FakeHolidayApi { year ->
                if (year == 2026) {
                    Response.success(
                        listOf(
                            PublicHolidayDto("2026-12-25", "Navidad", "Christmas Day"),
                            PublicHolidayDto("2026-09-18", "Independencia Nacional", "Independence Day"),
                            PublicHolidayDto("2026-12-08", "Inmaculada Concepción", "Immaculate Conception")
                        )
                    )
                } else {
                    Response.success(listOf(PublicHolidayDto("2027-01-01", "Año Nuevo", "New Year's Day")))
                }
            }

            val result = RemoteHolidayRepository(api).getUpcomingHolidays(today)

            assertTrue(result is HolidayResult.Success)
            val names = (result as HolidayResult.Success).holidays.map { it.name }
            assertEquals(listOf("Inmaculada Concepción", "Navidad", "Año Nuevo"), names)
        }
    }

    @Test
    fun http_error_returns_error_message_with_code() {
        runBlocking {
            val api = FakeHolidayApi { Response.error(500, "".toResponseBody(null)) }

            val result = RemoteHolidayRepository(api).getUpcomingHolidays(today)

            assertTrue(result is HolidayResult.Error)
            assertTrue((result as HolidayResult.Error).message.contains("500"))
        }
    }

    @Test
    fun no_connection_returns_friendly_error() {
        runBlocking {
            val api = FakeHolidayApi { throw IOException("sin red") }

            val result = RemoteHolidayRepository(api).getUpcomingHolidays(today)

            assertTrue(result is HolidayResult.Error)
            assertTrue((result as HolidayResult.Error).message.contains("Sin conexión"))
        }
    }

    @Test
    fun invalid_date_format_is_handled() {
        runBlocking {
            val api = FakeHolidayApi {
                Response.success(listOf(PublicHolidayDto("25-12-2026", "Navidad", "Christmas Day")))
            }

            val result = RemoteHolidayRepository(api).getUpcomingHolidays(today)

            assertTrue(result is HolidayResult.Error)
            assertTrue((result as HolidayResult.Error).message.contains("formato"))
        }
    }
}
