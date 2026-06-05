package com.example.doesitprovider.ui.screens.register

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.doesitprovider.R
import com.example.doesitprovider.data.model.RegisterRequest
import com.example.doesitprovider.data.repository.UserRepository
import com.example.doesitprovider.ui.components.DoesItButton
import com.example.doesitprovider.ui.components.DoesItTextField
import com.example.doesitprovider.ui.components.ErrorBanner
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    // Assinatura alinhada com o app Usuário: passa mensagem de sucesso para LoginScreen
    onRegisterSuccess: (String) -> Unit
) {
    var nome     by remember { mutableStateOf("") }
    var cpf      by remember { mutableStateOf("") }
    var dataNasc by remember { mutableStateOf("") }
    var genero   by remember { mutableStateOf("") }
    var celular  by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var senha    by remember { mutableStateOf("") }

    var cep     by remember { mutableStateOf("") }
    var rua     by remember { mutableStateOf("") }
    var numero  by remember { mutableStateOf("") }
    var bairro  by remember { mutableStateOf("") }
    var cidade  by remember { mutableStateOf("") }
    var estado  by remember { mutableStateOf("") }

    var isLoading       by remember { mutableStateOf(false) }
    var generoExpanded  by remember { mutableStateOf(false) }
    var isCelularFocused by remember { mutableStateOf(false) }
    var errors          by remember { mutableStateOf(setOf<String>()) }

    // Error Banner state
    var errorMessage by remember { mutableStateOf("") }
    var isError      by remember { mutableStateOf(false) }

    val generos = listOf("Masculino", "Feminino", "Outros", "Não informar")
    val scope      = rememberCoroutineScope()
    val repository = remember { UserRepository() }

    LaunchedEffect(isError) { if (isError) { delay(5000); isError = false } }

    fun showError(msg: String) {
        errorMessage = msg
        isError = true
        Log.e("RegisterScreen", "Erro: $msg")
    }

    fun validate(): String? {
        val newErrors = mutableSetOf<String>()

        if (nome.isBlank() || !nome.all { it.isLetter() || it.isWhitespace() }) {
            newErrors.add("nome"); errors = newErrors
            return "O campo Nome Completo deve aceitar somente letras."
        }
        if (cpf.length != 11) {
            newErrors.add("cpf"); errors = newErrors
            return "O campo CPF deve conter 11 dígitos."
        }

        // Validação de data de nascimento
        var dateError: String? = null
        if (dataNasc.length != 10) {
            dateError = "Data de nascimento inválida (use DD/MM/AAAA)."
        } else {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
                val birthDate = sdf.parse(dataNasc)
                if (birthDate != null) {
                    val cal = Calendar.getInstance()
                    val ageCal = Calendar.getInstance().apply { time = birthDate }
                    var age = cal.get(Calendar.YEAR) - ageCal.get(Calendar.YEAR)
                    if (cal.get(Calendar.DAY_OF_YEAR) < ageCal.get(Calendar.DAY_OF_YEAR)) age--
                    if (age < 18)  dateError = "Você deve ter pelo menos 18 anos."
                    if (age > 110) dateError = "Data de nascimento inválida."
                } else dateError = "Data de nascimento inválida."
            } catch (_: Exception) { dateError = "Data de nascimento inválida." }
        }
        if (dateError != null) { newErrors.add("dataNasc"); errors = newErrors; return dateError }

        if (genero.isBlank()) { newErrors.add("genero"); errors = newErrors; return "Selecione seu gênero." }

        if (cep.length != 8) { newErrors.add("cep"); errors = newErrors; return "CEP inválido (deve ter 8 dígitos)." }

        // Rua aceita qualquer caractere (alinhado com app Usuário)
        if (rua.isBlank()) { newErrors.add("rua"); errors = newErrors; return "Informe a rua." }

        if (numero.isEmpty()) { newErrors.add("numero"); errors = newErrors; return "Informe o número do endereço." }
        if (bairro.isBlank()) { newErrors.add("bairro"); errors = newErrors; return "Informe o bairro." }

        if (cidade.isBlank() || !cidade.all { it.isLetter() || it.isWhitespace() }) {
            newErrors.add("cidade"); errors = newErrors; return "O campo Cidade deve conter apenas letras."
        }
        if (estado.length != 2 || !estado.all { it.isLetter() }) {
            newErrors.add("estado"); errors = newErrors; return "O campo Estado deve conter exatamente 2 letras (UF)."
        }

        // Celular: mínimo 10 dígitos, máximo 11
        if (celular.length < 10 || celular.length > 11) {
            newErrors.add("celular"); errors = newErrors
            return "Celular inválido (mínimo 10, máximo 11 dígitos)."
        }

        if (email.isBlank() || !email.contains("@")) {
            newErrors.add("email"); errors = newErrors; return "E-mail inválido."
        }

        // Regra de senha forte — alinhada com backend e app Usuário
        val hasUpper  = senha.any { it.isUpperCase() }
        val hasLower  = senha.any { it.isLowerCase() }
        val hasDigit  = senha.any { it.isDigit() }
        val hasSymbol = senha.any { !it.isLetterOrDigit() }
        if (senha.length < 8 || !hasUpper || !hasLower || !hasDigit || !hasSymbol) {
            newErrors.add("senha"); errors = newErrors
            return "A senha deve ter no mínimo 8 caracteres, uma letra maiúscula, uma minúscula, um número e um símbolo."
        }

        errors = emptySet()
        return null
    }

    fun formatCpf(input: String) = input.filter { it.isDigit() }.take(11)
    fun formatDataNasc(input: String): String {
        val d = input.filter { it.isDigit() }.take(8)
        return when {
            d.length <= 2 -> d
            d.length <= 4 -> "${d.substring(0, 2)}/${d.substring(2)}"
            else -> "${d.substring(0, 2)}/${d.substring(2, 4)}/${d.substring(4)}"
        }
    }
    fun formatCelularVisual(input: String): String {
        val d = input.filter { it.isDigit() }
        return when {
            d.length <= 2 -> d
            d.length <= 7 -> "(${d.substring(0, 2)})${d.substring(2)}"
            else -> "(${d.substring(0, 2)})${d.substring(2, 7)}-${d.substring(7, minOf(11, d.length))}"
        }
    }

    val isFormComplete = nome.isNotEmpty() && cpf.isNotEmpty() && dataNasc.isNotEmpty() &&
        genero.isNotEmpty() && cep.isNotEmpty() && rua.isNotEmpty() &&
        numero.isNotEmpty() && bairro.isNotEmpty() && cidade.isNotEmpty() &&
        estado.isNotEmpty() && celular.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty()

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            item {
                Spacer(Modifier.height(50.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo_doesit_prestador),
                    contentDescription = "Logo",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(32.dp))
                Text("DoesIt", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text("Acesse sua conta de prestador e gerencie seus serviços.", fontSize = 14.sp, color = AppColors.TextSecondary)

                Spacer(Modifier.height(32.dp))

                // Tabs
                Row(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.weight(1f).clickable { onBackToLogin() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Entrar", color = AppColors.TextSecondary, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.width(60.dp).height(2.dp).background(Color.Transparent))
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cadastrar", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.width(60.dp).height(2.dp).background(AppColors.Primary))
                    }
                }

                Spacer(Modifier.height(32.dp))
                SectionLabel("Dados pessoais")

                DoesItTextField(
                    value = nome,
                    onValueChange = { if (it.all { c -> c.isLetter() || c.isWhitespace() }) nome = it },
                    label = "Nome completo", placeholder = "Como está no seu documento",
                    isError = "nome" in errors
                )
                Spacer(Modifier.height(16.dp))
                DoesItTextField(
                    value = cpf, onValueChange = { cpf = formatCpf(it) },
                    label = "CPF", placeholder = "000.000.000-00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = "cpf" in errors
                )
                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    DoesItTextField(
                        value = dataNasc, onValueChange = { dataNasc = formatDataNasc(it) },
                        label = "Data de nascimento", placeholder = "dd/mm/aaaa",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = "dataNasc" in errors
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Gênero", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = AppColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                        Box {
                            OutlinedTextField(
                                value = genero, onValueChange = {}, readOnly = true,
                                placeholder = { Text("Selecione", color = AppColors.TextDisabled) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = AppColors.TextSecondary)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor   = AppColors.InputBackground,
                                    unfocusedContainerColor = AppColors.InputBackground,
                                    focusedBorderColor      = if ("genero" in errors) AppColors.ErrorBanner else AppColors.Primary,
                                    unfocusedBorderColor    = if ("genero" in errors) AppColors.ErrorBanner else Color.Transparent,
                                    focusedTextColor        = AppColors.TextPrimary,
                                    unfocusedTextColor      = AppColors.TextPrimary,
                                    disabledContainerColor  = AppColors.InputBackground,
                                    disabledBorderColor     = if ("genero" in errors) AppColors.ErrorBanner else Color.Transparent,
                                    disabledTextColor       = AppColors.TextPrimary,
                                    disabledPlaceholderColor = AppColors.TextDisabled
                                ),
                                enabled = false
                            )
                            Box(Modifier.matchParentSize()
                                .background(Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { generoExpanded = true })
                            DropdownMenu(
                                expanded = generoExpanded,
                                onDismissRequest = { generoExpanded = false },
                                modifier = Modifier.background(AppColors.Surface)
                            ) {
                                generos.forEach { g ->
                                    DropdownMenuItem(
                                        text = { Text(g, color = AppColors.TextPrimary) },
                                        onClick = { genero = g; generoExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                SectionLabel("Endereço")

                DoesItTextField(
                    value = cep,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 8) cep = it },
                    label = "CEP", placeholder = "00000-000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = "cep" in errors
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    // Rua aceita qualquer caractere — alinhado com app Usuário
                    DoesItTextField(
                        value = rua, onValueChange = { rua = it },
                        label = "Rua", placeholder = "Nome da rua",
                        modifier = Modifier.weight(2f),
                        isError = "rua" in errors
                    )
                    Spacer(Modifier.width(16.dp))
                    DoesItTextField(
                        value = numero,
                        onValueChange = { if (it.all { c -> c.isDigit() }) numero = it },
                        label = "Número", placeholder = "000",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = "numero" in errors
                    )
                }
                Spacer(Modifier.height(16.dp))
                DoesItTextField(
                    value = bairro, onValueChange = { bairro = it },
                    label = "Bairro", placeholder = "Nome do bairro",
                    isError = "bairro" in errors
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    DoesItTextField(
                        value = cidade,
                        onValueChange = { if (it.all { c -> c.isLetter() || c.isWhitespace() }) cidade = it },
                        label = "Cidade", placeholder = "Sua cidade",
                        modifier = Modifier.weight(2f),
                        isError = "cidade" in errors
                    )
                    Spacer(Modifier.width(16.dp))
                    DoesItTextField(
                        value = estado,
                        onValueChange = { if (it.all { c -> c.isLetter() }) estado = it.uppercase().take(2) },
                        label = "Estado", placeholder = "UF",
                        modifier = Modifier.weight(1f),
                        isError = "estado" in errors
                    )
                }

                Spacer(Modifier.height(32.dp))
                SectionLabel("Acessos")

                // Celular: máximo 11 dígitos
                DoesItTextField(
                    value = if (isCelularFocused) celular else formatCelularVisual(celular),
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 11) celular = it },
                    label = "Celular", placeholder = "(11) 90000-0000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = "celular" in errors,
                    modifier = Modifier.onFocusChanged { isCelularFocused = it.isFocused }
                )
                Spacer(Modifier.height(16.dp))
                DoesItTextField(
                    value = email, onValueChange = { email = it },
                    label = "E-mail", placeholder = "exemplo@email.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = "email" in errors
                )
                Spacer(Modifier.height(16.dp))
                DoesItTextField(
                    value = senha, onValueChange = { senha = it },
                    label = "Senha", placeholder = "Mínimo 8 caracteres",
                    isPassword = true,
                    isError = "senha" in errors
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        val error = validate()
                        if (error == null) {
                            Log.d("RegisterScreen", "Formulário válido. Iniciando registro...")
                            scope.launch {
                                isLoading = true
                                repository.register(
                                    RegisterRequest(
                                        name = nome, email = email, password = senha,
                                        phone = celular, cpf = cpf,
                                        birthDate = dataNasc, gender = genero,
                                        addressCep = cep, addressStreet = rua,
                                        addressNumber = numero, addressNeighborhood = bairro,
                                        addressCity = cidade, addressState = estado
                                    )
                                ).fold(
                                    onSuccess = {
                                        Log.d("RegisterScreen", "✓ Registro bem-sucedido")
                                        // Navega de volta ao Login passando mensagem (igual ao app Usuário)
                                        onRegisterSuccess("Cadastro feito com sucesso!")
                                    },
                                    onFailure = {
                                        Log.e("RegisterScreen", "✗ Erro no registro: ${it.message}")
                                        showError(it.message ?: "Erro ao fazer o cadastro")
                                    }
                                )
                                isLoading = false
                            }
                        } else {
                            Log.d("RegisterScreen", "Erro de validação: $error")
                            showError(error)
                        }
                    },
                    enabled = isFormComplete && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormComplete) AppColors.Primary else AppColors.ButtonDisabled,
                        contentColor   = if (isFormComplete) Color.White else AppColors.TextButtonDisabled
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Criar conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        // ErrorBanner animado — igual ao app Usuário (substitui NotificationManager.show)
        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = slideInVertically { -it } + fadeIn(),
            exit  = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter)
        ) { ErrorBanner(message = errorMessage) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold,
        color = AppColors.TextPrimary, modifier = Modifier.padding(bottom = 16.dp))
}
