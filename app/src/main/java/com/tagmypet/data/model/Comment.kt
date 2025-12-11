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
    val totalReplies: Int = 0, // <-- NOVO: Total de replies no banco
    val nextReplyPage: Int = 2, // <-- NOVO: Próxima página a carregar (Inicialmente 2, pois a primeira página já foi pré-carregada (limit:2))
)