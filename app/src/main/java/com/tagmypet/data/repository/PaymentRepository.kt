package com.tagmypet.data.repository

import com.tagmypet.data.api.ApiService
import com.tagmypet.data.api.PaymentIntentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val apiService: ApiService,
) {
    // Cria a intenção de pagamento no backend (que fala com o Stripe)
    suspend fun createPaymentIntent(planType: String): Resource<PaymentIntentResponse> {
        return try {
            // O backend espera { "planType": "PREMIUM", "currency": "brl" }
            val request = mapOf("planType" to planType, "currency" to "brl")
            val response = apiService.createPaymentIntent(request)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Erro ao iniciar pagamento")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro de conexão")
        }
    }
}