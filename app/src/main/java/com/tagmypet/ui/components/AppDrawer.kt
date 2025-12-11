package com.tagmypet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tagmypet.ui.components.icons.PawIcon
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    onClose: () -> Unit
) {
    // Lista de itens do menu principal
    val menuItems = listOf(
        Screen.Home,
        Screen.Profile,
        Screen.Plans,
        Screen.Scan,
        Screen.Settings,
        Screen.Help
    )

    ModalDrawerSheet(
        drawerContainerColor = Surface,
        drawerContentColor = TextPrimary,
        windowInsets = WindowInsets.statusBars // Respeita a área da status bar
    ) {
        // --- CABEÇALHO (LOGO) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícone da Marca (Patinha)
                Surface(
                    color = Primary600,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PawIcon(color = Color.White, filled = true, modifier = Modifier.size(28.dp))
                    }
                }

                // Texto Logo
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("Tag", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp), color = TextPrimary)
                        Text("My", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp), color = Primary600)
                        Text("Pet", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp), color = TextPrimary)
                    }
                    Text(
                        "Proteção e carinho",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        HorizontalDivider(
            color = BorderColor.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- ITENS DO MENU ---
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)
        ) {
            menuItems.forEach { screen ->
                val isSelected = currentRoute == screen.route

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) screen.activeIcon ?: screen.inactiveIcon!! else screen.inactiveIcon!!,
                            contentDescription = null,
                            tint = if (isSelected) Primary600 else TextSecondary
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        onNavigate(screen)
                        onClose()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Primary100, // Laranja suave
                        unselectedContainerColor = Color.Transparent,
                        selectedTextColor = Primary600,
                        unselectedTextColor = TextPrimary
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // --- RODAPÉ (PERFIL RESUMIDO) ---
        Column(modifier = Modifier.padding(16.dp)) {
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 16.dp))

            Surface(
                color = InputBg.copy(alpha = 0.5f), // Fundo sutil
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigate(Screen.Profile)
                        onClose()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Mockado
                    AsyncImage(
                        model = "https://i.pravatar.cc/300?u=maria",
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Maria Silva",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Bolinha indicativa do plano
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEAB308)) // Dourado (Premium/Essencial)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Plano Essencial",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Versão do App
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}