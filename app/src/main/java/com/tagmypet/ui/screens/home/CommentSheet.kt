package com.tagmypet.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tagmypet.data.model.Comment
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheet(
    postId: String,
    onDismiss: () -> Unit,
    onProfileClick: (String) -> Unit = {},
    viewModel: CommentViewModel = hiltViewModel(),
) {
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadMoreLoading by viewModel.isLoadMoreLoading.collectAsState() // <-- NOVO

    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }

    // Estado para quem estamos respondendo
    var parentCommentId by remember { mutableStateOf<String?>(null) }
    var parentCommentName by remember { mutableStateOf<String?>(null) }

    // NOVO: Estado para rolagem da lista
    val listState = rememberLazyListState()

    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }

    // NOVO: Lógica de Infinite Scroll para Comentários Raiz
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                // Carrega mais quando faltam 3 itens para o final
                if (lastIndex != null && lastIndex >= comments.size - 3) {
                    viewModel.loadMoreComments()
                }
            }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 36.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Comentários (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            HorizontalDivider(color = BorderColor)

            // --- LISTA DE COMENTÁRIOS ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary600)
                }
            } else if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum comentário ainda.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    state = listState, // <-- NOVO: Adiciona o estado para o Infinite Scroll
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        CommentItem(
                            comment = comment,
                            onProfileClick = onProfileClick,
                            onReplyClick = { name, commentId ->
                                parentCommentId = commentId
                                parentCommentName = name
                                val text = "@$name "
                                inputText = inputText.copy(
                                    text = text,
                                    selection = androidx.compose.ui.text.TextRange(text.length)
                                )
                                focusRequester.requestFocus()
                            },
                            onLoadMoreReplies = { id -> viewModel.loadMoreReplies(id) } // <-- NOVO
                        )
                    }

                    // Indicador de "Carregando Mais" no final da lista de comentários raiz
                    if (isLoadMoreLoading) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Primary600
                                )
                            }
                        }
                    }
                }
            }


            // --- INPUT FIXO NO RODAPÉ ---
            Surface(
                shadowElevation = 16.dp,
                color = Surface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Texto "Respondendo a X"
                    if (parentCommentName != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(InputBg)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Respondendo a ${parentCommentName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            // Botão para cancelar a resposta
                            IconButton(onClick = {
                                parentCommentId = null
                                parentCommentName = null
                                inputText = TextFieldValue(
                                    inputText.text.replace(
                                        "@${parentCommentName} ",
                                        ""
                                    )
                                )
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar do Usuário Logado (Mockado)
                        AsyncImage(
                            model = "https://i.pravatar.cc/300?u=maria",
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Escreva um comentário...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InputBg,
                                unfocusedContainerColor = InputBg,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Primary600,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botão Enviar
                        val canSend = inputText.text.isNotBlank()
                        IconButton(
                            onClick = {
                                viewModel.postComment(
                                    content = inputText.text,
                                    parentCommentId = parentCommentId
                                )
                                inputText = TextFieldValue("")
                                parentCommentId = null
                                parentCommentName = null
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
                                tint = if (canSend) Color.White else TextSecondary.copy(0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * FUNÇÃO DE VISUALIZAÇÃO RECURSIVA (2 NÍVEIS VISUAIS)
 */
@Composable
fun CommentItem(
    comment: Comment,
    onProfileClick: (String) -> Unit,
    onReplyClick: (userName: String, commentId: String) -> Unit,
    onLoadMoreReplies: (commentId: String) -> Unit, // <-- NOVO
) {
    // CORREÇÃO: Indentação fixa de 24.dp se o parentCommentId não for nulo (ou seja, se for uma resposta)
    val isReply = comment.parentCommentId != null
    val startPadding = if (isReply) 24.dp else 0.dp

    Column(
        modifier = Modifier.padding(
            top = 16.dp,
            start = startPadding
        )
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Avatar
            AsyncImage(
                model = comment.userAvatar,
                contentDescription = null,
                modifier = Modifier
                    .size(if (isReply) 24.dp else 32.dp)
                    .clip(CircleShape)
                    .background(InputBg)
                    .clickable { onProfileClick(comment.userId) },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.userName,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        modifier = Modifier.clickable { onProfileClick(comment.userId) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = comment.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Text(
                    text = "Responder",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onReplyClick(comment.userName, comment.id) }
                )
            }
        }

        // --- REPLIES (Exibição Aninhada) ---
        if (comment.replies.isNotEmpty()) {
            // As respostas são exibidas com um padding extra
            Column(modifier = Modifier.padding(start = 24.dp)) {
                comment.replies.forEach { reply ->
                    // Chamada recursiva para exibir a resposta (que agora sempre será Level 2)
                    CommentItem(
                        comment = reply,
                        onProfileClick = onProfileClick,
                        // Respostas de replies voltam ao pai do comment
                        onReplyClick = { name, _ -> onReplyClick(name, comment.id) },
                        onLoadMoreReplies = onLoadMoreReplies
                    )
                }
            }
        }

        // NOVO: BOTÃO "VER MAIS RESPOSTAS"
        // Exibe se for um comentário raiz E houver mais replies no total do que as carregadas.
        val loadedRepliesCount = comment.replies.size
        val hasMoreReplies = comment.totalReplies > loadedRepliesCount

        if (!isReply && hasMoreReplies) {
            TextButton(
                onClick = { onLoadMoreReplies(comment.id) },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 40.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    "Ver mais respostas (${comment.totalReplies - loadedRepliesCount} restantes)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary600
                )
            }
        }
    }
}