package com.tagmypet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tagmypet.ui.components.icons.PawIcon
import com.tagmypet.ui.theme.*
import com.tagmypet.utils.DateUtils // <--- IMPORTADO
import com.tagmypet.utils.NumberUtils // <--- IMPORTADO

// Enum movido para o topo
enum class PostOption {
    SAVE, REPORT, COPY_LINK, UNFOLLOW
}

data class PostVO(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val location: String? = null,
    val imageUrl: String,
    val content: String,
    val likesCount: Int,
    val isLiked: Boolean,
    val commentsCount: Int,
    val createdAt: String,
    val isLostAlert: Boolean = false,
)

@Composable
fun PostCard(
    post: PostVO,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onShareClick: (String) -> Unit, // Agora recebe o ID para criar o cartaz/share
    onOptionClick: (PostOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (post.isLiked) 1.2f else 1f,
        label = "paw_scale"
    )

    var showMenu by remember { mutableStateOf(false) }

    // Formatações (Problema 4 e 5)
    val formattedDate = remember(post.createdAt) { DateUtils.formatTimeAgo(post.createdAt) }
    val formattedLikesCount = remember(post.likesCount) { NumberUtils.formatCount(post.likesCount) }
    val formattedCommentsCount =
        remember(post.commentsCount) { NumberUtils.formatCount(post.commentsCount) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp),
        border = if (post.isLostAlert) BorderStroke(
            1.dp,
            Destructive.copy(alpha = 0.5f)
        ) else BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // --- ALERT BANNER ---
            if (post.isLostAlert) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Destructive)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pet Desaparecido - Ajude a encontrar!",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }

            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userAvatarUrl
                        ?: "https://ui-avatars.com/api/?name=${post.userName}",
                    contentDescription = "Avatar de ${post.userName}",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(InputBg)
                        .clickable { onProfileClick(post.userId) },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onProfileClick(post.userId) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) { // <--- CORRIGIDO: Agrupa nome e data
                        Text(
                            text = post.userName,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            // <--- PROBLEMA 4: Data ao lado do nome
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary,
                        )
                    }
                    if (!post.location.isNullOrBlank()) {
                        Text(
                            text = post.location,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreHoriz, "Opções", tint = TextPrimary)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = Surface,
                        modifier = Modifier
                            .background(Surface)
                            .width(180.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Salvar", color = TextPrimary) },
                            onClick = { showMenu = false; onOptionClick(PostOption.SAVE) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.BookmarkBorder,
                                    null,
                                    tint = TextPrimary
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copiar Link", color = TextPrimary) },
                            onClick = { showMenu = false; onOptionClick(PostOption.COPY_LINK) },
                            leadingIcon = { Icon(Icons.Outlined.Link, null, tint = TextPrimary) }
                        )
                        HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
                        DropdownMenuItem(
                            text = { Text("Denunciar", color = Destructive) },
                            onClick = { showMenu = false; onOptionClick(PostOption.REPORT) },
                            leadingIcon = { Icon(Icons.Outlined.Flag, null, tint = Destructive) }
                        )
                    }
                }
            }

            // --- IMAGEM ---
            if (post.imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InputBg)
                        .aspectRatio(1f)
                ) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Foto do post",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // --- AÇÕES E CONTADORES ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LIKE + CONTADOR
                IconButton(onClick = onLikeClick) {
                    Box(modifier = Modifier.scale(if (post.isLiked) scale else 1f)) {
                        PawIcon(
                            color = if (post.isLiked) PawLove else TextPrimary,
                            filled = post.isLiked,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                if (post.likesCount > 0) {
                    Text(
                        text = formattedLikesCount, // <--- PROBLEMA 5: Formata Likes
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )
                }

                // COMENTÁRIO + CONTADOR
                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Outlined.ModeComment,
                        contentDescription = "Comentar",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                if (post.commentsCount > 0) {
                    Text(
                        text = formattedCommentsCount, // <--- PROBLEMA 5: Formata Comments
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // CORREÇÃO: BOTÃO "COMPARTILHAR / CARTAZ"
                IconButton(onClick = { onShareClick(post.id) }) {
                    val icon =
                        if (post.isLostAlert) Icons.Outlined.Emergency else Icons.Outlined.Share
                    val description = if (post.isLostAlert) "Gerar Cartaz" else "Compartilhar"
                    Icon(
                        imageVector = icon,
                        contentDescription = description,
                        tint = if (post.isLostAlert) Destructive else TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // SALVAR (Bookmark)
                IconButton(onClick = { onOptionClick(PostOption.SAVE) }) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Salvar",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // --- LEGENDA ---
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                // A data (createdAt) foi movida para o Header.
            }
        }
    }
}