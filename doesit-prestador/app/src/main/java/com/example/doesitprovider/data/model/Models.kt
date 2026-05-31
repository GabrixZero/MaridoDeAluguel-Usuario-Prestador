package com.example.doesitprovider.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

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

data class AuthResponse(
    @SerializedName("token")       val token: String,
    @SerializedName("id")          val id: Long,
    @SerializedName("name")        val name: String,
    @SerializedName("email")       val email: String,
    @SerializedName("role")        val role: String,
    @SerializedName("phone")       val phone: String?,
    @SerializedName("cpf")         val cpf: String?,
    @SerializedName("birthDate")   val birthDate: String?,
    @SerializedName("gender")      val gender: String?,
    @SerializedName("rating")      val rating: Double?,
    @SerializedName("ratingCount") val ratingCount: Int?
)

// ServiceRequestDTO alinhado com backend — inclui startedAt e address
data class ServiceRequestDTO(
    @SerializedName("id")              val id: Long,
    @SerializedName("userName")        val userName: String,
    @SerializedName("userRating")      val userRating: Double?,
    @SerializedName("userRatingCount") val userRatingCount: Int?,
    @SerializedName("providerName")    val providerName: String?,
    @SerializedName("providerRating")  val providerRating: Double?,
    @SerializedName("categoryName")    val categoryName: String,
    @SerializedName("description")     val description: String,
    @SerializedName("status")          val status: String,
    @SerializedName("type")            val type: String,
    @SerializedName("scheduledAt")     val scheduledAt: String?,
    @SerializedName("requestedAt")     val requestedAt: String,
    @SerializedName("startedAt")       val startedAt: String?,
    @SerializedName("userLatitude")    val userLatitude: Double?,
    @SerializedName("userLongitude")   val userLongitude: Double?,
    @SerializedName("finalPrice")      val finalPrice: Double?,
    @SerializedName("address")         val address: String?,
    @SerializedName("preferredProviderId") val preferredProviderId: Long?
)

// Avaliação de usuário pelo prestador (POST /api/ratings/user)
data class RatingRequest(
    @SerializedName("serviceRequestId") val serviceRequestId: Long,
    @SerializedName("stars")            val stars: Int,
    @SerializedName("comment")          val comment: String
)

// DTO tipado para status online — evita NPE por type-erasure
data class SetOnlineRequest(
    @SerializedName("online")    val online: Boolean,
    @SerializedName("latitude")  val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
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

// Especialidade de preço do prestador
data class ProviderSpecialtyDTO(
    @SerializedName("id")           val id: Long,
    @SerializedName("categoryId")   val categoryId: Long,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("price")        val price: Double
)
