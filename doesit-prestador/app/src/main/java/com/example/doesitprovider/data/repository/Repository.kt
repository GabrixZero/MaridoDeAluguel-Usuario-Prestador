package com.example.doesitprovider.data.repository

import android.util.Log
import com.example.doesitprovider.data.model.*
import com.example.doesitprovider.data.network.CognitoService
import com.example.doesitprovider.data.network.RetrofitClient
import com.example.doesitprovider.data.network.SessionManager
import aws.sdk.kotlin.services.cognitoidentityprovider.model.*

// ── UserRepository ─────────────────────────────────────────────────────────────

class UserRepository {
    private val api = RetrofitClient.apiService
    private val cognitoService = CognitoService()

    private fun saveSession(body: AuthResponse) {
        val safeToken = body.token?.takeIf { it.isNotBlank() } ?: SessionManager.token
        SessionManager.save(
            token       = safeToken,
            id          = body.id,
            name        = body.name ?: "Prestador",
            email       = body.email ?: "",
            phone       = body.phone ?: "",
            cpf         = body.cpf ?: "",
            birthDate   = body.birthDate ?: "",
            gender      = body.gender?.toString() ?: "",
            rating      = body.rating ?: 0.0,
            ratingCount = body.ratingCount ?: 0
        )
    }

    suspend fun login(email: String, password: String): Result<String> {
        Log.d("UserRepo", "Iniciando login para: $email")
        val cognitoResult = cognitoService.signIn(email, password)
        return cognitoResult.fold(
            onSuccess = { (idToken, accessToken) ->
                Log.d("UserRepo", "✓ Cognito SignIn bem-sucedido. Obtendo dados do usuário...")
                SessionManager.token       = idToken
                SessionManager.accessToken = accessToken
                getCurrentUser()
            },
            onFailure = { e ->
                Log.e("UserRepo", "✗ Erro no Cognito SignIn", e)
                val message = when (e) {
                    is NotAuthorizedException        -> "E-mail ou senha incorretos."
                    is UserNotFoundException          -> "Usuário não encontrado."
                    is UserNotConfirmedException      -> "Conta não confirmada. Verifique seu e-mail."
                    is PasswordResetRequiredException -> "Redefinição de senha necessária."
                    else                              -> e.message ?: "Erro ao entrar"
                }
                Result.failure(Exception(message))
            }
        )
    }

    suspend fun getCurrentUser(): Result<String> = try {
        Log.d("UserRepo", "Chamando GET /get-current-user...")
        val r = api.getCurrentUser()
        Log.d("UserRepo", "Resposta recebida: código ${r.code()}, sucesso=${r.isSuccessful}")
        if (r.isSuccessful && r.body() != null) {
            Log.d("UserRepo", "✓ Usuário obtido com sucesso: ${r.body()?.name}")
            saveSession(r.body()!!)
            Result.success(r.body()!!.name ?: "Prestador")
        } else {
            val errorBody = r.errorBody()?.string() ?: "sem detalhes"
            Log.e("UserRepo", "✗ getCurrentUser falhou: HTTP ${r.code()} - $errorBody")
            Result.failure(Exception("Erro ao carregar dados do usuário (HTTP ${r.code()}). Verifique sua conexão."))
        }
    } catch (e: Exception) {
        Log.e("UserRepo", "✗ getCurrentUser exception", e)
        Result.failure(Exception("Sem conexão com o servidor. Verifique sua internet."))
    }

