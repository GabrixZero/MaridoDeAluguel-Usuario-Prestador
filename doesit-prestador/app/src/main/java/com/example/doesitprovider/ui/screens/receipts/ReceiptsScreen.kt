package com.example.doesitprovider.ui.screens.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.components.DoesItButton
import com.example.doesitprovider.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recebimentos", fontWeight = FontWeight.Bold) },
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
                currentRoute = "receipts",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
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
                .padding(24.dp)
        ) {
            // Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Saldo disponível para saque", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("R$ 1.250,00", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Valor acumulado dos serviços concluídos.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            DoesItButton(
                text = "Solicitar saque",
                onClick = { /* TODO */ },
                showArrow = false
            )
            
            Spacer(Modifier.height(16.dp))
            Text(
                "O valor será transferido para uma das contas bancárias cadastradas.",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(32.dp))
            Text("Minhas contas bancárias", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            
            BankAccountItem("Banco do Brasil", "Ag. 1234 • C/C 98765-4 • Titular: João Silva", Icons.Default.AccountBalance)
            Spacer(Modifier.height(12.dp))
            BankAccountItem("Nubank", "Banco 260 • C/P 12345-6 • Titular: João Silva", Icons.Default.AccountBalance)
            
            Spacer(Modifier.height(24.dp))
            
            // Dashed Register New Account Button
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color.LightGray
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    Text("Cadastrar nova conta bancária", color = Color.Gray, fontSize = 16.sp)
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun BankAccountItem(name: String, details: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.Black)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(details, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
        }
    }
}
