package com.tagmypet.data.model

import com.squareup.moshi.Json

data class User(
    // O MongoDB retorna "_id", mas queremos usar "id" no Kotlin
    @Json(name = "_id") val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val role: String = "user",
    val planType: PlanType = PlanType.FREE,
    val createdAt: String? = null
) {
    // Propriedade computada para manter compatibilidade com sua UI antiga
    val memberSince: String
        get() = createdAt ?: "Recente"
}

enum class PlanType(val label: String) {
    // Os nomes aqui (FREE, ESSENTIAL...) devem ser IGUAIS aos do banco de dados/API
    FREE("Gratuito"),
    ESSENTIAL("Essencial"),
    PREMIUM("Premium")
}