package com.example.doesitprovider.ui.screens.address

import androidx.compose.animation.*
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
import com.example.doesitprovider.data.model.AddressDTO
import com.example.doesitprovider.data.repository.ServiceRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,             // passa o id do endereço para a tela de edição
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val repo  = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()

    var address   by remember { mutableStateOf<AddressDTO?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error     by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repo.getAddresses().fold(
            onSuccess = { list -> address = list.firstOrNull(); isLoading = false },
            onFailure = { error = it.message ?: "Erro ao carregar endereço"; isLoading = false }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meu endereço", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            DoesItBottomNavBar(currentRoute = "address", onNavigate = { route ->
                when (route) {
                    "home"    -> onNavigateToHome()
                    "profile" -> onNavigateToProfile()
                }
            })
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp)
        ) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                }
                error.isNotEmpty() -> {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.ErrorLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = AppColors.Error)
                            Spacer(Modifier.width(12.dp))
                            Text(error, color = AppColors.Error, fontSize = 14.sp)
                        }
                    }
                }
                address == null -> {
                    // Nenhum endereço cadastrado — botão para adicionar
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocationOff, null, tint = AppColors.TextDisabled, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Nenhum endereço cadastrado", color = AppColors.TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { onEdit(0L) },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Adicionar endereço", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {
                    val addr = address!!
                    // Card com o endereço principal
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).background(Color.White, RoundedCornerShape(12.dp)),
                                Alignment.Center
                            ) {
                                Icon(Icons.Default.Home, null, tint = Color.Black)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    addr.tag?.ifBlank { null } ?: "Principal",
                                    fontWeight = FontWeight.Bold, fontSize = 18.sp
                                )
                                val parts = listOfNotNull(
                                    addr.street?.ifBlank { null },
                                    addr.number?.ifBlank { null },
                                    addr.neighborhood?.ifBlank { null },
                                    addr.city?.ifBlank { null }?.let { c ->
                                        addr.state?.ifBlank { null }?.let { s -> "$c - $s" } ?: c
                                    }
                                )
                                Text(
                                    if (parts.isNotEmpty()) parts.joinToString(", ")
                                    else addr.formatted ?: addr.cep ?: "—",
                                    color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp
                                )
                            }
                            IconButton(onClick = { onEdit(addr.id) }) {
                                Icon(Icons.Default.Edit, null, tint = Color.LightGray)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Info box
                    Box(
                        Modifier.fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, null, tint = AppColors.Primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Este endereço é utilizado como base para você receber solicitações de serviços e agendamentos próximos. Caso mude de localização, edite seu endereço fixo para continuar recebendo serviços corretamente.",
                                color = AppColors.Primary, fontSize = 14.sp, lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
