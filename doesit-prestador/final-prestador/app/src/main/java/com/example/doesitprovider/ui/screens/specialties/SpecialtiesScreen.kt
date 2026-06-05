package com.example.doesitprovider.ui.screens.specialties

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.doesitprovider.data.model.ProviderSpecialtyDTO
import com.example.doesitprovider.data.repository.ServiceRepository
import com.example.doesitprovider.ui.components.DoesItBottomNavBar
import com.example.doesitprovider.ui.components.ErrorBanner
import com.example.doesitprovider.ui.components.SuccessBanner
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// Mesmas 7 categorias do app Usuário — sem Ar-condicionado
private val ALL_CATEGORIES = listOf(
    "Encanamento", "Elétrica", "Pintura", "Marcenaria", "Limpeza", "Chaveiro", "Montagem"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialtiesScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val repo = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()

    var specialties by remember { mutableStateOf<List<ProviderSpecialtyDTO>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }

    // Campos para nova especialidade
    var newCategory       by remember { mutableStateOf("") }
    var newPrice          by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isSaving          by remember { mutableStateOf(false) }

    var errorMsg   by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }

    LaunchedEffect(errorMsg)   { if (errorMsg.isNotEmpty())   { delay(5000); errorMsg   = "" } }
    LaunchedEffect(successMsg) { if (successMsg.isNotEmpty()) { delay(5000); successMsg = "" } }

    fun loadSpecialties() {
        scope.launch {
            isLoading = true
            repo.getMySpecialties().fold(
                onSuccess = { specialties = it },
                onFailure = { errorMsg = "Erro ao carregar especialidades" }
            )
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadSpecialties() }

    // Categorias ainda não adicionadas
    val availableCategories = ALL_CATEGORIES.filter { cat ->
        specialties.none { it.categoryName.equals(cat, ignoreCase = true) }
    }

    // Preço convertido de String → Double
    val parsedPrice: Double? = newPrice.replace(",", ".").toDoubleOrNull()
    val canAdd = newCategory.isNotEmpty() && parsedPrice != null

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
            DoesItBottomNavBar(currentRoute = "specialties", onNavigate = { route ->
                when (route) {
                    "home"    -> onNavigateToHome()
                    "profile" -> onNavigateToProfile()
                }
            })
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
                    "Defina as categorias que você atende e o seu valor por serviço. Sem definição, será usado o preço padrão da plataforma.",
                    color = AppColors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp
                )
                Spacer(Modifier.height(20.dp))

                // ── Campo para adicionar nova especialidade ────────────────
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Adicionar especialidade", fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                            // Dropdown de categoria
                            ExposedDropdownMenuBox(
                                expanded = isDropdownExpanded,
                                onExpandedChange = { if (availableCategories.isNotEmpty()) isDropdownExpanded = it },
                                modifier = Modifier.weight(1.5f)
                            ) {
                                OutlinedTextField(
                                    value = newCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Categoria", color = AppColors.TextDisabled, fontSize = 14.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isDropdownExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = AppColors.InputBackground,
                                        focusedContainerColor   = AppColors.InputBackground,
                                        unfocusedBorderColor    = Color.Transparent,
                                        focusedBorderColor      = AppColors.Primary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                    modifier = Modifier.background(AppColors.Surface)
                                ) {
                                    if (availableCategories.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Todas adicionadas!", color = AppColors.TextSecondary) },
                                            onClick = { isDropdownExpanded = false }
                                        )
                                    }
                                    availableCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, color = AppColors.TextPrimary) },
                                            onClick = { newCategory = cat; isDropdownExpanded = false }
                                        )
                                    }
                                }
                            }

                            // Campo de preço
                            OutlinedTextField(
                                value = newPrice,
                                onValueChange = { v -> if (v.length <= 10) newPrice = v },
                                placeholder = { Text("R$ 0,00", color = AppColors.TextDisabled, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = AppColors.InputBackground,
                                    focusedContainerColor   = AppColors.InputBackground,
                                    unfocusedBorderColor    = Color.Transparent,
                                    focusedBorderColor      = AppColors.Primary
                                )
                            )

                            // Botão adicionar
                            IconButton(
                                onClick = {
                                    val price = parsedPrice ?: return@IconButton
                                    val catMatch = ALL_CATEGORIES.firstOrNull { it.equals(newCategory, ignoreCase = true) }
                                        ?: return@IconButton
                                    scope.launch {
                                        isSaving = true
                                        // Precisa do categoryId — carrega da lista de categorias disponíveis
                                        // Como não temos o ID aqui, usamos o nome para encontrar via API
                                        // Passo: POST /api/providers/specialties com categoryName (backend faz o lookup)
                                        // Temporariamente: o backend já aceita categoryId — o frontend precisará
                                        // buscar o ID da categoria pelo nome. Por agora, passamos 0L e o backend
                                        // é responsável por resolver pelo nome (ajuste necessário no backend).
                                        // ATENÇÃO: ideal é ter /api/categories e cachear os IDs.
                                        // Aqui usamos índice da lista como placeholder (1-based).
                                        val categoryIndex = ALL_CATEGORIES.indexOf(catMatch)
                                        // O ID real vem do backend ao carregar a lista de especialidades.
                                        // Aqui chamamos upsert com categoryId baseado no index + 1 como fallback.
                                        // DEVE ser substituído por um mapeamento real quando /api/categories estiver integrado.
                                        val categoryId = (categoryIndex + 1).toLong()
                                        repo.upsertSpecialty(categoryId, price).fold(
                                            onSuccess = {
                                                newCategory = ""; newPrice = ""
                                                successMsg = "Especialidade adicionada!"
                                                loadSpecialties()
                                            },
                                            onFailure = { errorMsg = it.message ?: "Erro ao salvar" }
                                        )
                                        isSaving = false
                                    }
                                },
                                enabled = canAdd && !isSaving,
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        if (canAdd) AppColors.Primary else AppColors.ButtonDisabled,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(Icons.Default.Add, null,
                                    tint = if (canAdd) Color.White else AppColors.TextButtonDisabled)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Lista de especialidades cadastradas ────────────────────
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                } else if (specialties.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MilitaryTech, null, Modifier.size(48.dp), tint = AppColors.TextDisabled)
                            Spacer(Modifier.height(8.dp))
                            Text("Nenhuma especialidade definida ainda.", color = AppColors.TextSecondary, fontSize = 14.sp)
                            Text("Use o formulário acima para adicionar.", color = AppColors.TextDisabled, fontSize = 12.sp)
                        }
                    }
                } else {
                    Text("Minhas especialidades (${specialties.size})",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(specialties, key = { it.id }) { item ->
                            SpecialtyRow(
                                item = item,
                                onRemove = {
                                    scope.launch {
                                        repo.deleteSpecialty(item.categoryId).fold(
                                            onSuccess = { successMsg = "${item.categoryName} removida."; loadSpecialties() },
                                            onFailure = { errorMsg = it.message ?: "Erro ao remover" }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }

            // Banners de feedback
            AnimatedVisibility(
                visible = errorMsg.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).zIndex(10f)
            ) { ErrorBanner(errorMsg) }

            AnimatedVisibility(
                visible = successMsg.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).zIndex(10f)
            ) { SuccessBanner(successMsg) }
        }
    }
}

@Composable
private fun SpecialtyRow(item: ProviderSpecialtyDTO, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    Modifier.size(40.dp).background(AppColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    Alignment.Center
                ) {
                    Icon(Icons.Default.MilitaryTech, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(item.categoryName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
                    Text(
                        "R$ ${String.format(Locale.getDefault(), "%.2f", item.price)}",
                        fontSize = 14.sp, color = AppColors.Primary, fontWeight = FontWeight.Medium
                    )
                }
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp).background(AppColors.ErrorLight, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Delete, null, tint = AppColors.Error, modifier = Modifier.size(18.dp))
            }
        }
    }
}
