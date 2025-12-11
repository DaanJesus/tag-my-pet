package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiService
import com.tagmypet.data.api.ConversationDTO
import com.tagmypet.data.api.MessageDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getConversations(): Resource<List<ConversationDTO>> {
        return try {
            val response = apiService.getConversations()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.conversations)
            } else {
                Resource.Error("Erro ao carregar conversas")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro desconhecido")
        }
    }

    suspend fun getMessages(otherUserId: String): Resource<List<MessageDTO>> {
        return try {
            val response = apiService.getMessages(otherUserId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.messages)
            } else {
                Resource.Error("Erro ao carregar mensagens")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro desconhecido")
        }
    }

    suspend fun sendMessage(receiverId: String, text: String): Resource<MessageDTO> {
        return try {
            val request = mapOf("receiverId" to receiverId, "text" to text)
            val response = apiService.sendMessage(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.data.message)
            } else {
                Resource.Error("Erro ao enviar")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro desconhecido")
        }
    }
}