package com.tagmypet.ui.screens.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*
import com.tagmypet.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    planId: String,
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    // Inicializa Stripe e prepara checkout
    LaunchedEffect(Unit) {
        // Certifique-se de que Constants.STRIPE_PUBLISHABLE_KEY existe e começa com pk_test
        PaymentConfiguration.init(context, "pk_test_SUA_CHAVE_AQUI_OU_NO_CONSTANTS")
        viewModel.prepareCheckout(planId)
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val clientSecret by viewModel.paymentClientSecret.collectAsState()
    val paymentSuccess by viewModel.paymentSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> viewModel.onPaymentSheetResult(true, null)
            is PaymentSheetResult.Canceled -> viewModel.onPaymentSheetResult(false, null)
            is PaymentSheetResult.Failed -> viewModel.onPaymentSheetResult(
                false,
                result.error.localizedMessage
            )
        }
    }

    // Navegação automática após sucesso
    LaunchedEffect(paymentSuccess) {
        if (paymentSuccess) {
            navController.navigate(Screen.PaymentSuccess.route) {
                popUpTo(Screen.Home.route)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pagamento Seguro", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Erro",
                    color = Destructive,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (isLoading || clientSecret == null) {
                CircularProgressIndicator(color = Primary600)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Preparando pagamento...", color = TextSecondary)
            } else {
                Text(
                    text = "Total a pagar: R$ 39,90",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                ShadcnButton(
                    text = "Pagar com Cartão",
                    onClick = {
                        paymentSheet.presentWithPaymentIntent(
                            clientSecret!!,
                            com.stripe.android.paymentsheet.PaymentSheet.Configuration("Tag My Pet")
                        )
                    },
                    variant = ButtonVariant.PRIMARY,
                    icon = { Icon(Icons.Default.CreditCard, null, Modifier.size(20.dp)) },
                    modifier = Modifier.height(56.dp)
                )
            }
        }
    }
}