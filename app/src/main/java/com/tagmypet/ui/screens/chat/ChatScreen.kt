package com.tagmypet.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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
import com.tagmypet.ui.components.ShadcnInput
import com.tagmypet.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val chatTitle by viewModel.chatTitle.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll para o final sempre que a lista de mensagens mudar
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size) // .size pega o último índice + typing indicator
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar com status online
                        Box {
                            AsyncImage(
                                model = "https://i.pravatar.cc/150?u=${chatTitle}",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(InputBg),
                                contentScale = ContentScale.Crop
                            )
                            // Bolinha Online
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E)) // Verde Online
                                    .border(2.dp, Surface, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(chatTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                if (isTyping) "Digitando..." else "Online",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isTyping) Primary600 else TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu de opções do chat */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opções")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            // Barra de Input Fixa
            Surface(
                shadowElevation = 12.dp,
                tonalElevation = 2.dp,
                color = Surface
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding() // Respeita a barra de gestos
                        .imePadding(), // Sobe com o teclado
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ShadcnInput(
                            value = inputText,
                            onValueChange = { inputText = it },
                            label = "",
                            placeholder = "Digite uma mensagem...",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    // Botão Enviar
                    val canSend = inputText.isNotBlank()
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = canSend,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (canSend) Primary600 else InputBg,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (canSend) Color.White else TextSecondary
                        )
                    }
                }
            }
        },
        containerColor = Background // Fundo geral do chat (Creme suave)
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Renderiza Mensagens
            items(messages) { message ->
                MessageBubble(message = message)
            }

            // Indicador de "Digitando" (Bubble fake)
            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isMe = message.isFromMe

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // O Balão
        Surface(
            color = if (isMe) Primary600 else Surface,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMe) 18.dp else 2.dp, // "Ponta" do balão
                bottomEnd = if (isMe) 2.dp else 18.dp
            ),
            shadowElevation = 1.dp,
            border = if (!isMe) androidx.compose.foundation.BorderStroke(1.dp, BorderColor) else null,
            modifier = Modifier.widthIn(max = 280.dp) // Limita a largura máxima
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMe) Color.White else TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Timestamp
        Text(
            text = message.timestamp,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun TypingIndicator() {
    Surface(
        color = Surface,
        shape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 2.dp,
            bottomEnd = 18.dp
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.width(60.dp).height(40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Pontinhos simples (...)
            Text(
                "...",
                style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 2.sp),
                color = TextSecondary
            )
        }
    }
}