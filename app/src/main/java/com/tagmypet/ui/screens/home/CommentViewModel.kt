package com.tagmypet.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagmypet.data.model.Comment
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

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val repository: CommentRepository,
    private val socketManager: SocketManager,
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false) // Loading inicial
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // NOVO: Estados de Paginação para Comentários Raiz
    private var currentPage = 1
    private var isLastPage = false
    private val rootCommentLimit = 10

    private val _isLoadMoreLoading = MutableStateFlow(false) // Loading do scroll infinito
    val isLoadMoreLoading: StateFlow<Boolean> = _isLoadMoreLoading.asStateFlow()

    private var currentPostId: String = ""

    init {
        socketManager.connect()
        observeSocketComments()
    }

    fun loadComments(postId: String) {
        currentPostId = postId
        currentPage = 1
        isLastPage = false
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getPostComments(postId, currentPage, rootCommentLimit)) {
                is Resource.Success -> {
                    val paginatedData = result.data
                    _comments.value = paginatedData?.comments ?: emptyList()
                    // Define se a primeira página já é a última
                    isLastPage =
                        paginatedData?.totalComments ?: 0 <= paginatedData?.comments?.size ?: 0
                }

                is Resource.Error -> { /* Tratar erro */
                }

                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadMoreComments() {
        if (isLastPage || _isLoading.value || _isLoadMoreLoading.value) return

        currentPage++
        viewModelScope.launch {
            _isLoadMoreLoading.value = true
            when (val result =
                repository.getPostComments(currentPostId, currentPage, rootCommentLimit)) {
                is Resource.Success -> {
                    val paginatedData = result.data
                    val newComments = paginatedData?.comments ?: emptyList()
                    _comments.update { it + newComments }
                    // Se o número de resultados for menor que o limite, é a última página.
                    isLastPage = newComments.size < rootCommentLimit
                }

                is Resource.Error -> {
                    // Reverte a página em caso de falha
                    currentPage--
                    /* Tratar erro */
                }

                else -> {}
            }
            _isLoadMoreLoading.value = false
        }
    }

    /**
     * PROBLEMA 3 CORRIGIDO: Mescla as novas respostas no array existente em vez de substituí-lo.
     */
    fun loadMoreReplies(parentCommentId: String) {
        val targetComment = _comments.value.find { it.id == parentCommentId }
        val nextPage = targetComment?.nextReplyPage ?: return

        viewModelScope.launch {
            // Não usamos o isLoadMoreLoading global aqui para não bloquear o scroll de root comments
            when (val result = repository.getReplies(parentCommentId, nextPage)) {
                is Resource.Success -> {
                    val paginatedData = result.data
                    val newReplies = paginatedData?.comments ?: emptyList()
                    val newNextPage = nextPage + 1

                    _comments.update { currentList ->
                        updateRepliesAndPagination(
                            currentList,
                            parentCommentId,
                            newReplies,
                            newNextPage,
                            paginatedData?.totalComments ?: 0 // Passa o total real
                        )
                    }
                }

                is Resource.Error -> { /* Tratar erro */
                }

                else -> {}
            }
        }
    }

    // Função auxiliar para injetar as novas respostas na árvore (Imutável)
    private fun updateRepliesAndPagination(
        comments: List<Comment>,
        parentId: String,
        newReplies: List<Comment>,
        newNextPage: Int,
        totalReplies: Int, // <--- Total real vindo da API
    ): List<Comment> {
        return comments.map { comment ->
            if (comment.id == parentId) {
                // Junta as respostas existentes com as novas
                val updatedReplies = comment.replies + newReplies
                // Atualiza o contador da próxima página e o total de replies no objeto.
                comment.copy(
                    replies = updatedReplies,
                    nextReplyPage = newNextPage,
                    totalReplies = totalReplies // <--- Atualiza com o total real
                )
            } else {
                comment
            }
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
                // Otimista: Incrementa o totalReplies no estado local.
                comment.copy(
                    replies = comment.replies + newReply,
                    totalReplies = comment.totalReplies + 1
                )
            } else if (comment.replies.isNotEmpty()) {
                // 2. Busca recursivamente nas respostas
                val updatedReplies = findAndAddNewReply(comment.replies, parentId, newReply)

                // Propaga a cópia apenas se a lista interna realmente mudou (essencial para Compose)
                if (updatedReplies !== comment.replies) {
                    // Se o reply é de um reply, ele deve atualizar o repliesCount do root
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
            timeAgo = "Agora", // Não temos a data completa aqui, usamos placeholder
            replies = emptyList(),
            parentCommentId = json.optString("parentComment", null)
        )
    }

    /**
     * Função para enviar comentário/resposta com atualização OTIMISTA.
     * Inclui a lógica para persistir o parentCommentId na resposta otimista.
     */
    fun postComment(content: String, parentCommentId: String? = null) {
        if (content.isBlank() || currentPostId.isBlank()) return

        viewModelScope.launch {
            // Sem spinner para o usuário
            when (val result = repository.addComment(currentPostId, content, parentCommentId)) {
                is Resource.Success -> {
                    // ATUALIZAÇÃO OTIMISTA IMEDIATA:
                    result.data?.let { newComment ->
                        // Garante que o parentId está no DTO de resposta da API para a inserção otimista
                        val commentToInsert =
                            if (parentCommentId != null && newComment.parentCommentId.isNullOrBlank()) {
                                newComment.copy(parentCommentId = parentCommentId)
                            } else {
                                newComment
                            }

                        if (commentToInsert.parentCommentId.isNullOrBlank()) {
                            // Comentário Raiz
                            _comments.update { listOf(commentToInsert) + it }
                        } else {
                            // Resposta (Usa a lógica recursiva que incrementa o contador)
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