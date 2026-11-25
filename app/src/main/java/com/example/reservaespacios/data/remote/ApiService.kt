package com.example.reservaespacios.data.remote

import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UserDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserDto>

    @POST("api/auth/recover-password")
    suspend fun recoverPassword(@Body request: RecoverPasswordRequest): Response<MessageResponse>

    @PUT("api/auth/users/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body request: UpdateProfileRequest): Response<UserDto>

    @GET("api/auth/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<UserDto>
}

interface SpacesApiService {
    @GET("api/spaces")
    suspend fun getSpaces(): Response<List<SpaceDto>>

    @POST("api/spaces")
    suspend fun createSpace(@Body space: SpaceDto): Response<SpaceDto>

    @GET("api/schedules")
    suspend fun getSchedules(): Response<List<AcademicClassDto>>

    @POST("api/schedules")
    suspend fun createSchedule(@Body schedule: AcademicClassDto): Response<AcademicClassDto>

    @DELETE("api/schedules/{id}")
    suspend fun deleteSchedule(@Path("id") id: Int): Response<Unit>
}

interface ReservationsApiService {
    @GET("api/reservations")
    suspend fun getReservations(): Response<List<ReservationDto>>

    @POST("api/reservations")
    suspend fun createReservation(@Body reservation: ReservationDto): Response<ReservationDto>

    @PUT("api/reservations/{id}/approve")
    suspend fun approveReservation(@Path("id") id: Int): Response<MessageResponse>

    @PUT("api/reservations/{id}/reject")
    suspend fun rejectReservation(@Path("id") id: Int, @Body request: RejectRequest): Response<MessageResponse>

    @PUT("api/reservations/{id}/cancel")
    suspend fun cancelReservation(@Path("id") id: Int): Response<MessageResponse>
}

// API Externa Pública (IE 3.1.3)
interface ExternalHolidayApiService {
    @GET("api/v3/PublicHolidays/{year}/CL")
    suspend fun getChileHolidays(@Path("year") year: Int): Response<List<PublicHolidayDto>>
}