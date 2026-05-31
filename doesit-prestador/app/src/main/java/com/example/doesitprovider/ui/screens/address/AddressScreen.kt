package com.example.doesitprovider.ui.screens.address

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meu endereço", fontWeight = FontWeight.Bold) },
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
                currentRoute = "address",
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
                .padding(24.dp)
        ) {
            // Address Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Home, null, tint = Color.Black)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Casa", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Rua Laura Alves Barbosa da Silva, 44, Osasco - SP",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, tint = Color.LightGray)
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Warning Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, null, tint = AppColors.Primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Este endereço é utilizado como base para você receber solicitações de serviços e agendamentos próximos. Caso você mude de localização, é necessário editar o seu endereço fixo para continuar recebendo serviços corretamente.",
                        color = AppColors.Primary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
