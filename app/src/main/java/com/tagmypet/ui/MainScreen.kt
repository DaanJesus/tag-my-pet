package com.tagmypet.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tagmypet.ui.components.AppDrawerContent
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.screens.auth.LoginScreen
import com.tagmypet.ui.screens.auth.OnboardingScreen
import com.tagmypet.ui.screens.auth.RegisterScreen
import com.tagmypet.ui.screens.chat.ChatScreen
import com.tagmypet.ui.screens.help.HelpScreen
import com.tagmypet.ui.screens.home.HomeScreen
import com.tagmypet.ui.screens.notification.NotificationScreen
import com.tagmypet.ui.screens.payment.PaymentScreen
import com.tagmypet.ui.screens.payment.PaymentSuccessScreen
import com.tagmypet.ui.screens.plans.PlansScreen
import com.tagmypet.ui.screens.post.CreatePostScreen
import com.tagmypet.ui.screens.post.PosterPreviewScreen
import com.tagmypet.ui.screens.profile.EditProfileScreen
import com.tagmypet.ui.screens.profile.ProfileScreen
import com.tagmypet.ui.screens.profile.health.PetHealthScreen
import com.tagmypet.ui.screens.profile.pet_form.PetFormScreen
import com.tagmypet.ui.screens.profile.public_profile.PublicProfileScreen
import com.tagmypet.ui.screens.scan.ScanErrorScreen
import com.tagmypet.ui.screens.scan.ScanTagScreen
import com.tagmypet.ui.screens.search.SearchScreen
import com.tagmypet.ui.screens.settings.SettingsScreen
import com.tagmypet.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    startDestination: String,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Função para abrir o menu (passada para as telas internas)
    val onOpenDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    // Bloqueia gestos de abrir menu lateral nas telas de Login/Onboarding
    val enableDrawerGestures = when {
        currentRoute == Screen.Onboarding.route -> false
        currentRoute == Screen.Login.route -> false
        currentRoute == Screen.Register.route -> false
        else -> true
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = enableDrawerGestures,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        // Removemos a TopBar daqui. Agora cada tela define a sua própria Scaffold/TopBar.
        Scaffold(
            containerColor = Background,
            // topBar = { ... } <- REMOVIDO PARA EVITAR DUPLICIDADE
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        // Aplicamos apenas o padding inferior (barras de navegação do sistema)
                        // O padding superior (status bar) é gerenciado pelas TopBars internas de cada tela
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
                // --- AUTH ---
                composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
                composable(Screen.Login.route) { LoginScreen(navController) }
                composable(Screen.Register.route) { RegisterScreen(navController) }

                // --- TELAS PRINCIPAIS (Passamos o onOpenDrawer para elas) ---
                composable(Screen.Home.route) {
                    HomeScreen(navController, onOpenDrawer = onOpenDrawer)
                }

                // CORREÇÃO: Rota Profile agora aceita um argumento 'tab'
                composable(
                    route = Screen.Profile.route + "?tab={tab}",
                    arguments = listOf(navArgument("tab") { defaultValue = "data" })
                ) { backStackEntry ->
                    ProfileScreen(
                        navController,
                        onOpenDrawer = onOpenDrawer,
                        initialTab = backStackEntry.arguments?.getString("tab") ?: "data"
                    )
                }

                composable(Screen.Plans.route) {
                    PlansScreen(navController, onOpenDrawer = onOpenDrawer)
                }
                composable(Screen.Scan.route) {
                    ScanTagScreen(navController, onOpenDrawer = onOpenDrawer)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(navController, onOpenDrawer = onOpenDrawer)
                }
                composable(Screen.Help.route) {
                    HelpScreen(onOpenDrawer = onOpenDrawer)
                }

                // --- TELAS SECUNDÁRIAS (Geralmente têm botão "Voltar" na própria TopBar) ---
                composable(Screen.Notifications.route) { NotificationScreen(navController) }
                composable(Screen.Search.route) { SearchScreen(navController) }
                composable(Screen.CreatePost.route) { CreatePostScreen(navController) }
                composable(Screen.EditProfile.route) { EditProfileScreen(navController) }
                composable(Screen.PaymentSuccess.route) { PaymentSuccessScreen(navController) }
                composable(Screen.ScanError.route) { ScanErrorScreen(navController) }

                // --- ROTAS DINÂMICAS ---
                composable("public_profile/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    PublicProfileScreen(userId = userId, navController = navController)
                }
                composable("pet_form/{petId}") { backStackEntry ->
                    val petId = backStackEntry.arguments?.getString("petId") ?: "new"
                    PetFormScreen(navController = navController, petId = petId)
                }
                composable("chat/{chatId}") { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                    ChatScreen(navController = navController)
                }
                composable("payment/{planId}") { backStackEntry ->
                    val planId = backStackEntry.arguments?.getString("planId") ?: "UNKNOWN"
                    PaymentScreen(planId = planId, navController = navController)
                }
                composable("pet_health/{petId}") { backStackEntry ->
                    val petId = backStackEntry.arguments?.getString("petId") ?: ""
                    PetHealthScreen(navController = navController, petId = petId)
                }
                composable("poster_preview/{petId}") { backStackEntry ->
                    val petId = backStackEntry.arguments?.getString("petId") ?: ""
                    PosterPreviewScreen(navController = navController, petId = petId)
                }
            }
        }
    }
}