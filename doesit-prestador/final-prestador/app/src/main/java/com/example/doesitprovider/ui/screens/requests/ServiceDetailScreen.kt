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
    onDone: () -> Unit   // navega para Home após cancelamento ou conclusão
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

    // Avaliação
    var selectedStars by remember { mutableIntStateOf(0) }
    var ratingComment by remember { mutableStateOf("") }
    var alreadyRated by remember { mutableStateOf(false) }

    fun loadRequest() {
        scope.launch {
            isLoading = true
            repo.getById(requestId).fold(onSuccess = { req = it }, onFailure = {
                errorMessage = "Erro ao carregar detalhes"; isError = true
            })
            isLoading = false
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

                        // ── Ações por status ─────────────────────────────────
                        when (service.status.uppercase().trim()) {
                            "PENDING", "PENDENTE" -> {
                                // Aceitar ou recusar
                                DoesItButton(
                                    text = "Aceitar a Solicitação",
                                    onClick = {
                                        scope.launch {
                                            isActionLoading = true
                                            repo.accept(service.id).fold(
                                                onSuccess = {
                                                    successMessage = "Solicitação aceita!"; isSuccess = true
                                                    loadRequest()
                                                },
                                                onFailure = { errorMessage = it.message ?: "Erro ao aceitar"; isError = true }
                                            )
                                            isActionLoading = false
                                        }
                                    },
                                    isLoading = isActionLoading,
                                    showArrow = false
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isActionLoading = true
                                            repo.refuse(service.id).fold(
                                                onSuccess = {
                                                    successMessage = "Solicitação recusada."; isSuccess = true
                                                    delay(1500); onDone()
                                                },
                                                onFailure = { errorMessage = it.message ?: "Erro ao recusar"; isError = true }
                                            )
                                            isActionLoading = false
                                        }
                                    },
                                    enabled = !isActionLoading,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Error),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                                ) {
                                    Text("Recusar a Solicitação", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            "ACCEPTED", "AGENDADO" -> {
                                // Iniciar serviço (com regra de 10 min para SCHEDULED)
                                DoesItButton(
                                    text = "Iniciar Serviço",
                                    onClick = {
                                        scope.launch {
                                            isActionLoading = true
                                            repo.start(service.id).fold(
                                                onSuccess = {
                                                    successMessage = "Serviço iniciado!"; isSuccess = true
                                                    loadRequest()
                                                },
                                                onFailure = { errorMessage = it.message ?: "Erro ao iniciar"; isError = true }
                                            )
                                            isActionLoading = false
                                        }
                                    },
                                    isLoading = isActionLoading,
                                    showArrow = false,
                                    containerColor = AppColors.Success
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isActionLoading = true
                                            repo.cancel(service.id).fold(
                                                onSuccess = { delay(800); onDone() },
                                                onFailure = { errorMessage = it.message ?: "Erro ao cancelar"; isError = true }
                                            )
                                            isActionLoading = false
                                        }
                                    },
                                    enabled = !isActionLoading,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Error),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error)
                                ) {
                                    Text("Cancelar Serviço", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                if (service.type == "SCHEDULED") {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Para serviços agendados, o botão 'Iniciar' fica disponível a partir de 10 minutos antes do horário marcado.",
                                        fontSize = 12.sp, color = AppColors.TextSecondary, lineHeight = 18.sp
                                    )
                                }
                            }

                            "IN_PROGRESS", "EM ANDAMENTO" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = AppColors.SuccessLight),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PlayCircle, null, tint = AppColors.Success, modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Serviço em andamento", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Success)
                                            Text("Aguardando confirmação de finalização pelo cliente.", fontSize = 14.sp, color = AppColors.TextSecondary, lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }

                            "COMPLETED", "CONCLUIDO", "CONCLUÍDO" -> {
                                // Avaliação do cliente (apenas uma vez)
                                if (alreadyRated) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = AppColors.SuccessLight),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, null, tint = AppColors.Success)
                                            Spacer(Modifier.width(12.dp))
                                            Text("Avaliação enviada! Obrigado.", color = AppColors.Success, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Text("Avaliar Cliente", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                                    Spacer(Modifier.height(16.dp))
                                    Text("Como foi atender ${service.userName ?: "o cliente"}?", color = AppColors.TextSecondary, fontSize = 14.sp)
                                    Spacer(Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        for (i in 1..5) {
                                            val scale by animateFloatAsState(
                                                if (selectedStars >= i) 1.2f else 1f,
                                                animationSpec = spring(Spring.DampingRatioMediumBouncy), label = "star$i"
                                            )
                                            Icon(
                                                imageVector = if (selectedStars >= i) Icons.Default.Star else Icons.Outlined.StarOutline,
                                                contentDescription = null,
                                                tint = if (selectedStars >= i) Color(0xFFFFC107) else AppColors.TextDisabled,
                                                modifier = Modifier.size(40.dp).scale(scale)
                                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { selectedStars = i }
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(20.dp))
                                    OutlinedTextField(
                                        value = ratingComment, onValueChange = { ratingComment = it },
                                        placeholder = { Text("Comentário sobre o cliente...", color = AppColors.TextDisabled) },
                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = AppColors.InputBackground, unfocusedContainerColor = AppColors.InputBackground,
                                            focusedBorderColor = AppColors.Primary, unfocusedBorderColor = Color.Transparent
                                        )
                                    )
                                    Spacer(Modifier.height(20.dp))
                                    val canRate = selectedStars > 0 && !isActionLoading
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isActionLoading = true
                                                repo.rateUser(service.id, selectedStars, ratingComment.trim()).fold(
                                                    onSuccess = { alreadyRated = true; successMessage = "Avaliação enviada!"; isSuccess = true },
                                                    onFailure = {
                                                        if ((it.message ?: "").contains("já avaliou")) alreadyRated = true
                                                        else { errorMessage = it.message ?: "Erro ao avaliar"; isError = true }
                                                    }
                                                )
                                                isActionLoading = false
                                            }
                                        },
                                        enabled = canRate,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (canRate) AppColors.Primary else AppColors.ButtonDisabled,
                                            contentColor = if (canRate) Color.White else AppColors.TextButtonDisabled
                                        )
                                    ) {
                                        if (isActionLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                        else Text("Enviar Avaliação", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }

                            "CANCELLED", "CANCELADO" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = AppColors.ErrorLight),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Cancel, null, tint = AppColors.Error)
                                        Spacer(Modifier.width(12.dp))
                                        Text("Este serviço foi cancelado.", color = AppColors.Error, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            else -> {
                                // Caso caia em algum status não mapeado ou nulo
                                Text("Status: ${service.status}", color = AppColors.TextSecondary)
                            }
                        }
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }

            // Banners de feedback
            AnimatedVisibility(
                visible = isError && errorMessage.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter).zIndex(10f)
            ) { ErrorBanner(errorMessage) }

            AnimatedVisibility(
                visible = isSuccess && successMessage.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter).zIndex(10f)
            ) { SuccessBanner(successMessage) }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, fontSize = 14.sp, color = AppColors.TextSecondary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary,
            textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
    }
}
