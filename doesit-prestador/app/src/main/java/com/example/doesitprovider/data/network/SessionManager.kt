package com.example.doesitprovider.data.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SessionManager {
    var token: String = ""
    var userId: Long = 0
    var userName: String = ""
    var userEmail: String = ""
    var userPhone: String = ""
    var userCpf: String = ""
    var userBirthDate: String = ""
    var userGender: String = ""
    // rating e ratingCount são reativos para que o header da Home atualize automaticamente
    var rating by mutableStateOf(0.0)
    var ratingCount by mutableStateOf(0)
    var isOnline by mutableStateOf(false)

    fun isLoggedIn() = token.isNotEmpty()
    fun bearerToken() = "Bearer $token"

    fun save(token: String, id: Long, name: String, email: String,
             phone: String = "", cpf: String = "", birthDate: String = "",
             gender: String = "", rating: Double = 0.0, ratingCount: Int = 0) {
        this.token = token; this.userId = id; this.userName = name; this.userEmail = email
        this.userPhone = phone; this.userCpf = cpf; this.userBirthDate = birthDate
        this.userGender = gender; this.rating = rating; this.ratingCount = ratingCount
    }

    fun clear() {
        token = ""; userId = 0; userName = ""; userEmail = ""; userPhone = ""
        userCpf = ""; userBirthDate = ""; userGender = ""
        rating = 0.0; ratingCount = 0; isOnline = false
    }
}
