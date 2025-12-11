package com.tagmypet.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosterPreviewScreen(
    navController: NavController,
    petId: String,
) {
    // Dados Mockados do Pet (Em app real, viria do ViewModel baseado no ID)
    val petName = "Thor"
    val petBreed = "Golden Retriever"
    val lastSeen = "Visto por último no Jardins, SP"
    val contact = "(11) 99999-9999"
    val photoUrl =
        "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=500"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerar Cartaz", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Compartilhe este cartaz nas redes sociais para ajudar a encontrar seu pet!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- O CARTAZ ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f), // Proporção Instagram (4:5)
                shape = RoundedCornerShape(0.dp), // Cartazes geralmente são quadrados/retangulares
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Vermelho
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Destructive)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "PROCURA-SE",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White,
                            letterSpacing = 4.sp
                        )
                    }

                    // Foto
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )

                    // Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            petName.uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            petBreed,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Primary100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = lastSeen,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Primary700,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            "Se viu este pet, entre em contato:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            contact,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Footer (App Branding)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Criado com Tag My Pet App",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botões de Ação
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShadcnButton(
                    text = "Baixar Imagem",
                    onClick = { /* Lógica de download */ },
                    variant = ButtonVariant.OUTLINE,
                    modifier = Modifier.weight(1f)
                )
                ShadcnButton(
                    text = "Compartilhar",
                    onClick = { /* Intent de Share */ },
                    variant = ButtonVariant.PRIMARY,
                    icon = { Icon(Icons.Default.Share, null, Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}