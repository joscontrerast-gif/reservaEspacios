package com.example.reservaespacios.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // URLs para emulador Android Studio apuntando a tus 3 microservicios locales
    // Al subir a Render/Railway, reemplaza estas URLs por las de producción
    private const val AUTH_BASE_URL = "http://10.0.2.2:3001/"
    private const val SPACES_BASE_URL = "http://10.0.2.2:3002/"
    private const val RESERVATIONS_BASE_URL = "http://10.0.2.2:3003/"
    private const val EXTERNAL_HOLIDAYS_URL = "https://date.nager.at/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private fun buildRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApiService by lazy { buildRetrofit(AUTH_BASE_URL).create(AuthApiService::class.java) }
    val spacesApi: SpacesApiService by lazy { buildRetrofit(SPACES_BASE_URL).create(SpacesApiService::class.java) }
    val reservationsApi: ReservationsApiService by lazy { buildRetrofit(RESERVATIONS_BASE_URL).create(ReservationsApiService::class.java) }
    val externalHolidayApi: ExternalHolidayApiService by lazy { buildRetrofit(EXTERNAL_HOLIDAYS_URL).create(ExternalHolidayApiService::class.java) }
}