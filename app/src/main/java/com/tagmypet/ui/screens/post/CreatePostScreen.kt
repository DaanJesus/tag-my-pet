package com.tagmypet.ui.screens.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: CreatePostViewModel = hiltViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var textContent by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para abrir a galeria nativa do Android
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar publicação", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    // Botão Publicar no topo (Padrão mobile)
                    val isEnabled = textContent.isNotBlank() || selectedImageUri != null

                    TextButton(
                        onClick = {
                            viewModel.createPost(
                                content = textContent,
                                imageUri = selectedImageUri,
                                onSuccess = {
                                    // CORREÇÃO: Navega para Home e garante que o HomeViewModel recarregue o feed no ON_RESUME
                                    navController.navigate(Screen.Home.route) {
                                        // Limpa a tela de criação
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                }
                            )
                        },
                        enabled = isEnabled && !isLoading,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Primary600,
                            disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Primary600
                            )
                        } else {
                            Text("Publicar", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            // Barra de Ferramentas (Teclado)
            Surface(
                color = Surface,
                shadowElevation = 8.dp,
                modifier = Modifier.imePadding() // Sobe com o teclado
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        // Abre apenas imagens
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Outlined.AddPhotoAlternate, "Galeria", tint = Primary600)
                    }
                    IconButton(onClick = { /* Lógica de Câmera futura */ }) {
                        Icon(Icons.Outlined.CameraAlt, "Câmera", tint = Primary600)
                    }
                    IconButton(onClick = { /* Lógica de Localização futura */ }) {
                        Icon(Icons.Outlined.LocationOn, "Localização", tint = TextSecondary)
                    }
                }
            }
        },
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. INFO DO USUÁRIO
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = userAvatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(InputBg),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        userName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = InputBg,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            "Público",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. CAMPO DE TEXTO
            TextField(
                value = textContent,
                onValueChange = { textContent = it },
                placeholder = {
                    Text(
                        "No que seu pet está pensando?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Primary600
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )

            // 3. PREVIEW DA IMAGEM SELECIONADA
            AnimatedVisibility(visible = selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Imagem selecionada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(), // Ajusta altura mantendo proporção
                        contentScale = ContentScale.FillWidth
                    )

                    // Botão Remover Imagem
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remover",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}