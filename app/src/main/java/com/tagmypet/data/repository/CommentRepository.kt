package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiCommentDTO
import com.tagmypet.data.api.ApiService
import com.tagmypet.data.model.Comment
import javax.inject.Inject
import javax.inject.Singleton

// Novo DTO para o resultado do getPostComments, incluindo a contagem total e página atual.
data class PaginatedComments(
    val comments: List<Comment>,
    val totalComments: Int,
    val currentPage: Int,
    val limit: Int,
)

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
            replies = apiComment.replies?.map(::mapApiCommentToUi) ?: emptyList(),
            parentCommentId = apiComment.parentComment,
            // Usa o repliesCount do API DTO. Se for uma resposta (reply), ele será nulo ou 0.
            totalReplies = apiComment.repliesCount ?: apiComment.replies?.size ?: 0
        )
    }

    // Função simples de formatação de tempo (implementação completa no Notification.kt)
    private fun formatTimeAgo(iso: String): String {
        // Implementação real da formatação, mas aqui usamos um placeholder
        // O modelo Notification.kt já tem a lógica de tempo.
        return "Recente"
    }

    // Paginação para comentários raiz
    suspend fun getPostComments(
        postId: String,
        page: Int,
        limit: Int = 10,
    ): Resource<PaginatedComments> {
        return try {
            val response = apiService.getComments(postId, page, limit)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val comments = body.data.comments.map(::mapApiCommentToUi)
                Resource.Success(
                    PaginatedComments(
                        comments = comments,
                        totalComments = body.total,
                        currentPage = body.page,
                        limit = limit
                    )
                )
            } else {
                Resource.Error("Erro ao carregar comentários")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }

    // Paginação para respostas
    suspend fun getReplies(
        parentCommentId: String,
        page: Int,
        limit: Int = 5,
    ): Resource<PaginatedComments> {
        return try {
            // A API já usa limit=5 por padrão, mas passamos a página para controle
            val response = apiService.getReplies(parentCommentId, page, limit)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Mapeamos as respostas. O total no body é o total de replies disponíveis no banco.
                val replies = body.data.replies.map(::mapApiCommentToUi)
                Resource.Success(
                    PaginatedComments(
                        comments = replies,
                        totalComments = body.total,
                        currentPage = body.page,
                        limit = limit
                    )
                )
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