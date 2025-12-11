package com.tagmypet.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
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
import com.tagmypet.data.model.Pet
import com.tagmypet.data.model.User
import com.tagmypet.ui.components.ShadcnInput
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            Surface(
                color = Background,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    // Barra de Busca
                    ShadcnInput(
                        value = state.query,
                        onValueChange = { viewModel.onQueryChange(it) },
                        label = "", // Sem label flutuante para parecer uma barra de busca
                        placeholder = "Buscar pessoas ou pets...",
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Loading
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary600)
                    }
                }
            }

            // Resultados de Usuários
            if (state.users.isNotEmpty()) {
                item {
                    Text(
                        "Pessoas",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                }
                items(state.users) { user ->
                    UserResultItem(user) {
                        navController.navigate(Screen.PublicProfile.createRoute(user.id))
                    }
                }
            }

            // Resultados de Pets
            if (state.pets.isNotEmpty()) {
                item {
                    Text(
                        "Pets",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                }
                items(state.pets) { pet ->
                    PetResultItem(pet) {
                        // Se tiver ownerId, vai pro perfil do dono para ver o pet
                        if (pet.ownerId.isNotBlank()) {
                            navController.navigate(Screen.PublicProfile.createRoute(pet.ownerId))
                        }
                    }
                }
            }

            // Estado Vazio
            if (!state.isLoading && state.query.isNotBlank() && state.users.isEmpty() && state.pets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum resultado encontrado.", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun UserResultItem(user: User, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.photoUrl ?: "https://ui-avatars.com/api/?name=${user.name}",
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(InputBg),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Person, null, tint = TextSecondary.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun PetResultItem(pet: Pet, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pet.photoUrl,
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
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = pet.breed,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Pets, null, tint = TextSecondary.copy(alpha = 0.5f))
        }
    }
}