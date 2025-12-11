package com.tagmypet.data.model

data class Plan(
    val id: PlanType,
    val name: String,
    val price: Double,
    val description: String,
    val features: List<String>,
    val isPopular: Boolean = false,
    val isPremium: Boolean = false
) {
    // Helper para formatar preço (R$ 19,90)
    val formattedPrice: String
        get() = if (price == 0.0) "FREE" else "R$ ${String.format("%.2f", price).replace('.', ',')}"
}