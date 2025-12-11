package com.tagmypet.ui.screens.profile.pet_form

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.components.ShadcnInput
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetFormScreen(
    navController: NavController,
    petId: String, // "new" ou UUID
    viewModel: PetFormViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val isEditing = petId != "new"

    // Launcher para foto
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onPhotoSelected(uri) }
    )

    // Efeito para carregar dados se for edição
    LaunchedEffect(petId) {
        if (isEditing) viewModel.loadPet(petId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditing) "Editar Pet" else "Novo Pet",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. FOTO DO PET
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(InputBg)
                        .clickable {
                            photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        .border(1.dp, BorderColor, CircleShape)
                ) {
                    if (state.photoUri != null || state.currentPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = state.photoUri ?: state.currentPhotoUrl,
                            contentDescription = "Foto do Pet",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(40.dp)
                        )
                    }
                }
                // Badge de "Editar"
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 12.dp)
                        .background(Surface, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Alterar foto",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. CAMPOS PRINCIPAIS
            ShadcnInput(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = "Nome do Pet",
                placeholder = "Ex: Thor"
            )

            // Espécie (Chips)
            Column {
                Text("Espécie", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpeciesChip(
                        label = "Cachorro",
                        selected = state.species == "Dog",
                        onClick = { viewModel.onSpeciesChange("Dog") }
                    )
                    SpeciesChip(
                        label = "Gato",
                        selected = state.species == "Cat",
                        onClick = { viewModel.onSpeciesChange("Cat") }
                    )
                }
            }

            ShadcnInput(
                value = state.breed,
                onValueChange = { viewModel.onBreedChange(it) },
                label = "Raça",
                placeholder = "Ex: Vira-lata, Siamês..."
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ShadcnInput(
                        value = state.age,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) viewModel.onAgeChange(
                                it
                            )
                        },
                        label = "Idade (anos)",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ShadcnInput(
                        value = state.weight,
                        onValueChange = { viewModel.onWeightChange(it) }, // Permitir ponto/vírgula na lógica do VM
                        label = "Peso (kg)",
                        placeholder = "0.0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

            // 3. SAÚDE (Alergias e Remédios)
            // Componente de lista dinâmica
            DynamicListItem(
                title = "Alergias",
                items = state.allergies,
                onAddItem = { viewModel.addAllergy(it) },
                onRemoveItem = { viewModel.removeAllergy(it) },
                placeholder = "Adicionar alergia (ex: Frango)"
            )

            DynamicListItem(
                title = "Medicamentos",
                items = state.medications,
                onAddItem = { viewModel.addMedication(it) },
                onRemoveItem = { viewModel.removeMedication(it) },
                placeholder = "Adicionar remédio (ex: Simparic)"
            )

            // 4. BOTÃO SALVAR
            Spacer(modifier = Modifier.height(16.dp))
            ShadcnButton(
                text = if (state.isLoading) "Salvando..." else "Salvar Pet",
                onClick = {
                    viewModel.savePet {
                        // Apenas volta para a tela anterior (ProfileScreen)
                        navController.popBackStack()
                    }
                },
                enabled = !state.isLoading && state.name.isNotBlank(),
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.height(56.dp)
            )
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun SpeciesChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) Primary100 else Surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) Primary600 else BorderColor),
        modifier = Modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Primary600 else TextSecondary
            )
        }
    }
}

@Composable
fun DynamicListItem(
    title: String,
    items: List<String>,
    onAddItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    placeholder: String,
) {
    var tempText by remember { mutableStateOf("") }

    Column {
        Text(title, style = MaterialTheme.typography.labelMedium, color = TextSecondary)

        // Lista de Itens (Chips)
        if (items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    Surface(
                        color = InputBg,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                start = 12.dp,
                                end = 4.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            )
                        ) {
                            Text(
                                item,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            IconButton(
                                onClick = { onRemoveItem(item) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input com botão de adicionar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                ShadcnInput(
                    value = tempText,
                    onValueChange = { tempText = it },
                    label = "",
                    placeholder = placeholder,
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (tempText.isNotBlank()) {
                        onAddItem(tempText)
                        tempText = ""
                    }
                },
                enabled = tempText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (tempText.isNotBlank()) Primary600 else InputBg,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Add,
                    null,
                    tint = if (tempText.isNotBlank()) Color.White else TextSecondary
                )
            }
        }
    }
}