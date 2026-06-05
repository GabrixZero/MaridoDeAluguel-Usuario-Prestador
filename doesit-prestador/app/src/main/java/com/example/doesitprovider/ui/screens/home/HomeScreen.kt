package com.example.doesitprovider.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitprovider.data.network.SessionManager
import com.example.doesitprovider.data.repository.ServiceRepository
import com.example.doesitprovider.data.repository.UserRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.components.QuickAccessCard
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReceipts: () -> Unit,
    onNavigateToSpecialties: () -> Unit,
    onNavigateToAddress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToServiceDetail: (Long) -> Unit
) {
    val repo     = remember { ServiceRepository() }
    val userRepo = remember { UserRepository() }
    val scope    = rememberCoroutineScope()
    val isOnline = SessionManager.isOnline
    val userName = SessionManager.userName.split(" ").firstOrNull() ?: "Prestador"

    LaunchedEffect(Unit) { userRepo.getCurrentUser() }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            DoesItBottomNavBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "history" -> onNavigateToHistory()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(AppColors.Primary, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                ) {
                    // Avatar padrão — Person icon em fundo com alpha
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.padding(10.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Bem vindo,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text("$userName!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onNavigateToNotifications) {
                                Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Card de disponibilidade
            Card(
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Disponível para receber serviços?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Fique online para receber solicitações de clientes próximos a você.",
                                color = AppColors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp
                            )
                        }
                        Box(
                            Modifier.size(50.dp).clip(CircleShape).background(AppColors.Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RssFeed, null, tint = AppColors.Primary)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { scope.launch { repo.setOnline(!isOnline) } },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOnline) AppColors.Success else AppColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isOnline) "Ficar Offline" else "Ficar Online",
                                fontWeight = FontWeight.Bold, fontSize = 16.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.PowerSettingsNew, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("Acesso rápido", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                color = AppColors.TextPrimary, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(16.dp))

            Column(Modifier.padding(horizontal = 24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessCard("Pagamentos", Icons.Default.CreditCard, onNavigateToReceipts, Modifier.weight(1f))
                    QuickAccessCard("Serviços",   Icons.Default.Assignment,  onNavigateToHistory,  Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickAccessCard("Especialidades", Icons.Default.MilitaryTech, onNavigateToSpecialties, Modifier.weight(1f))
                    QuickAccessCard("Meu Endereço",   Icons.Default.LocationOn,  onNavigateToAddress,      Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
