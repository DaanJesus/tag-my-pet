package com.tagmypet.ui.screens.plans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tagmypet.data.model.Plan
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*
import kotlin.math.absoluteValue

// Cores Específicas para destaque do Premium
val PremiumGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFEF9C3), Color(0xFFFEF3C7))
)
val PremiumBorder = Color(0xFFEAB308)
val PremiumText = Color(0xFFA16207)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    navController: NavController,
    viewModel: PlansViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
) {
    val plans by viewModel.plans.collectAsState()

    // Configuração do Pager (Carrossel)
    val pagerState = rememberPagerState(pageCount = { plans.size }, initialPage = 1)
    val currentSelectedPlan = plans.getOrNull(pagerState.currentPage)

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Planos", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- CABEÇALHO ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Escolha seu plano",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Deslize para comparar as opções",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
            }

            // --- CARROSSEL ---
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 48.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.weight(1f)
            ) { page ->
                val plan = plans[page]
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                val scale = lerp(
                    start = 0.85f, stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )
                val alpha = lerp(
                    start = 0.6f, stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .fillMaxHeight()
                ) {
                    PlanCard(plan = plan)
                }
            }

            // --- RODAPÉ COM AÇÃO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if ((currentSelectedPlan?.price ?: 0.0) > 0) {
                    Surface(
                        color = Primary100,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "🏷️ Tag enviada grátis para todo o Brasil",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary700,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                val buttonText = if ((currentSelectedPlan?.price ?: 0.0) == 0.0) {
                    "Começar Grátis"
                } else {
                    "Assinar ${currentSelectedPlan?.name}"
                }

                ShadcnButton(
                    text = buttonText,
                    onClick = {
                        currentSelectedPlan?.let { plan ->
                            if (plan.price > 0) {
                                navController.navigate(Screen.Payment.createRoute(plan.id.name))
                            } else {
                                viewModel.selectPlan(plan)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.height(56.dp)
                )
            }
        }
    }
}

@Composable
fun PlanCard(plan: Plan) {
    val isPremium = plan.isPremium
    val isPopular = plan.isPopular

    val borderColor = when {
        isPremium -> PremiumBorder
        isPopular -> Primary600
        else -> BorderColor
    }

    val backgroundColor =
        if (isPremium) PremiumGradient else Brush.linearGradient(listOf(Surface, Surface))

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(if (isPremium || isPopular) 2.dp else 1.dp, borderColor),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(backgroundColor)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isPremium) PremiumText else if (isPopular) Primary600 else TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = plan.formattedPrice,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        ),
                        color = TextPrimary
                    )
                    if (plan.price > 0) {
                        Text(
                            text = "/mês",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = borderColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        plan.features.forEach { feature ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isPremium) PremiumText else Primary600,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (isPopular) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(Primary600, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "MAIS POPULAR",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        if (isPremium) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp)
                    .background(PremiumBorder, CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Premium",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}