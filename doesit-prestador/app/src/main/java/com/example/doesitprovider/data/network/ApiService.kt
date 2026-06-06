package com.example.doesitprovider.data.network

import com.example.doesitprovider.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Senha esquecida (REST) ────────────────────────────────────────────────
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<Map<String, String>>

    // ── Lambda: Perfil do usuário logado ─────────────────────────────────────
    @GET("usuario")
    suspend fun getCurrentUser(): Response<AuthResponse>

    @PUT("api/users/me")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<AuthResponse>

    @PUT("api/users/me/password")
    suspend fun changePassword(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<Map<String, String>>

    @DELETE("api/users/me")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<Unit>

    // ── Lambda: Endereços — mesmos endpoints/payloads do app Usuário ─────────
    @GET("meus-enderecos")
    suspend fun getAddresses(): Response<AddressListResponse>

    @POST("cadastrar-endereco")
    suspend fun createAddress(@Body body: Map<String, String>): Response<AddressDTO>

    @PUT("atualizar-endereco")
    suspend fun updateAddress(@Query("id") id: Long, @Body body: Map<String, String>): Response<AddressDTO>

    // ── Lambda: Notificações — mesmo endpoint/payload do app Usuário ─────────
    @GET("minhas-notificacoes")
    suspend fun getNotifications(): Response<NotificationListResponse>

    // ── Lambda: Pedidos/Serviços — mesmos endpoints/payloads do app Usuário ──
    @GET("meus-pedidos")
    suspend fun getMyHistory(@Query("status") status: String? = null): Response<OrderListResponse>

    @GET("detalhes-pedido")
    suspend fun getRequestById(@Query("id") id: Long): Response<ServiceRequestDTO>

    // ── Prestador: ações específicas sobre um pedido ──────────────────────────
    @PUT("api/requests/{id}/accept")
    suspend fun acceptRequest(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    @PUT("api/requests/{id}/refuse")
    suspend fun refuseRequest(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    @PUT("api/requests/{id}/start")
    suspend fun startRequest(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    @PUT("api/requests/{id}/cancel")
    suspend fun cancelRequest(@Header("Authorization") token: String, @Path("id") id: Long): Response<ServiceRequestDTO>

    // ── Prestador: status online ──────────────────────────────────────────────
    @PUT("api/providers/status")
    suspend fun setStatus(@Header("Authorization") token: String, @Body body: SetOnlineRequest): Response<Map<String, Any>>

    // ── Prestador: especialidades ─────────────────────────────────────────────
    @GET("api/providers/specialties")
    suspend fun getMySpecialties(@Header("Authorization") token: String): Response<List<ProviderSpecialtyDTO>>

    @POST("api/providers/specialties")
    suspend fun upsertSpecialty(@Header("Authorization") token: String, @Body body: Map<String, Any>): Response<ProviderSpecialtyDTO>

    @DELETE("api/providers/specialties/{categoryId}")
    suspend fun deleteSpecialty(@Header("Authorization") token: String, @Path("categoryId") categoryId: Long): Response<Unit>

    // ── Fluxo de Especialidades (Novos Endpoints) ──────────────────────────────
    @GET("retorna-especialidades")
    suspend fun getSpecialtiesFlow(@Header("Authorization") token: String): Response<SpecialtyResponse>

    @POST("salva-especialidades")
    suspend fun saveSpecialtiesFlow(@Header("Authorization") token: String, @Body body: SaveSpecialtiesRequest): Response<Unit>

    // ── Prestador: avaliações recebidas ───────────────────────────────────────
    @POST("api/ratings/user")
    suspend fun rateUser(@Header("Authorization") token: String, @Body body: RatingRequest): Response<Any>

    @GET("api/ratings/my-received")
    suspend fun getMyReceivedRatings(@Header("Authorization") token: String): Response<List<RatingResponseDTO>>
}
