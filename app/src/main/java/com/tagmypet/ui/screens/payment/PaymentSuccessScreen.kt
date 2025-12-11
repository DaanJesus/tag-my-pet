package com.tagmypet.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*

@Composable
fun PaymentSuccessScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícone de Sucesso Gigante
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Sucesso",
            tint = Color(0xFF22C55E), // Verde Sucesso Vibrante
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Pagamento Confirmado!",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Obrigado! Seu plano foi ativado com sucesso. Agora seu pet conta com toda a proteção da nossa comunidade.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botão para voltar ao início limpando a pilha de navegação
        ShadcnButton(
            text = "Voltar para o Início",
            onClick = {
                navController.navigate(Screen.Home.route) {
                    // Remove telas de pagamento da pilha para que o botão "Voltar" não retorne ao checkout
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            variant = ButtonVariant.PRIMARY,
            modifier = Modifier.height(56.dp)
        )
    }
}