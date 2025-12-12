package com.tagmypet.data.model

import com.squareup.moshi.Json
import com.tagmypet.utils.DateUtils // <--- IMPORTADO

data class Notification(
    @Json(name = "_id") val id: String,
    val recipient: String,
    val sender: User?, // Pode ser null se for aviso do sistema
    val type: NotificationType,
    val title: String,
    val message: String,
    val relatedId: String?,
    val relatedModel: String?,
    val isRead: Boolean,
    val createdAt: String,
) {
    // Formata a data para "Há x tempo" (Agora usando a função utilitária)
    val timeAgo: String
        get() = DateUtils.formatTimeAgo(createdAt)
}

enum class NotificationType {
    LIKE,
    COMMENT,
    FOLLOW,
    REPLY,
    SYSTEM,
    VACCINE,
    REMINDER
}