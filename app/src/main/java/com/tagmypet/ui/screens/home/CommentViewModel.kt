package com.tagmypet.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.remote.SocketManager
import com.tagmypet.data.repository.CommentRepository
import com.tagmypet.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

// DTO CANÔNICO DA UI - Única declaração no Kotlin
data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timeAgo: String,
    val replies: List<Comment> = emptyList(),
    val parentCommentId: String? = null,
)

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val repository: CommentRepository,
    private val socketManager: SocketManager,
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPostId: String = ""

    init {
        socketManager.connect()
        observeSocketComments()
    }

    fun loadComments(postId: String) {
        currentPostId = postId
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getPostComments(postId)) {
                is Resource.Success -> _comments.value = result.data ?: emptyList()
                is Resource.Error -> { /* Tratar erro */
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }

    /**
     * FUNÇÃO RECURSIVA PARA BUSCAR E ADICIONAR RESPOSTAS em tempo real.
     * Propaga a atualização de forma imutável para que o Compose detecte a mudança.
     */
    private fun findAndAddNewReply(
        comments: List<Comment>,
        parentId: String,
        newReply: Comment,
    ): List<Comment> {
        return comments.map { comment ->
            if (comment.id == parentId) {
                // 1. Encontrou o pai: Retorna uma cópia do pai com a nova resposta
                comment.copy(replies = comment.replies + newReply)
            } else if (comment.replies.isNotEmpty()) {
                // 2. Busca recursivamente nas respostas
                val updatedReplies = findAndAddNewReply(comment.replies, parentId, newReply)

                // Propaga a cópia apenas se a lista interna realmente mudou (essencial para Compose)
                if (updatedReplies !== comment.replies) {
                    comment.copy(replies = updatedReplies)
                } else {
                    comment
                }
            } else {
                comment
            }
        }
    }


    /**
     * Escuta o evento 'new_comment' do servidor via Socket.IO para atualização em tempo real.
     * (Principalmente para outros usuários, mas serve como fallback)
     */
    private fun observeSocketComments() {
        viewModelScope.launch {
            // Usamos messageFlow (SocketManager.kt) como fluxo de dados
            socketManager.messageFlow.collect { json ->
                val receivedPostId = json.optString("post")

                if (receivedPostId == currentPostId) {
                    val newComment = mapSocketJsonToComment(json)
                    val parentId = newComment.parentCommentId

                    if (parentId.isNullOrBlank()) {
                        // Novo comentário raiz: Adiciona no topo da lista
                        _comments.update { listOf(newComment) + it }
                    } else {
                        // Resposta: Usa a função recursiva
                        _comments.update { currentList ->
                            findAndAddNewReply(currentList, parentId, newComment)
                        }
                    }
                }
            }
        }
    }

    private fun mapSocketJsonToComment(json: JSONObject): Comment {
        val authorJson = json.optJSONObject("author") ?: JSONObject()
        return Comment(
            id = json.optString("_id", ""),
            userId = authorJson.optString("_id", ""),
            userName = authorJson.optString("name", "Usuário"),
            userAvatar = authorJson.optString("photoUrl", ""),
            text = json.optString("content", ""),
            timeAgo = "Agora",
            replies = emptyList(),
            parentCommentId = json.optString("parentComment", null)
        )
    }

    /**
     * Função para enviar comentário/resposta com atualização OTIMISTA.
     */
    fun postComment(content: String, parentCommentId: String? = null) {
        if (content.isBlank() || currentPostId.isBlank()) return

        viewModelScope.launch {
            // Sem spinner para o usuário
            when (val result = repository.addComment(currentPostId, content, parentCommentId)) {
                is Resource.Success -> {
                    // ATUALIZAÇÃO OTIMISTA IMEDIATA (para quem postou):
                    result.data?.let { newComment ->
                        // Garante que o parentId está no DTO para a inserção otimista
                        val commentToInsert =
                            if (parentCommentId != null && newComment.parentCommentId.isNullOrBlank()) {
                                newComment.copy(parentCommentId = parentCommentId)
                            } else {
                                newComment
                            }

                        if (commentToInsert.parentCommentId.isNullOrBlank()) {
                            _comments.update { listOf(commentToInsert) + it }
                        } else {
                            _comments.update { currentList ->
                                findAndAddNewReply(
                                    currentList,
                                    commentToInsert.parentCommentId!!,
                                    commentToInsert
                                )
                            }
                        }
                    }
                }

                is Resource.Error -> { /* Tratar erro */
                }

                else -> {}
            }
        }
    }
}