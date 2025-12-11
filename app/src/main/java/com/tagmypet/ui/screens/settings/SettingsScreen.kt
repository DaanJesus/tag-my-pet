package com.tagmypet.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController, // Adicionado para navegação
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
) {
    val notifications by viewModel.notificationsEnabled.collectAsState()
    val emailUpdates by viewModel.emailUpdatesEnabled.collectAsState()
    val darkMode by viewModel.darkModeEnabled.collectAsState()
    val logoutEvent by viewModel.logoutEvent.collectAsState()

    LaunchedEffect(logoutEvent) {
        if (logoutEvent) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Configurações", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- PLANOS E ASSINATURA (RESTAURADO) ---
            item {
                SettingsSection(title = "Assinatura") {
                    SettingItem(
                        icon = Icons.Outlined.CreditCard,
                        title = "Gerenciar Plano",
                        subtitle = "Ver opções e pagamentos",
                        onClick = { navController.navigate(Screen.Plans.route) } // <--- NAVEGAÇÃO PARA PLANOS
                    )
                }
            }

            // --- NOTIFICAÇÕES ---
            item {
                SettingsSection(title = "Notificações") {
                    SettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notificações push",
                        trailing = {
                            Switch(
                                checked = notifications,
                                onCheckedChange = { viewModel.toggleNotification(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary600)
                            )
                        }
                    )
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    SettingItem(
                        icon = Icons.Outlined.Email,
                        title = "E-mail de novidades",
                        trailing = {
                            Switch(
                                checked = emailUpdates,
                                onCheckedChange = { viewModel.toggleEmailUpdates(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary600)
                            )
                        }
                    )
                }
            }

            // --- PRIVACIDADE ---
            item {
                SettingsSection(title = "Privacidade e Segurança") {
                    SettingItem(
                        icon = Icons.Outlined.Lock,
                        title = "Alterar senha",
                        onClick = {}
                    )
                }
            }

            // --- CONTA ---
            item {
                SettingsSection(title = "Conta") {
                    SettingItem(
                        icon = Icons.Outlined.Logout,
                        title = "Sair da conta",
                        onClick = { viewModel.logout() },
                        iconColor = Primary600
                    )
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                    SettingItem(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Excluir minha conta",
                        onClick = { viewModel.deleteAccount() },
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider(color = BorderColor)
            content()
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    isDestructive: Boolean = false,
    iconColor: Color = Primary600,
) {
    val finalIconColor = if (isDestructive) Destructive else iconColor
    val finalTitleColor = if (isDestructive) Destructive else TextPrimary
    val bgIconColor = if (isDestructive) DestructiveBg else Primary100

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícone com fundo colorido
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgIconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = finalIconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Textos
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = finalTitleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Ação ou Seta
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}