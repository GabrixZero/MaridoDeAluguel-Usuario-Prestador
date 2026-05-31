package com.example.doesitprovider.data.repository

import com.example.doesitprovider.data.model.RatingResponseDTO
import com.example.doesitprovider.data.network.RetrofitClient
import com.example.doesitprovider.data.network.SessionManager

class RatingRepository {
    private val api = RetrofitClient.apiService

    suspend fun getMyReceivedRatings(): Result<List<RatingResponseDTO>> = try {
        val r = api.getMyReceivedRatings(SessionManager.bearerToken())
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao carregar avaliações"))
    } catch (e: Exception) {
        Result.failure(Exception("Sem conexão"))
    }
}