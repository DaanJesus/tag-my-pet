package com.tagmypet.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val activeIcon: ImageVector? = null,
    val inactiveIcon: ImageVector? = null,
) {
    // Menu Principal (Drawer)
    data object Home : Screen("home", "Início", Icons.Filled.Home, Icons.Outlined.Home)
    data object Profile : Screen("profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person) {
        fun createRouteWithTab(tab: String) = "profile?tab=$tab"
    }

    data object Plans :
        Screen("plans", "Planos", Icons.Filled.CreditCard, Icons.Outlined.CreditCard)

    data object Scan : Screen("scan", "Escanear Tag", Icons.Filled.QrCode, Icons.Filled.QrCode)
    data object Settings :
        Screen("settings", "Configurações", Icons.Filled.Settings, Icons.Outlined.Settings)

    data object Help : Screen("help", "Ajuda", Icons.Filled.Help, Icons.Outlined.HelpOutline)

    // --- FLUXO DE ENTRADA (AUTH) ---
    data object Onboarding : Screen("onboarding", "Bem-vindo")
    data object Login : Screen("login", "Entrar")
    data object Register : Screen("register", "Criar Conta")

    // --- FUNCIONALIDADES CENTRAIS ---
    data object Search : Screen("search", "Buscar")
    data object Notifications : Screen("notifications", "Notificações")

    // --- ROTAS DINÂMICAS ---
    data object CreatePost : Screen("create_post", "Criar Publicação")

    data object PublicProfile : Screen("public_profile/{userId}", "Perfil") {
        fun createRoute(userId: String) = "public_profile/$userId"
    }

    data object Chat : Screen("chat/{chatId}", "Chat") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }

    data object EditProfile : Screen("edit_profile", "Editar Perfil")

    data object PetForm : Screen("pet_form/{petId}", "Gerenciar Pet") {
        fun createRoute(petId: String = "new") = "pet_form/$petId"
    }

    data object PetHealth : Screen("pet_health/{petId}", "Carteira de Vacinação") {
        fun createRoute(petId: String) = "pet_health/$petId"
    }

    data object PosterPreview : Screen("poster_preview/{petId}", "Cartaz de Procurado") {
        fun createRoute(petId: String) = "poster_preview/$petId"
    }

    data object Payment : Screen("payment/{planId}", "Pagamento") {
        fun createRoute(planId: String) = "payment/$planId"
    }

    data object PaymentSuccess : Screen("payment_success", "Sucesso")

    data object ScanError : Screen("scan_error", "Erro na Leitura")
}