package com.example.doesitprovider.ui.screens.avaliacoes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitprovider.data.model.RatingResponseDTO
import com.example.doesitprovider.data.network.SessionManager
import com.example.doesitprovider.data.repository.RatingRepository
import com.example.doesitprovider.data.repository.UserRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaliacoesScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val ratingRepo = remember { RatingRepository() }
    val userRepo   = remember { UserRepository() }

    var ratingsRecebidos by remember { mutableStateOf<List<RatingResponseDTO>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(true) }

    val rating      = SessionManager.rating
    val ratingCount = SessionManager.ratingCount

    LaunchedEffect(Unit) {
        userRepo.refreshProfile()
        ratingRepo.getMyReceivedRatings().fold(
            onSuccess = { lista -> ratingsRecebidos = lista },
            onFailure = {}
        )
        isLoading = false
    }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Avaliações", fontWeight = FontWeight.Bold) },
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
                currentRoute = "history",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reputação Card
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Sua Reputação", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(String.format("%.1f", rating), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(5) { i ->
                                val tint = if (i < rating.toInt()) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f)
                                Icon(Icons.Default.Star, null, tint = tint, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (ratingCount == 0) "Nenhuma avaliação ainda"
                            else "$ratingCount avaliações recebidas",
                            color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Text("Avaliações recebidas (${ratingsRecebidos.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            if (ratingsRecebidos.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhuma avaliação recebida ainda", color = Color.Gray)
                    }
                }
            }

            items(ratingsRecebidos) { r ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color.Gray)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.clienteName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(r.categoryName, color = Color.Gray, fontSize = 12.sp)
                            }
                            Row {
                                repeat(5) { i ->
                                    val tint = if (i < r.stars) Color(0xFFFFD700) else Color.LightGray
                                    Icon(Icons.Default.Star, null, tint = tint, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        if (!r.comment.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "\"${r.comment}\"",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp)).padding(12.dp).fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
