package com.tagmypet.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageEmoji: String,
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    // Injeta o ViewModel
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pages = listOf(
        OnboardingPage(
            title = "Bem-vindo ao Tag My Pet",
            description = "A maior rede de proteção e cuidado para o seu melhor amigo.",
            imageEmoji = "🐾"
        ),
        OnboardingPage(
            title = "Proteção Inteligente",
            description = "Use nossa Tag QR Code para garantir que seu pet sempre volte para casa.",
            imageEmoji = "🛡️"
        ),
        OnboardingPage(
            title = "Saúde em Dia",
            description = "Controle vacinas, remédios e consultas em um só lugar.",
            imageEmoji = "💉"
        ),
        OnboardingPage(
            title = "Comunidade Ativa",
            description = "Compartilhe momentos e conecte-se com outros tutores apaixonados.",
            imageEmoji = "📸"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botão Pular
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = {
                        // Salva que viu e vai para Login
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Login.route)
                    }
                ) {
                    Text("Pular", color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }

            Spacer(modifier = Modifier.weight(1f))

            // Indicadores
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Primary600 else BorderColor
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                    )
                }
            }

            // Botões de Navegação
            if (pagerState.currentPage == pages.lastIndex) {
                ShadcnButton(
                    text = "Criar Conta Grátis",
                    onClick = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Register.route)
                    },
                    variant = ButtonVariant.PRIMARY
                )
                Spacer(modifier = Modifier.height(12.dp))
                ShadcnButton(
                    text = "Já tenho conta",
                    onClick = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Login.route)
                    },
                    variant = ButtonVariant.OUTLINE
                )
            } else {
                ShadcnButton(
                    text = "Próximo",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    variant = ButtonVariant.PRIMARY
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Primary100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = page.imageEmoji,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}