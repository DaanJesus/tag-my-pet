package com.tagmypet.ui.screens.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tagmypet.data.model.Notification
import com.tagmypet.data.model.NotificationType
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificações", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(Icons.Default.Check, contentDescription = "Marcar todas como lidas")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { innerPadding ->
        if (state.isLoading && state.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary600)
            }
        } else if (state.notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nenhuma notificação por enquanto", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(state.notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            viewModel.markAsRead(notification.id)
                            // Navegar para o conteúdo relacionado (opcional)
                            // if (notification.relatedModel == "Post") navController.navigate(...)
                        }
                    )
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
) {
    val bgColor = if (notification.isRead) Background else Primary100.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícone ou Foto do Remetente
        Box(contentAlignment = Alignment.BottomEnd) {
            if (notification.sender?.photoUrl != null) {
                AsyncImage(
                    model = notification.sender.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Ícone genérico se não tiver remetente (Sistema)
                Surface(
                    color = Primary100,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Ícone do Tipo (Pequeno no canto)
            val typeIcon = getNotificationIcon(notification.type)
            val typeColor = getNotificationColor(notification.type)

            Surface(
                color = typeColor,
                shape = CircleShape,
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = 4.dp, y = 4.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Background)
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium),
                color = TextPrimary
            )
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.timeAgo,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.LIKE -> Icons.Default.Favorite
        NotificationType.COMMENT, NotificationType.REPLY -> Icons.Default.Message
        NotificationType.FOLLOW -> Icons.Default.PersonAdd
        NotificationType.SYSTEM, NotificationType.REMINDER -> Icons.Default.Notifications
        NotificationType.VACCINE -> Icons.Default.Warning
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.LIKE -> PawLove
        NotificationType.COMMENT, NotificationType.REPLY -> Color(0xFF3B82F6) // Azul
        NotificationType.FOLLOW -> Primary600
        NotificationType.SYSTEM, NotificationType.REMINDER -> Color(0xFFF59E0B) // Laranja/Amarelo
        NotificationType.VACCINE -> Destructive
    }
}