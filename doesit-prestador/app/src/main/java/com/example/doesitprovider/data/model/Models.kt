package com.example.doesitprovider.data.model

import com.google.gson.annotations.SerializedName

// ── DTO de cadastro (usado pela RegisterScreen) ───────────────────────────────
data class RegisterRequest(
    @SerializedName("name")                val name: String,
    @SerializedName("email")               val email: String,
    @SerializedName("password")            val password: String,
    @SerializedName("phone")               val phone: String,
    @SerializedName("cpf")                 val cpf: String,
    @SerializedName("birthDate")           val birthDate: String,
    @SerializedName("gender")              val gender: String,
    @SerializedName("role")                val role: String = "PROVIDER",
    @SerializedName("addressCep")          val addressCep: String = "",
    @SerializedName("addressStreet")       val addressStreet: String = "",
    @SerializedName("addressNumber")       val addressNumber: String = "",
    @SerializedName("addressNeighborhood") val addressNeighborhood: String = "",
    @SerializedName("addressCity")         val addressCity: String = "",
    @SerializedName("addressState")        val addressState: String = ""
)

// ── AuthResponse — SerializedNames = chaves JSON reais da Lambda GET /usuario ─
data class AuthResponse(
    @SerializedName("token")           val token: String? = null,
    @SerializedName("id")              val id: Long = 0,
    @SerializedName("nome")            val name: String? = null,
    @SerializedName("email")           val email: String? = null,
    @SerializedName("tipo")            val role: String? = null,
    @SerializedName("telefone")        val phone: String? = null,
    @SerializedName("cpf")             val cpf: String? = null,
    @SerializedName("data_nascimento") val birthDate: String? = null,
    @SerializedName("genero")          val gender: String? = null,
    @SerializedName("rating")          val rating: Double? = 0.0,
    @SerializedName("rating_count")    val ratingCount: Int? = 0
)

// ── ServiceRequestDTO — SerializedNames = chaves JSON reais das Lambdas
//    GET /meus-pedidos  (lista)  →  OrderListResponse.orders
//    GET /detalhes-pedido?id=    →  ServiceRequestDTO direto
//    Payload idêntico ao do app Usuário (regra de negócio compartilhada)
data class ServiceRequestDTO(
    @SerializedName("id")            val id: Long = 0,
    // "servico" — nome da categoria/serviço
    @SerializedName("servico")       val categoryName: String,
    // "cliente" — nome do cliente que solicitou
    @SerializedName("cliente")       val userName: String? = null,
    // "prestador" — nome do prestador atribuído
    @SerializedName("prestador")     val providerName: String? = null,
    // "data" — data/hora do pedido
    @SerializedName("data")          val requestedAt: String,
    // "valor" — valor do serviço
    @SerializedName("valor")         val finalPrice: Double? = null,
    @SerializedName("endereco")      val address: String? = null,
    @SerializedName("descricao")     val description: String? = null,
    @SerializedName("status", alternate = ["status_nome"]) val status: String,
    @SerializedName("status_id")     val statusId: Int? = null,
    // "minha_role" — papel do usuário logado neste pedido (CLIENTE ou PRESTADOR)
    @SerializedName("minha_role")    val myRole: String? = null,
    // "nome_parte" — nome da outra parte envolvida
    @SerializedName("nome_parte")    val otherPartyName: String? = null,
    // Campos extras opcionais retornados em algumas Lambdas de detalhe
    @SerializedName("tipo")          val type: String? = null,
    @SerializedName("data_agendamento") val scheduledAt: String? = null
)

data class OrderListResponse(
    @SerializedName("pedidos") val orders: List<ServiceRequestDTO>
)

// ── Avaliação ─────────────────────────────────────────────────────────────────
data class RatingRequest(
    @SerializedName("serviceRequestId") val serviceRequestId: Long,
    @SerializedName("stars")            val stars: Int,
    @SerializedName("comment")          val comment: String
)

data class RatingResponseDTO(
    @SerializedName("id")               val id: Long,
    @SerializedName("serviceRequestId") val serviceRequestId: Long,
    @SerializedName("clienteName")      val clienteName: String,
    @SerializedName("categoryName")     val categoryName: String,
    @SerializedName("stars")            val stars: Int,
    @SerializedName("comment")          val comment: String?,
    @SerializedName("createdAt")        val createdAt: String?
)

// ── Status online ─────────────────────────────────────────────────────────────
data class SetOnlineRequest(
    @SerializedName("online")    val online: Boolean,
    @SerializedName("latitude")  val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)

// ── Especialidades ────────────────────────────────────────────────────────────
data class ProviderSpecialtyDTO(
    @SerializedName("id")           val id: Long,
    @SerializedName("categoryId")   val categoryId: Long,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("price")        val price: Double
)

// ── Endereços — chaves JSON idênticas ao app Usuário ─────────────────────────
data class AddressDTO(
    @SerializedName("id")          val id: Long = 0,
    @SerializedName("titulo")      val tag: String? = null,
    @SerializedName("cep")         val cep: String? = null,
    @SerializedName("rua")         val street: String? = null,
    @SerializedName("numero")      val number: String? = null,
    @SerializedName("complemento") val complement: String? = null,
    @SerializedName("referencia")  val reference: String? = null,
    @SerializedName("bairro")      val neighborhood: String? = null,
    @SerializedName("cidade")      val city: String? = null,
    @SerializedName("estado")      val state: String? = null,
    @SerializedName("is_favorite") val isDefault: Boolean = false,
    @SerializedName("formatado")   val formatted: String? = null
)

data class AddressListResponse(
    @SerializedName("enderecos") val addresses: List<AddressDTO>
)

// ── Notificações — chaves JSON idênticas ao app Usuário ──────────────────────
data class NotificationDTO(
    @SerializedName("titulo")         val title: String,
    @SerializedName("mensagem")       val message: String,
    @SerializedName("dt_notificacao") val timestamp: String
)

data class NotificationListResponse(
    @SerializedName("notificacoes") val notifications: List<NotificationDTO>
)
