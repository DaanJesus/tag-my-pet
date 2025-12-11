package com.tagmypet.ui.screens.help

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class FaqItem(val question: String, val answer: String)

@HiltViewModel
class HelpViewModel @Inject constructor() : ViewModel() {
    private val _faqs = MutableStateFlow(listOf(
        FaqItem("Como funciona a tag QR Code?", "A tag é uma plaquinha que você coloca na coleira do seu pet. Quando alguém escaneia o QR Code, as informações do pet aparecem na tela."),
        FaqItem("Quanto tempo leva para receber minha tag?", "Após a confirmação do pagamento, sua tag é enviada em até 7 dias úteis."),
        FaqItem("Como marcar meu pet como desaparecido?", "Vá até seu Perfil, selecione o pet e toque no botão 'Marcar como Desaparecido'."),
        FaqItem("O GPS funciona em qualquer lugar?", "O GPS funciona em áreas com cobertura de sinal celular. A precisão pode variar."),
        FaqItem("Posso cancelar minha assinatura?", "Sim! Você pode cancelar a qualquer momento em Configurações > Planos.")
    ))
    val faqs: StateFlow<List<FaqItem>> = _faqs
}