package com.example.doesitprovider.ui.screens.specialties

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.doesitprovider.data.model.*
import com.example.doesitprovider.data.repository.ServiceRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.components.ErrorBanner
import com.example.doesitprovider.ui.components.SuccessBanner
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialtiesScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val repo = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var isSaving  by remember { mutableStateOf(false) }

    // Estado local para a lista de linhas editáveis
    val rows = remember { mutableStateListOf<EditableSpecialty>() }
    var catalog by remember { mutableStateOf<List<CatalogServiceDTO>>(emptyList()) }

    // Estado para a linha de "Adição" (a última com o botão +)
    var newItemCategoryId by remember { mutableStateOf<Int?>(null) }
    var newItemPrice      by remember { mutableStateOf("") }

    var errorMsg   by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }

    LaunchedEffect(errorMsg)   { if (errorMsg.isNotEmpty())   { delay(5000); errorMsg   = "" } }
    LaunchedEffect(successMsg) { if (successMsg.isNotEmpty()) { delay(5000); successMsg = "" } }

    fun loadData() {
        scope.launch {
            isLoading = true
            repo.getSpecialtiesFlow().fold(
                onSuccess = { response ->
                    catalog = response.generalCatalog
                    rows.clear()
                    response.linkedSpecialties.forEach {
                        rows.add(EditableSpecialty(it.serviceTypeId, it.basePrice.toString()))
                    }
                },
                onFailure = { errorMsg = "Erro ao carregar especialidades" }
            )
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Especialidades", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
        },
        bottomBar = {
            Column {
                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            
                            // 1. Processa as linhas que já estão na lista
                            val listToSave = rows.mapNotNull { row ->
                                val price = row.price.replace(",", ".").toDoubleOrNull()
                                if (price != null && price > 0) {
                                    SaveSpecialtyDTO(row.categoryId, price)
                                } else null
                            }.toMutableList()

                            // 2. Processa a linha de "Adição" se estiver preenchida (evita que o usuário precise clicar no +)
                            val pendingPrice = newItemPrice.replace(",", ".").toDoubleOrNull()
                            if (newItemCategoryId != null && pendingPrice != null && pendingPrice > 0) {
                                if (listToSave.none { it.serviceTypeId == newItemCategoryId }) {
                                    listToSave.add(SaveSpecialtyDTO(newItemCategoryId!!, pendingPrice))
                                }
                            }

                            if (listToSave.isEmpty()) {
                                errorMsg = "Selecione ao menos uma especialidade e defina seu valor base."
                                isSaving = false
                                return@launch
                            }

                            repo.saveSpecialtiesFlow(SaveSpecialtiesRequest(listToSave)).fold(
                                onSuccess = {
                                    successMsg = "Alterações salvas com sucesso!"
                                    // Limpa a linha de adição
                                    newItemCategoryId = null
                                    newItemPrice = ""
                                    loadData()
                                },
                                onFailure = { errorMsg = it.message ?: "Erro ao salvar" }
                            )
                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    enabled = !isSaving && !isLoading
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Salvar Alterações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                DoesItBottomNavBar(currentRoute = "specialties", onNavigate = { route ->
                    when (route) {
                        "home"    -> onNavigateToHome()
                        "profile" -> onNavigateToProfile()
                    }
                })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text("Meus Serviços", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Selecione as categorias de serviços que você realiza e defina o valor base cobrado por cada um.",
                    color = AppColors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp
                )
                Spacer(Modifier.height(20.dp))

                if (isLoading) {
                    Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Linhas existentes
                        itemsIndexed(rows) { index, row ->
                            SpecialtyEditableRow(
                                catalog = catalog,
                                selectedId = row.categoryId,
                                price = row.price,
                                isLast = false,
                                onIdChange = { newId -> rows[index] = row.copy(categoryId = newId) },
                                onPriceChange = { newPrice -> rows[index] = row.copy(price = newPrice) },
                                onAction = { rows.removeAt(index) }
                            )
                        }

                        // Linha para adicionar nova
                        item {
                            SpecialtyEditableRow(
                                catalog = catalog,
                                selectedId = newItemCategoryId,
                                price = newItemPrice,
                                isLast = true,
                                onIdChange = { newItemCategoryId = it },
                                onPriceChange = { newItemPrice = it },
                                onAction = {
                                    val id = newItemCategoryId
                                    val priceStr = newItemPrice
                                    if (id != null && priceStr.isNotEmpty()) {
                                        rows.add(EditableSpecialty(id, priceStr))
                                        newItemCategoryId = null
                                        newItemPrice = ""
                                    }
                                }
                            )
                        }
                        
                        item { Spacer(Modifier.height(20.dp)) }
                    }
                }
            }

            // Banners de feedback
            Box(Modifier.fillMaxWidth().padding(top = 16.dp).zIndex(100f)) {
                AnimatedVisibility(
                    visible = errorMsg.isNotEmpty(),
                    enter = slideInVertically { -it } + fadeIn(),
                    exit  = slideOutVertically { -it } + fadeOut()
                ) { ErrorBanner(errorMsg) }

                AnimatedVisibility(
                    visible = successMsg.isNotEmpty(),
                    enter = slideInVertically { -it } + fadeIn(),
                    exit  = slideOutVertically { -it } + fadeOut()
                ) { SuccessBanner(successMsg) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpecialtyEditableRow(
    catalog: List<CatalogServiceDTO>,
    selectedId: Int?,
    price: String,
    isLast: Boolean,
    onIdChange: (Int) -> Unit,
    onPriceChange: (String) -> Unit,
    onAction: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = catalog.find { it.serviceTypeId == selectedId }?.serviceName ?: "Selecionar..."

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Dropdown
        Box(modifier = Modifier.weight(1.2f)) {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(12.dp),
                color = AppColors.InputBackground,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedName,
                        fontSize = 14.sp,
                        color = if (selectedId == null) AppColors.TextDisabled else AppColors.TextPrimary,
                        maxLines = 1
                    )
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = AppColors.TextSecondary)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(AppColors.Surface).fillMaxWidth(0.5f)
            ) {
                catalog.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.serviceName, color = AppColors.TextPrimary) },
                        onClick = {
                            onIdChange(item.serviceTypeId)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Preço
        OutlinedTextField(
            value = price,
            onValueChange = { if (it.length <= 10) onPriceChange(it) },
            placeholder = { Text("0,00", fontSize = 14.sp) },
            modifier = Modifier.weight(0.8f).height(56.dp),
            shape = RoundedCornerShape(12.dp),
            prefix = { Text("R$ ", fontWeight = FontWeight.Medium, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = AppColors.InputBackground,
                focusedContainerColor   = AppColors.InputBackground,
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = AppColors.Primary
            )
        )

        // Botão + ou -
        IconButton(
            onClick = onAction,
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isLast) AppColors.Primary else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
        ) {
            if (isLast) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.InputBackground,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, null, tint = AppColors.TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

data class EditableSpecialty(
    val categoryId: Int,
    val price: String
)
