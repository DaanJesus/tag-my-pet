package com.tagmypet.ui.screens.plans

import androidx.lifecycle.ViewModel
import com.tagmypet.data.model.Plan
import com.tagmypet.data.model.PlanType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor() : ViewModel() {

    private val _plans = MutableStateFlow<List<Plan>>(emptyList())
    val plans: StateFlow<List<Plan>> = _plans.asStateFlow()

    // Simula qual plano o usuário tem atualmente (para destacar ou desabilitar botão)
    private val _currentPlan = MutableStateFlow(PlanType.ESSENTIAL)
    val currentPlan: StateFlow<PlanType> = _currentPlan.asStateFlow()

    init {
        loadPlans()
    }

    private fun loadPlans() {
        _plans.value = listOf(
            Plan(
                id = PlanType.FREE,
                name = "FREE",
                price = 0.0,
                description = "Perfeito para começar a compartilhar momentos do seu pet",
                features = listOf(
                    "3 publicações por dia",
                    "Cadastrar até 2 pets",
                    "Feed da comunidade",
                    "Curtir e comentar posts"
                )
            ),
            Plan(
                id = PlanType.ESSENTIAL,
                name = "ESSENCIAL",
                price = 19.90,
                description = "Tag QR Code para proteção do seu pet",
                features = listOf(
                    "Publicações ilimitadas",
                    "Cadastrar até 5 pets",
                    "2 coleções de fotos",
                    "Tag com QR Code inclusa",
                    "Chat com quem encontrar",
                    "Perfil verificado"
                ),
                isPopular = true
            ),
            Plan(
                id = PlanType.PREMIUM,
                name = "PREMIUM",
                price = 39.90,
                description = "Rastreamento GPS em tempo real",
                features = listOf(
                    "Tudo do plano Essencial",
                    "Pets ilimitados",
                    "Coleções ilimitadas",
                    "Tag com GPS inclusa",
                    "Rastreamento em tempo real",
                    "Alertas de movimento",
                    "Suporte prioritário"
                ),
                isPremium = true
            )
        )
    }

    fun selectPlan(plan: Plan) {
        // Lógica de assinatura iria aqui
        _currentPlan.value = plan.id
    }
}