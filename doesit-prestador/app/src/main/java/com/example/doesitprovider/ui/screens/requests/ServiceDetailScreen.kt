package com.example.doesitprovider.ui.screens.requests

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.doesitprovider.data.model.ServiceRequestDTO
import com.example.doesitprovider.data.repository.ServiceRepository
import com.example.doesitprovider.ui.components.DoesItButton
import com.example.doesitprovider.ui.components.ErrorBanner
import com.example.doesitprovider.ui.components.SuccessBanner
import com.example.doesitprovider.ui.screens.history.StatusBadge
import com.example.doesitprovider.ui.theme.AppColors
import com.example.doesitprovider.ui.theme.formatDateTimeBR
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    requestId: Long,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val repo = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()

    var req by remember { mutableStateOf<ServiceRequestDTO?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isActionLoading by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    fun loadRequest() {
        scope.launch {
            isLoading = true
            repo.getById(requestId).fold(
                onSuccess = { req = it },
                onFailure = { errorMessage = "Erro ao carregar detalhes"; isError = true }
            )
            isLoading = false
        }
    }

    // Função centralizada para atualizar status
    fun handleStatusUpdate(newStatusId: Int, message: String) {
        scope.launch {
            isActionLoading = true
            repo.updateStatus(requestId, newStatusId).fold(
                onSuccess = {
                    successMessage = message
                    isSuccess = true
                    if (newStatusId == 4) { // Se for cancelamento, volta para a lista
                        delay(1500)
                        onDone()
                    } else {
                        loadRequest() // Recarrega para atualizar a UI reativamente
                    }
                },
                onFailure = { 
                    errorMessage = it.message ?: "Erro ao atualizar status"
                    isError = true 
                }
            )
            isActionLoading = false
        }
    }

    LaunchedEffect(requestId) { loadRequest() }
    LaunchedEffect(isError)   { if (isError)   { delay(5000); isError   = false } }
    LaunchedEffect(isSuccess) { if (isSuccess) { delay(5000); isSuccess = false } }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalhes do Serviço", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when {
                isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
                req == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Serviço não encontrado", color = AppColors.TextSecondary)
                }
                else -> {
                    val service = req!!
                    Column(
                        Modifier.fillMaxSize().padding(padding)
                            .verticalScroll(rememberScrollState()).padding(24.dp)
                    ) {
                        // ── Cabeçalho ────────────────────────────────────────
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(Modifier.size(60.dp), CircleShape, AppColors.SurfaceVariant) {
                                        Icon(Icons.Default.Person, null, Modifier.padding(14.dp), tint = AppColors.TextDisabled)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(service.categoryName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                                        Text(service.userName ?: "", color = AppColors.TextSecondary, fontSize = 14.sp)
                                    }
                                    StatusBadge(service.status)
                                }
                                Spacer(Modifier.height(20.dp))
                                HorizontalDivider(color = AppColors.Border)
                                Spacer(Modifier.height(20.dp))
                                InfoRow("Data e Hora", formatDateTimeBR(service.scheduledAt ?: service.requestedAt))
                                InfoRow("Valor", "R$ ${String.format(Locale.getDefault(), "%.2f", service.finalPrice ?: 0.0)}")
                                InfoRow("Modo", if (service.type == "SCHEDULED") "Agendado" else "Agora")
                                if (!service.address.isNullOrBlank())
                                    InfoRow("Endereço", service.address)
                                if (!service.description.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Comentário", fontSize = 14.sp, color = AppColors.TextSecondary)
                                    Spacer(Modifier.height(4.dp))
                                    Text(service.description ?: "", fontSize = 15.sp, color = AppColors.TextPrimary, lineHeight = 22.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // ── Ações Reativas ao status_id ──────────────────────
                        // 1 "PENDENTE", 2 "AGENDADO", 3 "CONCLUIDO", 4 "CANCELADO", 5 "EM ANDAMENTO"
                        val statusId = service.statusId ?: 0

                        when (statusId) {
                            1 -> { // PENDENTE: Pode aceitar (2) ou cancelar (4)
                                DoesItButton(
                                    text = "Aceitar a Solicitação",
                                    onClick = { handleStatusUpdate(2, "Solicitação aceita!") },
                                    isLoading = isActionLoading,
                                    showArrow = false
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { handleStatusUpdate(4, "Solicitação recusada.") },
                                    enabled = !isActionLoading,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Error),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                                ) {
                                    Text("Recusar Solicitação", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            2 -> { // AGENDADO: Pode iniciar (5) ou cancelar (4)
                                DoesItButton(
                                    text = "Iniciar Serviço",
                                    onClick = { handleStatusUpdate(5, "Serviço iniciado!") },
                                    isLoading = isActionLoading,
                                    showArrow = false,
                                    containerColor = AppColors.Success
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { handleStatusUpdate(4, "Agendamento cancelado.") },
                                    enabled = !isActionLoading,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Error),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                                ) {
                                    Text("Cancelar Agendamento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            5 -> { // EM ANDAMENTO: Pode concluir (3)
                                DoesItButton(
                                    text = "Concluir Serviço",
                                    onClick = { handleStatusUpdate(3, "Serviço concluído com sucesso!") },
                                    isLoading = isActionLoading,
                                    showArrow = false,
                                    containerColor = AppColors.Primary
                                )
                                Spacer(Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = AppColors.SuccessLight),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PlayCircle, null, tint = AppColors.Success, modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Serviço em andamento", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Success)
                                            Text("Clique em concluir ao terminar o atendimento.", fontSize = 14.sp, color = AppColors.TextSecondary)
                                        }
                                    }
                                }
                            }

                            3 -> { // CONCLUIDO
                                Card(colors = CardDefaults.cardColors(containerColor = AppColors.SuccessLight), shape = RoundedCornerShape(12.dp)) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = AppColors.Success)
                                        Spacer(Modifier.width(12.dp))
                                        Text("Este serviço foi concluído.", color = AppColors.Success, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            4 -> { // CANCELADO
                                Card(colors = CardDefaults.cardColors(containerColor = AppColors.ErrorLight), shape = RoundedCornerShape(12.dp)) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Cancel, null, tint = AppColors.Error)
                                        Spacer(Modifier.width(12.dp))
                                        Text("Este serviço foi cancelado.", color = AppColors.Error, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            else -> {
                                Text("Status: ${service.status}", color = AppColors.TextSecondary)
                            }
                        }
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }

            // Feedback
            AnimatedVisibility(visible = isError && errorMessage.isNotEmpty(), enter = slideInVertically { -it } + fadeIn(), exit  = slideOutVertically { -it } + fadeOut(), modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter).zIndex(10f)) { ErrorBanner(errorMessage) }
            AnimatedVisibility(visible = isSuccess && successMessage.isNotEmpty(), enter = slideInVertically { -it } + fadeIn(), exit  = slideOutVertically { -it } + fadeOut(), modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter).zIndex(10f)) { SuccessBanner(successMessage) }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, fontSize = 14.sp, color = AppColors.TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
    }
}
