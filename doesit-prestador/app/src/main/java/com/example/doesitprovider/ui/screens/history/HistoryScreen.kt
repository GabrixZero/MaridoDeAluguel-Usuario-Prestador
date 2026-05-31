package com.example.doesitprovider.ui.screens.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitprovider.data.model.ServiceRequestDTO
import com.example.doesitprovider.data.repository.ServiceRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.theme.AppColors
import com.example.doesitprovider.ui.theme.getCategoryLogo
import com.example.doesitprovider.ui.theme.formatDateBR
import com.example.doesitprovider.ui.theme.formatDateTimeBR
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val repo = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()
    var pedidos by remember { mutableStateOf<List<ServiceRequestDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filtro by remember { mutableStateOf("Todos") }

    // Filtros iguais ao app Usuário
    val filtros = listOf("Todos", "PENDING", "ACCEPTED", "IN_PROGRESS", "COMPLETED", "CANCELLED")
    val filtrosLabel = mapOf(
        "Todos"       to "Todos",
        "PENDING"     to "Pendentes",
        "ACCEPTED"    to "Agendados",
        "IN_PROGRESS" to "Em Andamento",
        "COMPLETED"   to "Concluídos",
        "CANCELLED"   to "Cancelados"
    )

    LaunchedEffect(Unit) {
        scope.launch {
            repo.getHistory().fold(onSuccess = { pedidos = it }, onFailure = {})
            isLoading = false
        }
    }

    val filtrados = if (filtro == "Todos") pedidos else pedidos.filter { it.status == filtro }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Serviços", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
        },
        bottomBar = {
            DoesItBottomNavBar(currentRoute = "history", onNavigate = { route ->
                when (route) {
                    "home"    -> onNavigateToHome()
                    "profile" -> onNavigateToProfile()
                }
            })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                Modifier.padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { f ->
                    val isSelected = f == filtro
                    Surface(
                        modifier = Modifier.clickable { filtro = f },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) AppColors.TextPrimary else AppColors.SurfaceVariant
                    ) {
                        Text(
                            filtrosLabel[f] ?: f,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else AppColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else if (filtrados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum serviço encontrado", color = AppColors.TextSecondary)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtrados) { req ->
                        ServiceCard(req) { onNavigateToDetail(req.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(req: ServiceRequestDTO, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).background(AppColors.SurfaceVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(getCategoryLogo(req.categoryName)),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(req.categoryName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                    // No historial do prestador mostra o nome do cliente
                    Text(req.userName, color = AppColors.TextSecondary, fontSize = 14.sp)
                }
                Text(
                    "R$ ${String.format(Locale.getDefault(), "%.2f", req.finalPrice ?: 0.0)}",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary
                )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AppColors.Border)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, Modifier.size(16.dp), tint = AppColors.TextSecondary)
                    Spacer(Modifier.width(8.dp))
                    Text(formatDateBR(req.requestedAt), color = AppColors.TextSecondary, fontSize = 14.sp)
                }
                StatusBadge(req.status)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, txt, label) = when (status) {
        "PENDING"     -> Triple(Color(0xFFFFF3E0), Color(0xFFFFA000), "PENDENTE")
        "ACCEPTED"    -> Triple(Color(0xFFE3F2FD), Color(0xFF1E88E5), "AGENDADO")
        "IN_PROGRESS" -> Triple(AppColors.SuccessLight, AppColors.Success, "EM ANDAMENTO")
        "COMPLETED"   -> Triple(AppColors.SuccessLight, AppColors.Success, "CONCLUÍDO")
        "CANCELLED"   -> Triple(AppColors.ErrorLight,   AppColors.Error,   "CANCELADO")
        else          -> Triple(AppColors.SurfaceVariant, AppColors.TextSecondary, status)
    }
    Surface(color = bg, shape = RoundedCornerShape(4.dp)) {
        Text(label, color = txt, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}
