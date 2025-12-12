package com.tagmypet.data.model

data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timeAgo: String,
    val replies: List<Comment> = emptyList(),
    val parentCommentId: String? = null, // CORREÇÃO: Campo adicionado
    val totalReplies: Int = 0, // <-- Total de replies no banco
    val nextReplyPage: Int = 1, // <-- CORREÇÃO: Deve iniciar em 1 (para o novo cálculo de skip)
)