package com.example.doesitprovider.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val notifications = listOf(
        NotificationData("Novo orçamento recebido", "Você recebeu um pedido de orçamento para instalação de torneira.", "14:30", Icons.Default.Description, Color(0xFFFFEBEE), AppColors.Primary, true),
        NotificationData("Serviço confirmado", "O cliente confirmou o serviço de reparo elétrico para hoje às 18:00.", "09:15", Icons.Default.CheckCircle, Color(0xFFE8F5E9), AppColors.Success, true),
        NotificationData("Pagamento recebido", "Você recebeu R$ 180,00 pelo serviço de instalação de chuveiro.", "Ontem", Icons.Default.CreditCard, Color(0xFFE8F5E9), AppColors.Success, false),
        NotificationData("Serviço cancelado", "O cliente cancelou o serviço de limpeza agendado para 20/03.", "10 Mar", Icons.Default.Cancel, Color(0xFFFFEBEE), Color.Red, false),
        NotificationData("Novo serviço agendado", "Instalação de suporte de TV agendada para 25 Mar às 09:00.", "05 Mar", Icons.Default.Event, Color(0xFFFFEBEE), AppColors.Primary, false)
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notificações", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            DoesItBottomNavBar(
                currentRoute = "notifications",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(notifications) { item ->
                NotificationItem(item)
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

data class NotificationData(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color,
    val isNew: Boolean
)

@Composable
fun NotificationItem(item: NotificationData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(item.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = item.iconColor, modifier = Modifier.size(24.dp))
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (item.isNew) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.description,
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = item.time,
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}
