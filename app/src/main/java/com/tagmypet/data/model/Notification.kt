package com.tagmypet.data.model

import com.squareup.moshi.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    // Formata a data para "Há x tempo"
    val timeAgo: String
        get() = try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = format.parse(createdAt) ?: Date()
            val diff = Date().time - date.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                seconds < 60 -> "Agora mesmo"
                minutes < 60 -> "Há $minutes min"
                hours < 24 -> "Há $hours h"
                days < 7 -> "Há $days dias"
                else -> {
                    val outFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    outFormat.format(date)
                }
            }
        } catch (e: Exception) {
            "Recente"
        }
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