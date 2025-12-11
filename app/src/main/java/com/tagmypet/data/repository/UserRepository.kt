package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiService
import com.tagmypet.data.api.PublicProfileData
import com.tagmypet.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getMe(): Resource<User> {
        return try {
            val response = apiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.user)
            } else {
                Resource.Error("Erro ao carregar perfil: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    // NOVO: Atualizar Perfil
    suspend fun updateProfile(name: String, photoUrl: String?): Resource<User> {
        return try {
            val request = mutableMapOf("name" to name)
            if (photoUrl != null) request["photoUrl"] = photoUrl

            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.user)
            } else {
                Resource.Error("Erro ao atualizar perfil")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    suspend fun getPublicProfile(userId: String): Resource<PublicProfileData> {
        return try {
            val response = apiService.getPublicProfile(userId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data)
            } else {
                Resource.Error("Erro ao carregar perfil público")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    suspend fun toggleFollow(userId: String): Resource<Boolean> {
        return try {
            val response = apiService.toggleFollow(userId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.isFollowing)
            } else {
                Resource.Error("Erro ao realizar ação")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    // --- CORREÇÃO AQUI: Função que faltava ---
    suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val response = apiService.deleteAccount()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erro ao excluir conta")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }
}