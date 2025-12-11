package com.tagmypet.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tagmypet.data.model.Pet
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.components.icons.PawIcon
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    initialTab: String = "data",
) {
    val user by viewModel.user.collectAsState()
    val pets by viewModel.pets.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState() // Para SnackBar

    var selectedTab by remember {
        mutableIntStateOf(if (initialTab == "pets") 1 else 0)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    // --- CRÍTICO: LAUNCHER DE PERMISSÃO ---
    // Estado local para armazenar qual pet precisa de permissão (disparado pelo ViewModel)
    var petIdAwaitingPermission by remember { mutableStateOf<String?>(null) }

    // Launcher do Compose para o diálogo de permissão nativo
    val petPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            // Envia o resultado de volta para o ViewModel
            petIdAwaitingPermission?.let { petId ->
                // Ambos Fine e Coarse são necessários para o rastreamento ser preciso e funcionar como FGS Location
                viewModel.onPermissionResult(petId, fineLocationGranted && coarseLocationGranted)
                petIdAwaitingPermission = null
            }
        }
    )

    // Observa o evento do ViewModel para solicitar permissão
    LaunchedEffect(viewModel) {
        viewModel.permissionRequestEvent.collect { petId ->
            petIdAwaitingPermission = petId // Salva o ID para usar no onResult
            petPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    // --- FIM LAUNCHER ---


    // Força o recarregamento dos dados (incluindo pets) toda vez que a tela volta ao foreground (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(initialTab) {
        selectedTab = if (initialTab == "pets") 1 else 0
    }

    var showDeleteDialog by remember { mutableStateOf<Pet?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Exibir SnackBar de erro do ViewModel
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Remover Pet") },
            text = { Text("Tem certeza que deseja remover ${showDeleteDialog?.name}? Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { viewModel.removePet(it.id) }
                        showDeleteDialog = null
                    }, colors = ButtonDefaults.textButtonColors(contentColor = Destructive)
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            },
            containerColor = Surface
        )
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Adiciona SnackbarHost
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Perfil", style = MaterialTheme.typography.titleLarge, color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- HEADER DO USUÁRIO ---
            item {
                user?.let { u ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = u.photoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(InputBg),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    u.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    u.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Badge do Plano
                                Surface(
                                    color = Primary600,
                                    shape = RoundedCornerShape(50),
                                ) {
                                    Text(
                                        text = "Plano ${u.planType.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp, vertical = 4.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- TABS (Estilo Shadcn) ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(InputBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "Meus Dados", "Meus Pets (${pets.size})"
                    ).forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Surface else Color.Transparent)
                            .clickable { selectedTab = index }
                            .then(
                                if (isSelected) Modifier.border(
                                    1.dp,
                                    BorderColor.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                ) else Modifier
                            ), contentAlignment = Alignment.Center) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- CONTEÚDO DAS TABS ---
            if (selectedTab == 0) {
                // TAB: MEUS DADOS
                item {
                    user?.let { u ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ProfileInfoRow("Nome Completo", u.name)
                                ProfileInfoRow("E-mail", u.email)
                                ProfileInfoRow("Membro desde", u.memberSince)

                                Spacer(modifier = Modifier.height(8.dp))
                                ShadcnButton(text = "Editar perfil",
                                    onClick = { navController.navigate(Screen.EditProfile.route) },
                                    variant = ButtonVariant.OUTLINE,
                                    icon = {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            null,
                                            Modifier.size(16.dp)
                                        )
                                    })
                            }
                        }
                    }
                }
            } else {
                // TAB: MEUS PETS
                item {
                    ShadcnButton(
                        text = "Adicionar Pet",
                        onClick = { navController.navigate(Screen.PetForm.createRoute("new")) },
                        variant = ButtonVariant.SECONDARY,
                        icon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) },
                        modifier = Modifier.border(
                            1.dp, Primary600.copy(alpha = 0.3f), RoundedCornerShape(12.dp)
                        )
                    )
                }

                items(pets) { pet ->
                    PetCardItem(pet = pet,
                        onToggleLost = { viewModel.requestPermissionOrToggle(pet.id) },
                        onEdit = { navController.navigate(Screen.PetForm.createRoute(pet.id)) },
                        onDelete = { showDeleteDialog = pet },
                        onHealthClick = {
                            navController.navigate(Screen.PetHealth.createRoute(pet.id))
                        })
                }
            }
        }
    }
}

@Composable
fun PetCardItem(
    pet: Pet,
    onToggleLost: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onHealthClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Banner de Desaparecido
            if (pet.isLost) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Destructive)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Desaparecido",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Botão "Encontrado"
                    Surface(color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onToggleLost() }) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Marcar como Encontrado",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    // Foto
                    AsyncImage(
                        model = pet.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(InputBg),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Infos Principais
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    pet.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    pet.breed,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            // Botões de Ação (Ícones)
                            Row {
                                // BOTÃO DE SAÚDE
                                IconButton(
                                    onClick = onHealthClick, modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.MedicalServices,
                                        null,
                                        tint = Primary600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        null,
                                        tint = Destructive,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tags (Idade, Peso, Tag Ativa)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PetTag(icon = Icons.Default.CalendarToday, text = "${pet.age} anos")
                            PetTag(icon = Icons.Default.MonitorWeight, text = "${pet.weight}kg")
                        }

                        // Tag QR Code separada
                        if (pet.hasTag) {
                            Spacer(modifier = Modifier.height(8.dp))
                            PetTag(icon = null, text = "Tag Ativa", isHighlight = true)
                        }

                        // Botão "Perdi meu pet" se ele NÃO estiver perdido
                        if (!pet.isLost) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onToggleLost,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, Destructive.copy(alpha = 0.5f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Destructive),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Marcar como Desaparecido", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Extras (Alergias/Remédios)
                if (pet.allergies.isNotEmpty() || pet.medications.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp), color = BorderColor
                    )

                    if (pet.allergies.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                null,
                                tint = Destructive,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Alergias:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                pet.allergies.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                    if (pet.medications.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Medication,
                                null,
                                tint = Primary600,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Remédios:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                pet.medications.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PetTag(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    text: String,
    isHighlight: Boolean = false,
) {
    Surface(
        color = if (isHighlight) Primary100 else InputBg,
        shape = RoundedCornerShape(6.dp),
        border = if (isHighlight) androidx.compose.foundation.BorderStroke(
            1.dp, Primary600.copy(alpha = 0.3f)
        ) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(12.dp), tint = TextSecondary)
            } else if (isHighlight) {
                PawIcon(modifier = Modifier.size(12.dp), color = Primary600, filled = true)
            }
            Text(
                text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = if (isHighlight) Primary600 else TextSecondary
            )
        }
    }
}