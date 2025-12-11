package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiService
import com.tagmypet.data.local.TokenManager
import com.tagmypet.data.model.User
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

// Wrapper simples para lidar com Sucesso/Erro na UI
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
) {

    // Login
    suspend fun login(email: String, pass: String): Resource<User> {
        return try {
            val request = mapOf("email" to email, "password" to pass)
            val response = apiService.login(request)

            handleAuthResponse(response)
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }

    // Registro
    suspend fun register(name: String, email: String, pass: String): Resource<User> {
        return try {
            val request = mapOf("name" to name, "email" to email, "password" to pass)
            val response = apiService.register(request)

            handleAuthResponse(response)
        } catch (e: Exception) {
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }

    // Atualizar Token FCM (Firebase)
    suspend fun updateFcmToken(token: String) {
        try {
            apiService.updateFcmToken(mapOf("fcmToken" to token))
        } catch (e: Exception) {
            // Falha silenciosa ou log
            e.printStackTrace()
        }
    }

    // Logout
    suspend fun logout() {
        tokenManager.clearAuthData()
    }

    // Helper para processar a resposta da API (DRY)
    private suspend fun handleAuthResponse(response: retrofit2.Response<com.tagmypet.data.api.AuthResponse>): Resource<User> {
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            // 1. Salvar Token e ID localmente
            tokenManager.saveAuthData(body.token, body.data.user.id)

            // 2. Retornar o Usuário para a UI
            return Resource.Success(body.data.user)
        } else {
            // Tenta ler a mensagem de erro do JSON de erro da API
            val errorMsg = try {
                val errorBody = response.errorBody()?.string()
                JSONObject(errorBody!!).getString("message")
            } catch (e: Exception) {
                "Erro desconhecido (${response.code()})"
            }
            return Resource.Error(errorMsg)
        }
    }

    // Expor o token para quem precisar (ex: checar se está logado na Splash Screen)
    val authToken: Flow<String?> = tokenManager.getToken()
}