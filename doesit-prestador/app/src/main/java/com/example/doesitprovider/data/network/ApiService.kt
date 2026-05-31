package com.example.doesitprovider.data.network

import com.example.doesitprovider.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ── Auth ─────────────────────────────────────────────────────────────────
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // ── Usuário ───────────────────────────────────────────────────────────────
    @GET("api/users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<AuthResponse>

    @PUT("api/users/me")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<AuthResponse>

    @PUT("api/users/me/password")
    suspend fun changePassword(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<Map<String, String>>

    @DELETE("api/users/me")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<Unit>

    // ── Prestador ─────────────────────────────────────────────────────────────
    @PUT("api/providers/status")
    suspend fun setStatus(@Header("Authorization") token: String, @Body body: SetOnlineRequest): Response<Map<String, Any>>

    // ── Especialidades ────────────────────────────────────────────────────────
    @GET("api/providers/specialties")
    suspend fun getMySpecialties(@Header("Authorization") token: String): Response<List<ProviderSpecialtyDTO>>

    @POST("api/providers/specialties")
    suspend fun upsertSpecialty(@Header("Authorization") token: String, @Body body: Map<String, Any>): Response<ProviderSpecialtyDTO>

    @DELETE("api/providers/specialties/{categoryId}")
    suspend fun deleteSpecialty(@Header("Authorization") token: String, @Path("categoryId") categoryId: Long): Response<Unit>

    // ── Pedidos / Serviços ────────────────────────────────────────────────────
    @GET("api/requests/my")
    suspend fun getMyHistory(@Header("Authorization") token: String): Response<List<ServiceRequestDTO>>

    @GET("api/requests/{id}")
    suspend fun getRequestById(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    @PUT("api/requests/{id}/accept")
    suspend fun acceptRequest(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    @PUT("api/requests/{id}/refuse")
    suspend fun refuseRequest(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    @PUT("api/requests/{id}/start")
    suspend fun startRequest(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    @PUT("api/requests/{id}/cancel")
    suspend fun cancelRequest(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    // ── Avaliações ────────────────────────────────────────────────────────────
    @POST("api/ratings/user")
    suspend fun rateUser(@Header("Authorization") token: String, @Body body: RatingRequest): Response<Any>

    @GET("api/ratings/my-received")
    suspend fun getMyReceivedRatings(@Header("Authorization") token: String): Response<List<RatingResponseDTO>>

    // ── Endereços ─────────────────────────────────────────────────────────────
    @GET("api/addresses")
    suspend fun getAddresses(@Header("Authorization") token: String): Response<List<Map<String, Any>>>
}
