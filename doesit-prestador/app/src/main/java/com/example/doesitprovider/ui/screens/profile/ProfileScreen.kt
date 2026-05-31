package com.example.doesitprovider.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.doesitprovider.data.network.SessionManager
import com.example.doesitprovider.data.repository.UserRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.components.DoesItButton
import com.example.doesitprovider.ui.components.DoesItTextField
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repo = remember { UserRepository() }
    var nome by remember { mutableStateOf(SessionManager.userName) }
    var celular by remember { mutableStateOf(SessionManager.userPhone) }
    var email by remember { mutableStateOf(SessionManager.userEmail) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meu Perfil", fontWeight = FontWeight.Bold) },
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
                currentRoute = "profile",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "history" -> onNavigateToHistory()
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with Edit Button
            Box {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.fillMaxSize(), tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary)
                        .align(Alignment.BottomEnd)
                        .background(AppColors.Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            Column(Modifier.fillMaxWidth()) {
                Text("Informações Pessoais", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(20.dp))
                DoesItTextField(value = nome, onValueChange = { nome = it }, label = "Nome completo", placeholder = "Seu nome")
                Spacer(Modifier.height(20.dp))
                DoesItTextField(value = "***.456.789-**", onValueChange = {}, label = "CPF (Inalterável)", placeholder = "000.000.000-00", readOnly = true)
                Spacer(Modifier.height(20.dp))
                DoesItTextField(value = "15/08/1990", onValueChange = {}, label = "Data de Nascimento (Inalterável)", placeholder = "dd/mm/aaaa", readOnly = true)
                
                Spacer(Modifier.height(32.dp))
                
                Text("Contato", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(20.dp))
                DoesItTextField(value = email, onValueChange = { email = it }, label = "E-mail", placeholder = "seuemail@exemplo.com")
                Spacer(Modifier.height(20.dp))
                DoesItTextField(value = celular, onValueChange = { celular = it }, label = "Celular", placeholder = "(00) 00000-0000")
            }
            
            Spacer(Modifier.height(40.dp))
            
            DoesItButton(
                text = "Salvar Alterações",
                onClick = {
                    scope.launch {
                        isSaving = true
                        repo.updateProfile(mapOf("name" to nome, "phone" to celular))
                        isSaving = false
                    }
                },
                isLoading = isSaving,
                showArrow = false
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
