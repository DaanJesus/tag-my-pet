package com.tagmypet.ui.screens.scan

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.*
import androidx.compose.runtime.* // Importante para by, collectAsState
import androidx.compose.runtime.getValue // Garante que o delegate funcione
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tagmypet.data.model.Pet
import com.tagmypet.ui.components.ButtonVariant
import com.tagmypet.ui.components.ShadcnButton
import com.tagmypet.ui.components.icons.PawIcon
import com.tagmypet.ui.navigation.Screen
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTagScreen(
    navController: NavController,
    viewModel: ScanViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
) {
    // Coleta os estados do ViewModel
    val isScanning by viewModel.isScanning.collectAsState()
    val foundPet by viewModel.foundPet.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()
    val scanMessage by viewModel.scanMessage.collectAsState()
    val navigateToError by viewModel.navigateToError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted -> hasCameraPermission = granted })

    LaunchedEffect(navigateToError) {
        if (navigateToError) {
            navController.navigate(Screen.ScanError.route)
            viewModel.onNavigationHandled()
            viewModel.resetScan()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Exibe snackbar quando scanMessage mudar
    LaunchedEffect(scanMessage) {
        scanMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(
                    "Escanear", style = MaterialTheme.typography.titleLarge, color = TextPrimary
                )
            },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Menu", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (foundPet != null) Arrangement.Top else Arrangement.Center
        ) {
            foundPet?.let { pet ->
                FoundPetResultResponsive(pet = pet,
                    isOwner = isOwner,
                    onReset = { viewModel.resetScan() },
                    onChatClick = {
                        navController.navigate(Screen.Chat.createRoute(pet.ownerId))
                    })
            } ?: run {
                if (hasCameraPermission) {
                    if (isScanning) {
                        ScannerInterface(onBarcodeDetected = { code ->
                            viewModel.onBarcodeDetected(code)
                        })
                    } else {
                        CircularProgressIndicator(color = Primary600)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Precisamos da câmera para escanear a tag.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ShadcnButton(text = "Permitir Câmera",
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) })
                    }
                }
            }
        }
    }
}

// ... (Restante dos componentes ScannerInterface, ScannerAnimation, FoundPetResultResponsive mantidos iguais) ...
@Composable
fun ScannerInterface(onBarcodeDetected: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Escanear Tag", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Text(
            "Aponte para o QR Code",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CameraPreview(onBarcodeScanned = onBarcodeDetected)
                ScannerAnimation()
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(150.dp)
                )
            }
        }
    }
}

@Composable
fun ScannerAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        ), label = "offset"
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .offset(y = maxHeight * offsetY)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent, Primary600, Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun FoundPetResultResponsive(
    pet: Pet,
    isOwner: Boolean,
    onReset: () -> Unit,
    onChatClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isOwner) {
            Surface(
                color = Color(0xFF22C55E),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    ); Spacer(modifier = Modifier.width(12.dp)); Column {
                    Text(
                        "Você encontrou seu pet!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    ); Text(
                    "Status atualizado para 'Em Casa'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                }
                }
            }
        } else if (pet.isLost) {
            Surface(
                color = Destructive,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    ); Spacer(modifier = Modifier.width(12.dp)); Column {
                    Text(
                        "Este pet está desaparecido!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    ); Text(
                    "Entre em contato com o tutor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                }
                }
            }
        } else {
            Surface(
                color = Primary600,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PawIcon(
                        color = Color.White, filled = true, modifier = Modifier.size(20.dp)
                    ); Spacer(modifier = Modifier.width(12.dp)); Text(
                    "Perfil do Pet",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                }
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                AsyncImage(
                    model = pet.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.33f)
                        .background(InputBg),
                    contentScale = ContentScale.Crop
                ); Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    pet.name, style = MaterialTheme.typography.headlineLarge, color = TextPrimary
                ); Text(
                "${pet.breed} • ${pet.age} anos",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            ); Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = BorderColor); Spacer(
                modifier = Modifier.height(24.dp)
            ); Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tutor", style = MaterialTheme.typography.labelMedium, color = TextSecondary
                    ); Text(
                    if (isOwner) "Você" else "Maria Silva", // Idealmente buscar nome do dono
                    style = MaterialTheme.typography.titleMedium, color = TextPrimary
                )
                }; Column(
                modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Peso", style = MaterialTheme.typography.labelMedium, color = TextSecondary
                ); Text(
                "${pet.weight} kg",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            }
            }
            }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            if (!isOwner && pet.isLost) {
                ShadcnButton(
                    text = "Falar com Tutor",
                    onClick = onChatClick,
                    variant = ButtonVariant.PRIMARY,
                    icon = { Icon(Icons.Outlined.Message, null, Modifier.size(18.dp)) },
                    modifier = Modifier.height(56.dp)
                )
            }; ShadcnButton(
            text = "Escanear outro",
            onClick = onReset,
            variant = ButtonVariant.OUTLINE,
            modifier = Modifier.height(56.dp)
        )
        }
    }
}