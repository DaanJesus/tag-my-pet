package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiService
import com.tagmypet.data.model.Notification
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getNotifications(): Resource<List<Notification>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.notifications)
            } else {
                Resource.Error("Erro ao carregar notificações")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return try {
            val response = apiService.markAsRead(notificationId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erro ao marcar como lida")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    suspend fun markAllAsRead(): Resource<Unit> {
        return try {
            val response = apiService.markAllAsRead()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erro ao marcar todas como lidas")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }
}