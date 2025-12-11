package com.tagmypet.ui.screens.profile.public_profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tagmypet.data.api.UserStats
import com.tagmypet.data.model.User
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    navController: NavController,
    viewModel: PublicProfileViewModel = hiltViewModel(),
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Como ainda não temos endpoint de pets públicos, focamos nos posts
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        userProfile?.name ?: "Carregando...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, "Opções") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { innerPadding ->
        if (isLoading && userProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary600)
            }
        } else {
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                item {
                    userProfile?.let { user ->
                        ProfileHeaderSection(
                            user = user,
                            stats = stats,
                            isFollowing = isFollowing,
                            onFollowClick = { viewModel.toggleFollow() }
                        )
                    }
                }

                // Tabs (Grade de Posts)
                stickyHeader {
                    // Simples header de tabs (apenas Grid por enquanto)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Background)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.GridOn, null, tint = Primary600)
                    }
                    HorizontalDivider(color = BorderColor)
                }

                if (posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhuma publicação ainda.", color = TextSecondary)
                        }
                    }
                } else {
                    val rows = posts.chunked(3)
                    items(rows) { rowPosts ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (post in rowPosts) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(1.dp)
                                        .background(InputBg)
                                ) {
                                    AsyncImage(
                                        model = post.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            // Preenche espaço vazio se a última linha tiver menos de 3 itens
                            if (rowPosts.size < 3) {
                                repeat(3 - rowPosts.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(
    user: User,
    stats: UserStats,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = user.photoUrl ?: "https://ui-avatars.com/api/?name=${user.name}",
                contentDescription = null,
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .border(1.dp, BorderColor, CircleShape)
                    .background(InputBg),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(24.dp))

            // Estatísticas Reais
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStatItem(stats.posts.toString(), "Posts")
                ProfileStatItem(stats.followers.toString(), "Seguidores")
                ProfileStatItem(stats.following.toString(), "Seguindo")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        // Bio e localização podem vir do objeto User se adicionados futuramente
        Text(
            text = "Membro desde ${user.memberSince}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botão Seguir (Largura Total)
        ShadcnButton(
            text = if (isFollowing) "Seguindo" else "Seguir",
            onClick = onFollowClick,
            variant = if (isFollowing) ButtonVariant.SECONDARY else ButtonVariant.PRIMARY,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        )
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}