package com.example.doesitprovider.data.network

import android.util.Log
import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.*
import com.example.doesitprovider.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CognitoConfig {
    val REGION        = BuildConfig.COGNITO_REGION
    val USER_POOL_ID  = BuildConfig.COGNITO_USER_POOL_ID
    val CLIENT_ID     = BuildConfig.COGNITO_CLIENT_ID
    val CLIENT_SECRET = BuildConfig.COGNITO_CLIENT_SECRET
}

class CognitoService {

    private val client = CognitoIdentityProviderClient { region = CognitoConfig.REGION }

    private fun calculateSecretHash(userName: String): String {
        val hmacSha256 = "HmacSHA256"
        val spec = SecretKeySpec(CognitoConfig.CLIENT_SECRET.toByteArray(), hmacSha256)
        val mac  = Mac.getInstance(hmacSha256).also { it.init(spec); it.update(userName.toByteArray()) }
        return android.util.Base64.encodeToString(mac.doFinal(CognitoConfig.CLIENT_ID.toByteArray()), android.util.Base64.NO_WRAP)
    }

    // Retorna Pair(idToken, accessToken)
    suspend fun signIn(email: String, password: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            Log.d("CognitoService", "Iniciando SignIn para: $email")
            val response = client.initiateAuth(InitiateAuthRequest {
                clientId       = CognitoConfig.CLIENT_ID
                authFlow       = AuthFlowType.UserPasswordAuth
                authParameters = mapOf(
                    "USERNAME"    to email,
                    "PASSWORD"    to password,
                    "SECRET_HASH" to calculateSecretHash(email)
                )
            })
            val idToken     = response.authenticationResult?.idToken
            val accessToken = response.authenticationResult?.accessToken
            if (idToken != null && accessToken != null) {
                Log.d("CognitoService", "✓ SignIn bem-sucedido para $email")
                Result.success(Pair(idToken, accessToken))
            } else {
                Log.e("CognitoService", "✗ Tokens não recebidos do Cognito")
                Result.failure(Exception("Falha na autenticação: tokens não recebidos"))
            }
        } catch (e: NotAuthorizedException) {
            Log.e("CognitoService", "✗ E-mail ou senha incorretos", e)
            Result.failure(Exception("E-mail ou senha incorretos"))
        } catch (e: UserNotFoundException) {
            Log.e("CognitoService", "✗ Usuário não encontrado no Cognito", e)
            Result.failure(Exception("Usuário não encontrado"))
        } catch (e: Exception) {
            Log.e("CognitoService", "✗ Erro genérico no SignIn", e)
            Result.failure(e)
        }
    }

    suspend fun signUp(
        email: String, password: String, name: String, birthdate: String,
        cpf: String, genderId: String, phone: String, cep: String,
        street: String, number: String, neighborhood: String, city: String, state: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("CognitoService", "Iniciando SignUp para: $email")
            client.signUp(SignUpRequest {
                clientId       = CognitoConfig.CLIENT_ID
                secretHash     = calculateSecretHash(email)
                username       = email
                this.password  = password
                userAttributes = listOf(
                    AttributeType { this.name = "name";               this.value = name },
                    AttributeType { this.name = "birthdate";          this.value = birthdate },
                    AttributeType { this.name = "address";            this.value = cep },
                    AttributeType { this.name = "custom:cpf";         this.value = cpf },
                    AttributeType { this.name = "custom:tipo_usuario"; this.value = "PRESTADOR" },
                    AttributeType { this.name = "custom:id_genero";   this.value = genderId },
                    AttributeType { this.name = "custom:telefone";    this.value = phone },
                    AttributeType { this.name = "custom:rua";         this.value = street },
                    AttributeType { this.name = "custom:numero";      this.value = number },
                    AttributeType { this.name = "custom:bairro";      this.value = neighborhood },
                    AttributeType { this.name = "custom:cidade";      this.value = city },
                    AttributeType { this.name = "custom:estado";      this.value = state }
                )
            })
            Log.d("CognitoService", "✓ SignUp bem-sucedido para $email. Conta criada e aguardando confirmação.")
            Result.success(Unit)
        } catch (e: UsernameExistsException) {
            Log.e("CognitoService", "✗ E-mail já cadastrado", e)
            Result.failure(e)
        } catch (e: InvalidPasswordException) {
            Log.e("CognitoService", "✗ Senha não atende aos requisitos", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("CognitoService", "✗ Erro no SignUp", e)
            Result.failure(e)
        }
    }

    // Invalida todos os tokens do usuário no Cognito (best-effort)
    suspend fun signOut(accessToken: String) = withContext(Dispatchers.IO) {
        try {
            client.globalSignOut(GlobalSignOutRequest { this.accessToken = accessToken })
        } catch (_: Exception) {
            // Falha silenciosa — logout local prossegue de qualquer forma
        }
    }
}
