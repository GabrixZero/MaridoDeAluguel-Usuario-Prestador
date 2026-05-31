package com.example.doesitprovider.data.repository

import android.util.Log
import com.example.doesitprovider.data.model.*
import com.example.doesitprovider.data.network.RetrofitClient
import com.example.doesitprovider.data.network.SessionManager

class UserRepository {
    private val api = RetrofitClient.apiService

    suspend fun login(email: String, password: String): Result<String> = try {
        val r = api.login(LoginRequest(email, password))
        if (r.isSuccessful && r.body() != null) {
            val b = r.body()!!
            SessionManager.save(b.token, b.id, b.name, b.email, b.phone ?: "", b.cpf ?: "",
                b.birthDate ?: "", b.gender ?: "", b.rating ?: 0.0, b.ratingCount ?: 0)
            Result.success(b.name)
        } else Result.failure(Exception("Email ou senha inválidos"))
    } catch (e: Exception) {
        Log.e("UserRepository", "Login error", e)
        Result.failure(Exception("Sem conexão com o servidor"))
    }

    suspend fun register(dto: RegisterRequest): Result<String> = try {
        val r = api.register(dto)
        if (r.isSuccessful && r.body() != null) {
            val b = r.body()!!
            SessionManager.save(b.token, b.id, b.name, b.email, b.phone ?: "", b.cpf ?: "",
                b.birthDate ?: "", b.gender ?: "", b.rating ?: 0.0, b.ratingCount ?: 0)
            Result.success(b.name)
        } else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(when {
                err.contains("E-mail já existe") -> "E-mail já cadastrado."
                err.contains("CPF já cadastrado") -> "CPF já cadastrado."
                err.contains("senha", ignoreCase = true) -> "A senha não atende os requisitos."
                else -> "Não foi possível criar a conta."
            }))
        }
    } catch (e: Exception) {
        Log.e("UserRepository", "Register error", e)
        Result.failure(Exception("Sem conexão com o servidor"))
    }

    suspend fun refreshProfile(): Result<Unit> = try {
        val r = api.getMe(SessionManager.bearerToken())
        if (r.isSuccessful && r.body() != null) {
            val b = r.body()!!
            SessionManager.rating      = b.rating ?: 0.0
            SessionManager.ratingCount = b.ratingCount ?: 0
            SessionManager.userName    = b.name
            SessionManager.userPhone   = b.phone ?: ""
            Result.success(Unit)
        } else Result.failure(Exception("Erro ao atualizar perfil"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun updateProfile(body: Map<String, String>): Result<Unit> = try {
        val r = api.updateProfile(SessionManager.bearerToken(), body)
        if (r.isSuccessful && r.body() != null) {
            val b = r.body()!!
            SessionManager.userName  = b.name
            SessionManager.userPhone = b.phone ?: ""
            Result.success(Unit)
        } else Result.failure(Exception("Erro ao salvar perfil"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun changePassword(current: String, newPass: String): Result<String> = try {
        val r = api.changePassword(
            SessionManager.bearerToken(),
            mapOf("currentPassword" to current, "newPassword" to newPass)
        )
        if (r.isSuccessful) Result.success("Senha alterada com sucesso")
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(when {
                err.contains("atual incorreta") -> "Senha atual incorreta"
                err.contains("maiúscula") || err.contains("minúscula") || err.contains("símbolo") -> err
                else -> "Erro ao alterar senha"
            }))
        }
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun deleteAccount(): Result<Unit> = try {
        val r = api.deleteAccount(SessionManager.bearerToken())
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ao excluir conta"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }
}

class ServiceRepository {
    private val api = RetrofitClient.apiService

    suspend fun setOnline(online: Boolean): Result<Unit> = try {
        val r = api.setStatus(SessionManager.bearerToken(), SetOnlineRequest(online))
        if (r.isSuccessful) { SessionManager.isOnline = online; Result.success(Unit) }
        else Result.failure(Exception("Erro ao atualizar status (HTTP ${r.code()})"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão: ${e.message}")) }

    suspend fun getHistory() = try {
        val r = api.getMyHistory(SessionManager.bearerToken())
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao carregar histórico"))
    } catch (e: Exception) { Result.failure<List<ServiceRequestDTO>>(Exception("Sem conexão")) }

    suspend fun getById(id: Long) = try {
        val r = api.getRequestById(SessionManager.bearerToken(), id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Pedido não encontrado"))
    } catch (e: Exception) { Result.failure<ServiceRequestDTO>(Exception("Sem conexão")) }

    suspend fun accept(id: Long) = try {
        val r = api.acceptRequest(SessionManager.bearerToken(), id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(if (err.isNotBlank()) err else "Pedido não disponível"))
        }
    } catch (e: Exception) { Result.failure<ServiceRequestDTO>(Exception("Sem conexão")) }

    suspend fun refuse(id: Long) = try {
        val r = api.refuseRequest(SessionManager.bearerToken(), id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao recusar"))
    } catch (e: Exception) { Result.failure<ServiceRequestDTO>(Exception("Sem conexão")) }

    suspend fun start(id: Long) = try {
        val r = api.startRequest(SessionManager.bearerToken(), id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(if (err.isNotBlank()) err else "Erro ao iniciar serviço"))
        }
    } catch (e: Exception) { Result.failure<ServiceRequestDTO>(Exception("Sem conexão")) }

    suspend fun cancel(id: Long) = try {
        val r = api.cancelRequest(SessionManager.bearerToken(), id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao cancelar"))
    } catch (e: Exception) { Result.failure<ServiceRequestDTO>(Exception("Sem conexão")) }

    suspend fun rateUser(requestId: Long, stars: Int, comment: String) = try {
        val r = api.rateUser(SessionManager.bearerToken(), RatingRequest(requestId, stars, comment))
        if (r.isSuccessful) Result.success(Unit)
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(when {
                err.contains("já avaliou") -> "Você já avaliou este cliente"
                else -> "Erro ao enviar avaliação"
            }))
        }
    } catch (e: Exception) { Result.failure<Unit>(Exception("Sem conexão")) }

    suspend fun getMySpecialties() = try {
        val r = api.getMySpecialties(SessionManager.bearerToken())
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao carregar especialidades"))
    } catch (e: Exception) { Result.failure<List<ProviderSpecialtyDTO>>(Exception("Sem conexão")) }

    suspend fun upsertSpecialty(categoryId: Long, price: Double) = try {
        val r = api.upsertSpecialty(
            SessionManager.bearerToken(),
            mapOf("categoryId" to categoryId, "price" to price)
        )
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao salvar especialidade"))
    } catch (e: Exception) { Result.failure<ProviderSpecialtyDTO>(Exception("Sem conexão")) }

    suspend fun deleteSpecialty(categoryId: Long) = try {
        val r = api.deleteSpecialty(SessionManager.bearerToken(), categoryId)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ao remover especialidade"))
    } catch (e: Exception) { Result.failure<Unit>(Exception("Sem conexão")) }
}