    suspend fun refreshProfile(): Result<Unit> = try {
        val r = api.getCurrentUser()
        if (r.isSuccessful && r.body() != null) { saveSession(r.body()!!); Result.success(Unit) }
        else Result.failure(Exception("Erro ao atualizar perfil"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun register(dto: RegisterRequest): Result<String> {
        val parts = dto.birthDate.split("/")
        val formattedDate = if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dto.birthDate
        val genderId = when (dto.gender) { "Masculino" -> "1"; "Feminino" -> "2"; "Outros" -> "3"; else -> "4" }

        return cognitoService.signUp(
            email = dto.email, password = dto.password, name = dto.name,
            birthdate = formattedDate, cpf = dto.cpf, genderId = genderId,
            phone = dto.phone, cep = dto.addressCep, street = dto.addressStreet,
            number = dto.addressNumber, neighborhood = dto.addressNeighborhood,
            city = dto.addressCity, state = dto.addressState
        ).fold(
            onSuccess = { Result.success("Cadastro realizado com sucesso! Verifique seu e-mail.") },
             onFailure = { e ->
                 Log.e("UserRepo", "register error", e)
                 val msg = when (e) {
                     is UsernameExistsException      -> "Este e-mail já está cadastrado."
                     is InvalidPasswordException     -> "A senha não atende aos requisitos de segurança."
                     is InvalidParameterException    -> e.message ?: "Dados inválidos."
                     is CodeDeliveryFailureException -> "Erro ao enviar código de verificação."
                     else -> {
                         val errorMsg = e.message ?: "Erro ao criar conta"
                         when {
                             errorMsg.contains("duplicate key", ignoreCase = true) -> 
                                 "Este e-mail já está cadastrado no sistema."
                             errorMsg.contains("PostConfirmation", ignoreCase = true) -> 
                                 "Erro ao confirmar a conta. Por favor, tente novamente."
                             errorMsg.contains("constraint", ignoreCase = true) -> 
                                 "Não foi possível criar a conta. Tente com dados diferentes."
                             else -> errorMsg
                         }
                     }
                 }
                 Result.failure(Exception(msg))
             }
        )
    }

    suspend fun updateProfile(body: Map<String, String>): Result<Unit> = try {
        val r = api.updateProfile(SessionManager.bearerToken(), body)
        if (r.isSuccessful && r.body() != null) {
            r.body()!!.let { b ->
                SessionManager.userName  = b.name ?: SessionManager.userName
                SessionManager.userPhone = b.phone ?: SessionManager.userPhone
            }
            Result.success(Unit)
        } else Result.failure(Exception("Erro ao salvar perfil"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun changePassword(current: String, newPass: String): Result<String> = try {
        val r = api.changePassword(SessionManager.bearerToken(),
            mapOf("currentPassword" to current, "newPassword" to newPass))
        if (r.isSuccessful) Result.success("Senha alterada com sucesso")
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(when {
                err.contains("atual incorreta") -> "Senha atual incorreta"
                err.contains("maiúscula") || err.contains("símbolo") -> err
                else -> "Erro ao alterar senha"
            }))
        }
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun deleteAccount(): Result<Unit> = try {
        val r = api.deleteAccount(SessionManager.bearerToken())
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ao excluir conta"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun logout() {
        val accessToken = SessionManager.accessToken
        if (accessToken.isNotBlank()) {
            cognitoService.signOut(accessToken)
        }
        SessionManager.clear()
    }
}

// ── ServiceRepository ─────────────────────────────────────────────────────────

class ServiceRepository {
    private val api = RetrofitClient.apiService

    // ── Online/Offline ────────────────────────────────────────────────────────
    suspend fun setOnline(online: Boolean): Result<Unit> = try {
        val r = api.setStatus(SessionManager.bearerToken(), SetOnlineRequest(online))
        if (r.isSuccessful) { SessionManager.isOnline = online; Result.success(Unit) }
        else Result.failure(Exception("Erro ao atualizar status (HTTP ${r.code()})"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão: ${e.message}")) }

    // ── Pedidos/Serviços — Lambda GET /meus-pedidos ───────────────────────────
    suspend fun getHistory(status: String? = null): Result<List<ServiceRequestDTO>> = try {
        val r = api.getMyHistory(status)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.orders)
        else Result.failure(Exception("Erro ao carregar histórico"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    // ── Detalhe — Lambda GET /detalhes-pedido?id= ────────────────────────────
    suspend fun getById(id: Long): Result<ServiceRequestDTO> = try {
        val r = api.getRequestById(id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Pedido não encontrado"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    // ── Ações sobre pedidos (Agora usando o endpoint genérico /atualizar-pedido) ──
    suspend fun updateStatus(id: Long, newStatusId: Int): Result<UpdateOrderStatusResponse> = try {
        val r = api.updateOrderStatus(SessionManager.bearerToken(), UpdateOrderStatusRequest(id, newStatusId))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(err.ifBlank { "Erro ao atualizar pedido" }))
        }
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    // ── Avaliação ─────────────────────────────────────────────────────────────
    suspend fun rateUser(requestId: Long, stars: Int, comment: String): Result<Unit> = try {
        val r = api.rateUser(SessionManager.bearerToken(), RatingRequest(requestId, stars, comment))
        if (r.isSuccessful) Result.success(Unit)
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(if (err.contains("já avaliou")) "Você já avaliou este cliente" else "Erro ao enviar avaliação"))
        }
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    // ── Especialidades ────────────────────────────────────────────────────────
    suspend fun getSpecialtiesFlow(): Result<SpecialtyResponse> = try {
        val r = api.getSpecialtiesFlow(SessionManager.bearerToken())
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao carregar especialidades"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun saveSpecialtiesFlow(request: SaveSpecialtiesRequest): Result<Unit> = try {
        val r = api.saveSpecialtiesFlow(SessionManager.bearerToken(), request)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ao salvar especialidades"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun getMySpecialties(): Result<List<ProviderSpecialtyDTO>> = try {
        val r = api.getMySpecialties(SessionManager.bearerToken())
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao carregar especialidades"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun upsertSpecialty(categoryId: Long, price: Double): Result<ProviderSpecialtyDTO> = try {
        val r = api.upsertSpecialty(SessionManager.bearerToken(), mapOf("categoryId" to categoryId, "price" to price))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao salvar especialidade"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    suspend fun deleteSpecialty(categoryId: Long): Result<Unit> = try {
        val r = api.deleteSpecialty(SessionManager.bearerToken(), categoryId)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ao remover especialidade"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    // ── Endereços — Lambda GET /meus-enderecos ────────────────────────────────
    suspend fun getAddresses(): Result<List<AddressDTO>> = try {
        val r = api.getAddresses()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.addresses)
        else Result.failure(Exception("Erro ao buscar endereços"))
    } catch (e: Exception) {
        Log.e("ServiceRepo", "getAddresses error", e)
        Result.failure(Exception("Sem conexão"))
    }

    // ── Endereços — Lambda POST /cadastrar-endereco ───────────────────────────
    suspend fun createAddress(body: Map<String, String>): Result<AddressDTO> = try {
        val r = api.createAddress(body)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao criar endereço"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    // ── Endereços — Lambda PUT /atualizar-endereco?id= ────────────────────────
    suspend fun updateAddress(id: Long, body: Map<String, String>): Result<AddressDTO> = try {
        val r = api.updateAddress(id, body)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao atualizar endereço"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão")) }

    // ── Notificações — Lambda GET /minhas-notificacoes ────────────────────────
    suspend fun getNotifications(): Result<List<NotificationDTO>> = try {
        val r = api.getNotifications()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.notifications)
        else Result.failure(Exception("Erro ao buscar notificações"))
    } catch (e: Exception) {
        Log.e("ServiceRepo", "getNotifications error", e)
        Result.failure(Exception("Sem conexão"))
    }
}
