package com.tagmypet.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tagmypet.ui.components.AdMobNativePost
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.PostCard
import com.tagmypet.ui.components.PostOption
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
) {
    val feedItems by viewModel.feedItems.collectAsState()
    val tabs = listOf("Feed", "Reels", "Comércios")

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val selectedTab = pagerState.currentPage
    val scope = rememberCoroutineScope()

    var activeCommentPostId by remember { mutableStateOf<String?>(null) } // ID do post que abriu o sheet

    // --- BLOCO CRÍTICO: CHAMA O COMMENT SHEET PASSANDO O ID ---
    if (activeCommentPostId != null) {
        CommentSheet(
            postId = activeCommentPostId!!, // <--- ID PASSADO AQUI
            onDismiss = { activeCommentPostId = null },
            onProfileClick = { userId ->
                activeCommentPostId = null
                navController.navigate(Screen.PublicProfile.createRoute(userId))
            }
        )
    }
    // ---------------------------------------------------------


    // --- SINCRONIZAÇÃO AO RETORNAR (DISPOSABLE EFFECT) ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = object : LifecycleEventObserver {
            override fun onStateChanged(
                source: androidx.lifecycle.LifecycleOwner,
                event: Lifecycle.Event,
            ) {
                // Quando a tela volta a ser ativa (após CreatePost ou PetForm), recarrega o feed
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.loadFeed()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Início", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Buscar",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notificações",
                            tint = TextPrimary
                        )
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
                .background(Background)
        ) {
            // --- TABS SUPERIORES ---
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Surface,
                contentColor = Primary600,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Primary600
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // --- CONTEÚDO (Horizontal Pager) ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> FeedTab(
                        navController,
                        feedItems,
                        onCommentClick = { activeCommentPostId = it }, // Define o ID do post
                        viewModel = viewModel
                    )

                    1 -> ReelsTab()
                    2 -> CommerceMapTab()
                }
            }
        }
    }
}

// --- TAB 1: FEED ---
@Composable
fun FeedTab(
    navController: NavController,
    feedItems: List<FeedItem>,
    onCommentClick: (String) -> Unit, // Agora recebe o ID do post
    viewModel: HomeViewModel,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header de Input
        item(key = "header_input") {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 8.dp
                )
            ) {
                HomeInputSection(
                    userAvatarUrl = "https://i.pravatar.cc/300?u=maria",
                    onInputClick = { navController.navigate(Screen.CreatePost.route) }
                )
            }
        }

        // Stories
        item(key = "stories_row") {
            Column {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    item(key = "story_commerces") {
                        StoryItem(imageUrl = null, label = "Comércios", isSpecial = true)
                    }
                    items(6) { i ->
                        StoryItem(
                            imageUrl = "https://images.unsplash.com/photo-${1550000000000 + i * 100}?w=100&h=100&fit=crop",
                            label = "Pet Shop ${i + 1}"
                        )
                    }
                }
                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            }
        }

        // Lista Mista (Posts + Ads)
        items(feedItems) { item ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                when (item) {
                    is FeedItem.Post -> {
                        PostCard(
                            post = item.data,
                            onLikeClick = { viewModel.toggleLike(item.data.id) },
                            onCommentClick = { onCommentClick(item.data.id) },
                            onProfileClick = { userId ->
                                navController.navigate(Screen.PublicProfile.createRoute(userId))
                            },
                            onShareClick = { postId ->
                                // Lógica de compartilhamento para o post (leva ao cartaz se for perdido)
                                navController.navigate(Screen.PosterPreview.createRoute(postId))
                            },
                            onOptionClick = { option ->
                                if (option == PostOption.SAVE) {
                                    // TODO: Lógica de salvar bookmark
                                } else if (option == PostOption.REPORT) {
                                    // TODO: Lógica de denuncia
                                }
                            }
                        )
                    }

                    is FeedItem.Ad -> {
                        AdMobNativePost()
                    }
                }
            }
        }
    }
}

// --- TAB 2: REELS ---
@Composable
fun ReelsTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.PlayArrow,
                null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Vídeos Curtos em Breve", color = TextSecondary)
        }
    }
}

// --- TAB 3: COMÉRCIOS & MAPA ---
@Composable
fun CommerceMapTab() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa Mockado
        AsyncImage(
            model = "https://i.imgur.com/2Y5l9xL.png",
            contentDescription = "Mapa",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Pins
        MapPin(100, 200, "Parque Ibirapuera", true)
        MapPin(250, 400, "Pet Shop Legal", false)
        MapPin(150, 500, "Thor (Amigo)", false, isFriend = true)

        // Search Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth()
                // Adicione o clique aqui
                .clickable {
                    // TODO: Criar Screen.Search e navegar
                    // navController.navigate("search")
                },
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Primary600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar parques, pet shops...", color = TextSecondary)
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun NativeAdCard(ad: FeedItem.Ad) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = Color.Black, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        "Patrocinado",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(ad.title, style = MaterialTheme.typography.labelLarge)
            }

            AsyncImage(
                model = ad.imageUrl,
                contentDescription = "Anúncio",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.91f)
                    .background(InputBg),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    ad.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* Abrir link */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary100,
                        contentColor = Primary600
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Ver Mais", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun MapPin(x: Int, y: Int, label: String, isPark: Boolean, isFriend: Boolean = false) {
    val color = when {
        isPark -> Color(0xFF22C55E)
        isFriend -> Primary600
        else -> Color.Blue
    }

    Column(
        modifier = Modifier.offset(x.dp, y.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = color,
            shape = CircleShape,
            modifier = Modifier
                .size(40.dp)
                .border(2.dp, Color.White, CircleShape),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isFriend) {
                    AsyncImage(
                        model = "https://i.pravatar.cc/150?u=thor",
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Surface(color = Color.White, shape = RoundedCornerShape(4.dp), shadowElevation = 2.dp) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun HomeInputSection(
    userAvatarUrl: String?,
    onInputClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = userAvatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(InputBg),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(InputBg)
                    .clickable { onInputClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "No que seu pet está pensando?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onInputClick) {
                Icon(
                    Icons.Outlined.Image,
                    "Foto",
                    tint = Color(0xFF45BD62),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun StoryItem(
    imageUrl: String?,
    label: String,
    isSpecial: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    if (isSpecial) Brush.linearGradient(listOf(GradientStart, GradientEnd))
                    else SolidColor(Color.Transparent)
                )
                .border(
                    width = if (isSpecial) 0.dp else 2.dp,
                    color = if (isSpecial) Color.Transparent else Primary100,
                    shape = CircleShape
                )
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(if (imageUrl == null) Surface else InputBg),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Primary600,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 1,
        )
    }
}