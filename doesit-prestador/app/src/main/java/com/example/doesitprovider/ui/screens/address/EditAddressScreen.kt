package com.example.doesitprovider.ui.screens.address

import androidx.compose.animation.*
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
import com.example.doesitprovider.data.repository.ServiceRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.components.DoesItButton
import com.example.doesitprovider.ui.components.DoesItTextField
import com.example.doesitprovider.ui.components.ErrorBanner
import com.example.doesitprovider.ui.components.SuccessBanner
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressScreen(
    addressId: Long,                    // 0L = criar novo; >0 = editar existente
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val repo  = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()

    // Campos do formulário
    var cep    by remember { mutableStateOf("") }
    var rua    by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }

    var isLoadingData by remember { mutableStateOf(addressId > 0L) }
    var isSaving      by remember { mutableStateOf(false) }
    var errorMsg      by remember { mutableStateOf("") }
    var isError       by remember { mutableStateOf(false) }
    var successMsg    by remember { mutableStateOf("") }
    var isSuccess     by remember { mutableStateOf(false) }

    // Pré-carrega os dados do endereço se estiver editando um existente
    LaunchedEffect(addressId) {
        if (addressId > 0L) {
            repo.getAddresses().fold(
                onSuccess = { list ->
                    list.firstOrNull { it.id == addressId }?.let { addr ->
                        cep    = addr.cep    ?: ""
                        rua    = addr.street ?: ""
                        numero = addr.number ?: ""
                        bairro = addr.neighborhood ?: ""
                        cidade = addr.city   ?: ""
                        estado = addr.state  ?: ""
                        titulo = addr.tag    ?: ""
                    }
                },
                onFailure = {}
            )
            isLoadingData = false
        }
    }

    LaunchedEffect(isError)   { if (isError)   { delay(5000); isError   = false } }
    LaunchedEffect(isSuccess) { if (isSuccess) { delay(5000); isSuccess = false } }

    val isFormFilled = cep.isNotEmpty() && rua.isNotEmpty() && numero.isNotEmpty() &&
                       bairro.isNotEmpty() && cidade.isNotEmpty() && estado.isNotEmpty()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (addressId > 0L) "Editar endereço" else "Novo endereço", fontWeight = FontWeight.Bold) },
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
        Box(Modifier.fillMaxSize()) {
            if (isLoadingData) {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(padding)
                        .verticalScroll(rememberScrollState()).padding(24.dp)
                ) {
                    DoesItTextField(cep, { if (it.all { c -> c.isDigit() } && it.length <= 8) cep = it },
                        "CEP", "00000-000")
                    Spacer(Modifier.height(16.dp))

                    DoesItTextField(rua, { rua = it }, "Rua", "Nome da rua")
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth()) {
                        DoesItTextField(numero, { if (it.all { c -> c.isDigit() }) numero = it },
                            "Número", "000", modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(16.dp))
                        DoesItTextField(bairro, { bairro = it },
                            "Bairro", "Nome do bairro", modifier = Modifier.weight(2f))
                    }
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth()) {
                        DoesItTextField(cidade, { if (it.all { c -> c.isLetter() || c.isWhitespace() }) cidade = it },
                            "Cidade", "Sua cidade", modifier = Modifier.weight(2f))
                        Spacer(Modifier.width(16.dp))
                        DoesItTextField(estado, { if (it.all { c -> c.isLetter() }) estado = it.uppercase().take(2) },
                            "Estado", "UF", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))

                    DoesItTextField(titulo, { titulo = it },
                        "Título do endereço (Opcional)", "Ex: Casa, Trabalho")

                    Spacer(Modifier.height(32.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Definir como endereço principal",
                                Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Switch(checked = true, onCheckedChange = {},
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppColors.Primary))
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    DoesItButton(
                        text = "Salvar Alterações",
                        onClick = {
                            scope.launch {
                                isSaving = true
                                val body = buildMap<String, String> {
                                    put("cep", cep); put("rua", rua); put("numero", numero)
                                    put("bairro", bairro); put("cidade", cidade); put("estado", estado)
                                    if (titulo.isNotBlank()) put("titulo", titulo)
                                }
                                val result = if (addressId > 0L)
                                    repo.updateAddress(addressId, body)
                                else
                                    repo.createAddress(body)

                                result.fold(
                                    onSuccess = { successMsg = "Endereço salvo com sucesso!"; isSuccess = true },
                                    onFailure = { errorMsg = it.message ?: "Erro ao salvar endereço"; isError = true }
                                )
                                isSaving = false
                            }
                        },
                        isLoading = isSaving,
                        enabled   = isFormFilled,
                        showArrow = false
                    )

                    Spacer(Modifier.height(32.dp))
                }
            }

            // Error Banner (5 s — vermelho)
            AnimatedVisibility(
                visible = isError && errorMsg.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter)
            ) { ErrorBanner(errorMsg) }

            // Success Banner (5 s — verde)
            AnimatedVisibility(
                visible = isSuccess && successMsg.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter)
            ) { SuccessBanner(successMsg) }
        }
    }
}
