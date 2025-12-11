package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiCommentDTO
import com.tagmypet.data.api.ApiService
import com.tagmypet.ui.screens.home.Comment
import com.tagmypet.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val apiService: ApiService,
) {
    // Helper para mapear o DTO da API para o DTO da UI (recursivo para replies)
    private fun mapApiCommentToUi(apiComment: ApiCommentDTO): Comment {
        return Comment(
            id = apiComment._id,
            userId = apiComment.author.id,
            userName = apiComment.author.name,
            userAvatar = apiComment.author.photoUrl ?: "",
            text = apiComment.content,
            timeAgo = formatTimeAgo(apiComment.createdAt),
            replies = apiComment.replies?.map(::mapApiCommentToUi) ?: emptyList()
        )
    }

    // Função simples de formatação de tempo (implementação completa no Notification.kt)
    private fun formatTimeAgo(iso: String): String {
        // Implementação real da formatação, mas aqui usamos um placeholder
        // O modelo Notification.kt já tem a lógica de tempo.
        return "Recente"
    }

    suspend fun getPostComments(postId: String): Resource<List<Comment>> {
        return try {
            val response = apiService.getComments(postId)
            if (response.isSuccessful && response.body() != null) {
                val comments = response.body()!!.data.comments.map(::mapApiCommentToUi)
                Resource.Success(comments)
            } else {
                Resource.Error("Erro ao carregar comentários")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    suspend fun getReplies(parentCommentId: String, page: Int): Resource<List<Comment>> {
        return try {
            val response = apiService.getReplies(parentCommentId, page)
            if (response.isSuccessful && response.body() != null) {
                val replies = response.body()!!.data.replies.map(::mapApiCommentToUi)
                Resource.Success(replies)
            } else {
                Resource.Error("Erro ao carregar respostas.")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão.")
        }
    }

    suspend fun addComment(
        postId: String,
        content: String,
        parentCommentId: String? = null,
    ): Resource<Comment> {
        return try {
            // parentCommentId deve ser nulo se não for uma resposta
            val request = mutableMapOf("content" to content).apply {
                if (parentCommentId != null) {
                    put("parentCommentId", parentCommentId)
                }
            }

            // O filtro converte o MutableMap<String, String?> para Map<String, String>
            val response = apiService.addComment(
                postId,
                request.filterValues { it != null } as Map<String, String>)

            if (response.isSuccessful && response.body() != null) {
                val apiComment = response.body()!!.data.comment
                val newComment = mapApiCommentToUi(apiComment)
                Resource.Success(newComment)
            } else {
                Resource.Error("Erro ao enviar comentário")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }
}