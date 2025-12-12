package com.tagmypet.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    /**
     * Converte um ISO String para o formato "Há x tempo".
     * Lógica copiada e adaptada de Notification.kt para ser reutilizável.
     */
    fun formatTimeAgo(iso: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = format.parse(iso) ?: Date()
            val diff = Date().time - date.time

            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            when {
                seconds < 60 -> "Agora mesmo"
                minutes < 60 -> "Há $minutes min"
                hours < 24 -> "Há $hours h"
                days < 7 -> "Há $days dias"
                days < 30 -> "Há ${days / 7} sem"
                days < 365 -> "Há ${days / 30} meses"
                else -> {
                    val outFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    outFormat.format(date)
                }
            }
        } catch (e: Exception) {
            "Recente"
        }
    }
}