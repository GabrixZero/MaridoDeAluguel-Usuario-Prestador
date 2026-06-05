package com.example.doesitprovider.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.doesitprovider.data.model.NotificationDTO
import com.example.doesitprovider.data.repository.ServiceRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.theme.AppColors
import com.example.doesitprovider.ui.theme.formatDateTimeBR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val repo  = remember { ServiceRepository() }
    var notifications by remember { mutableStateOf<List<NotificationDTO>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var errorMsg      by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repo.getNotifications().fold(
            onSuccess = { notifications = it },
            onFailure = { errorMsg = it.message ?: "Erro ao carregar notificações" }
        )
        isLoading = false
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notificações", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            DoesItBottomNavBar(currentRoute = "notifications", onNavigate = { route ->
                when (route) {
                    "home"    -> onNavigateToHome()
                    "profile" -> onNavigateToProfile()
                }
            })
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }
            errorMsg.isNotEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WifiOff, null,
                            tint = AppColors.TextDisabled, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(errorMsg, color = AppColors.TextSecondary, fontSize = 14.sp)
                    }
                }
            }
            notifications.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsNone, null,
                            tint = AppColors.TextDisabled, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Nenhuma notificação", color = AppColors.TextSecondary, fontSize = 16.sp)
                    }
                }
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(notifications) { item ->
                        NotificationItem(item)
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(item: NotificationDTO) {
    Row(
        Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(AppColors.PrimaryLight),
            Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, null,
                tint = AppColors.Primary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = AppColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(item.message, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(formatDateTimeBR(item.timestamp), color = Color.LightGray, fontSize = 12.sp)
        }
    }
}
