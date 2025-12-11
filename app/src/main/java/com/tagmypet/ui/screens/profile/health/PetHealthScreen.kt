package com.tagmypet.ui.screens.profile.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tagmypet.data.model.Vaccine
import com.tagmypet.ui.components.ShadcnInput
import com.tagmypet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetHealthScreen(
    navController: NavController,
    petId: String,
    viewModel: PetHealthViewModel = hiltViewModel(),
) {
    val vaccines by viewModel.vaccines.collectAsState()
    val petName by viewModel.petName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estado para controlar o Dialog de Adicionar Vacina
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(petId) {
        viewModel.loadData(petId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Carteira de Vacinação", style = MaterialTheme.typography.titleMedium)
                        Text(
                            petName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary600,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Adicionar Vacina")
            }
        },
        containerColor = Background
    ) { innerPadding ->
        if (isLoading && vaccines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary600)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card de Resumo (Pode ser melhorado com lógica real de datas depois)
                item {
                    NextDoseCard()
                }

                item {
                    Text(
                        "Histórico de Vacinas",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                if (vaccines.isEmpty()) {
                    item {
                        Text(
                            "Nenhuma vacina registrada.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(vaccines) { vaccine ->
                        VaccineItem(
                            vaccine = vaccine,
                            onDelete = { viewModel.removeVaccine(vaccine.id) }
                        )
                    }
                }
            }
        }

        // Dialog para Adicionar Vacina
        if (showAddDialog) {
            AddVaccineDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, date, nextDate ->
                    viewModel.addVaccine(name, date, nextDate)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddVaccineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var dateApplied by remember { mutableStateOf("") }
    var nextDose by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Vacina") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnInput(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome da Vacina",
                    placeholder = "Ex: V10"
                )
                ShadcnInput(
                    value = dateApplied,
                    onValueChange = { dateApplied = it },
                    label = "Data de Aplicação",
                    placeholder = "DD/MM/AAAA"
                )
                ShadcnInput(
                    value = nextDose,
                    onValueChange = { nextDose = it },
                    label = "Próxima Dose (Opcional)",
                    placeholder = "DD/MM/AAAA"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && dateApplied.isNotBlank()) {
                        onConfirm(name, dateApplied, nextDose.ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary600)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = Surface
    )
}

@Composable
fun NextDoseCard() {
    // Nota: Em um app real, calcularíamos isso baseado na lista de vacinas
    Card(
        colors = CardDefaults.cardColors(containerColor = Primary100),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary600.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Primary600, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MedicalServices, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Próxima Dose",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    "Verifique as datas",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = Primary600
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Mantenha em dia",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary600
                    )
                }
            }
        }
    }
}

@Composable
fun VaccineItem(
    vaccine: Vaccine,
    onDelete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    vaccine.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Aplicado em: ${vaccine.dateApplied}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (!vaccine.nextDoseDate.isNullOrBlank()) {
                    Text(
                        "Próxima: ${vaccine.nextDoseDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary600
                    )
                }
            }

            // Ícone de Status
            if (vaccine.isApplied) {
                Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF22C55E))
            } else {
                Icon(Icons.Outlined.Warning, null, tint = TextSecondary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Botão Excluir
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover",
                    tint = Destructive.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}