package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService,
) {
    // Envia uma denúncia para a API
    // targetId: ID do post, comentário ou usuário
    // targetModel: "Post", "Comment", "User"
    // reason: Motivo da denúncia
    suspend fun createReport(
        targetId: String,
        targetModel: String,
        reason: String,
        description: String = "",
    ): Resource<Unit> {
        return try {
            val request = mapOf(
                "targetId" to targetId,
                "targetModel" to targetModel,
                "reason" to reason,
                "description" to description
            )

            val response = apiService.createReport(request)

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Erro ao enviar denúncia.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }
}