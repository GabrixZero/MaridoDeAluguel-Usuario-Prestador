package com.example.doesitprovider.ui.screens.address

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.doesitprovider.ui.components.DoesItButton
import com.example.doesitprovider.ui.components.DoesItTextField
import com.example.doesitprovider.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var cep by remember { mutableStateOf("04538-133") }
    var rua by remember { mutableStateOf("Av. Brigadeiro Faria Lima") }
    var numero by remember { mutableStateOf("3477") }
    var bairro by remember { mutableStateOf("Itaim Bibi") }
    var cidade by remember { mutableStateOf("São Paulo") }
    var estado by remember { mutableStateOf("SP") }
    var titulo by remember { mutableStateOf("Trabalho") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar endereço", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            DoesItTextField(value = cep, onValueChange = { cep = it }, label = "CEP", placeholder = "00000-000")
            Spacer(Modifier.height(24.dp))
            DoesItTextField(value = rua, onValueChange = { rua = it }, label = "Rua", placeholder = "Nome da rua")
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth()) {
                DoesItTextField(value = numero, onValueChange = { numero = it }, label = "Número", placeholder = "000", modifier = Modifier.weight(1f))
                Spacer(Modifier.width(16.dp))
                DoesItTextField(value = bairro, onValueChange = { bairro = it }, label = "Bairro", placeholder = "Nome do bairro", modifier = Modifier.weight(2f))
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth()) {
                DoesItTextField(value = cidade, onValueChange = { cidade = it }, label = "Cidade", placeholder = "Sua cidade", modifier = Modifier.weight(2f))
                Spacer(Modifier.width(16.dp))
                DoesItTextField(value = estado, onValueChange = { if (it.length <= 2) estado = it.uppercase() }, label = "Estado", placeholder = "UF", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            DoesItTextField(value = titulo, onValueChange = { titulo = it }, label = "Título do endereço (Opcional)", placeholder = "Ex: Casa, Trabalho")
            
            Spacer(Modifier.height(32.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Definir como endereço principal", modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppColors.Primary)
                    )
                }
            }
            
            Spacer(Modifier.height(40.dp))
            
            val isFormFilled = cep.isNotEmpty() && rua.isNotEmpty() && numero.isNotEmpty() && 
                               bairro.isNotEmpty() && cidade.isNotEmpty() && estado.isNotEmpty()
            
            DoesItButton(
                text = "Salvar Alterações",
                onClick = { /* TODO */ },
                showArrow = false,
                enabled = isFormFilled
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
