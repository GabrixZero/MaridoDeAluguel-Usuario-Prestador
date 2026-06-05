package com.example.doesitprovider.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.doesitprovider.R
import com.example.doesitprovider.data.network.RetrofitClient
import com.example.doesitprovider.data.repository.UserRepository
import com.example.doesitprovider.ui.components.DoesItButton
import com.example.doesitprovider.ui.components.DoesItTextField
import com.example.doesitprovider.ui.components.ErrorBanner
import com.example.doesitprovider.ui.components.SuccessBanner
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ── Tela de Login ─────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToCadastro: () -> Unit,
    initialSuccessMessage: String? = null
) {
    var email     by remember { mutableStateOf("") }
    var senha     by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage  by remember { mutableStateOf("") }
    var isError       by remember { mutableStateOf(false) }
    var showForgotModal by remember { mutableStateOf(false) }
    var successMessage  by remember { mutableStateOf(initialSuccessMessage ?: "") }
    var showSuccess     by remember { mutableStateOf(initialSuccessMessage != null) }

    val scope      = rememberCoroutineScope()
    val repository = remember { UserRepository() }
    val buttonEnabled = email.isNotBlank() && senha.isNotBlank() && !isLoading

    LaunchedEffect(isError) {
        if (isError) { delay(5000); isError = false }
    }
    LaunchedEffect(showSuccess) {
        if (showSuccess) { delay(5000); showSuccess = false }
    }

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Header
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.Start) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_doesit_prestador),
                        contentDescription = "DoesIt Logo",
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text("DoesIt", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text(
                        "Acesse sua conta de prestador e gerencie seus serviços.",
                        fontSize = 14.sp, lineHeight = 20.sp, color = AppColors.TextSecondary
                    )
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                // Tabs
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Entrar", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.width(60.dp).height(2.dp).background(AppColors.Primary))
                    }
                    Column(
                        Modifier.weight(1f).clickable { onGoToCadastro() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Cadastrar", color = AppColors.TextSecondary, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.width(60.dp).height(2.dp).background(Color.Transparent))
                    }
                }

                Spacer(Modifier.height(32.dp))

                DoesItTextField(
                    value = email,
                    onValueChange = { email = it; isError = false },
                    label = "E-mail",
                    placeholder = "joao.silva@gmail.com",
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(Modifier.height(24.dp))
                DoesItTextField(
                    value = senha,
                    onValueChange = { senha = it; isError = false },
                    label = "Senha",
                    placeholder = "••••••••",
                    isPassword = true,
                    isError = isError
                )

                Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        "Esqueci minha senha",
                        color = AppColors.Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showForgotModal = true }
                    )
                }

                Spacer(Modifier.height(48.dp))

                DoesItButton(
                    text = "Acessar conta",
                    onClick = {
                        scope.launch {
                            isLoading = true
                            isError = false
                            repository.login(email, senha).fold(
                                onSuccess = { onLoginSuccess() },
                                onFailure = {
                                    errorMessage = it.message ?: "E-mail e/ou senha incorretos"
                                    isError = true
                                    senha = ""
                                }
                            )
                            isLoading = false
                        }
                    },
                    enabled = buttonEnabled,
                    isLoading = isLoading
                )

                Spacer(Modifier.height(56.dp))
            }
        }

        // Error Banner
        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = slideInVertically { -it } + fadeIn(),
            exit  = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter)
        ) { ErrorBanner(message = errorMessage) }

        // Success Banner
        AnimatedVisibility(
            visible = showSuccess && successMessage.isNotEmpty(),
            enter = slideInVertically { -it } + fadeIn(),
            exit  = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter)
        ) { SuccessBanner(message = successMessage) }

        if (showForgotModal) {
            ForgotPasswordModal(
                onDismiss = { showForgotModal = false },
                onSuccess = { message ->
                    successMessage = message
                    showSuccess    = true
                    showForgotModal = false
                }
            )
        }
    }
}

// ── Modal "Esqueci minha senha" — fluxo de 3 passos ──────────────────────────
// Idêntico ao ForgotPasswordModal do app Usuário; usa DoesItTextField no lugar
// de DarkTextField pois é a convenção do app Prestador.

enum class ForgotStep { ENTER_EMAIL, VERIFY_CODE, NEW_PASSWORD }

@Composable
fun ForgotPasswordModal(onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    var step            by remember { mutableStateOf(ForgotStep.ENTER_EMAIL) }
    var email           by remember { mutableStateOf("") }
    var code            by remember { mutableStateOf(List(6) { "" }) }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }
    var isError         by remember { mutableStateOf(false) }
    var timeLeft        by remember { mutableIntStateOf(60) }
    var canResend       by remember { mutableStateOf(false) }
    var codeSentTime    by remember { mutableLongStateOf(0L) }

    val scope = rememberCoroutineScope()
    val api   = RetrofitClient.apiService

    LaunchedEffect(step) {
        if (step == ForgotStep.VERIFY_CODE) {
            timeLeft = 60; canResend = false; codeSentTime = System.currentTimeMillis()
            while (timeLeft > 0) { delay(1000); timeLeft-- }
            canResend = true
        }
    }
    LaunchedEffect(isError) { if (isError) { delay(5000); isError = false } }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isError && errorMessage.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp)
            ) { ErrorBanner(message = errorMessage) }

            Card(
                modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (step == ForgotStep.NEW_PASSWORD) "Nova senha" else "Esqueci minha senha",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary
                        )
                        Box(
                            modifier = Modifier.size(32.dp)
                                .background(AppColors.PrimaryLight, RoundedCornerShape(8.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, "Fechar", tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when (step) {
                            ForgotStep.ENTER_EMAIL  -> "Informe o seu e-mail abaixo para receber as instruções de recuperação de senha."
                            ForgotStep.VERIFY_CODE  -> "O código foi enviado ao seu e-mail e será válido por 15 minutos."
                            ForgotStep.NEW_PASSWORD -> "Crie uma nova senha de acesso para sua conta DoesIt."
                        },
                        fontSize = 14.sp, color = AppColors.TextSecondary, lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                        },
                        label = "StepTransition"
                    ) { currentStep ->
                        Column {
                            when (currentStep) {
                                ForgotStep.ENTER_EMAIL -> {
                                    DoesItTextField(
                                        value = email,
                                        onValueChange = { email = it; isError = false },
                                        label = "E-mail cadastrado",
                                        placeholder = "joao.silva@email.com",
                                        isError = isError,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                    )
                                    Spacer(Modifier.height(32.dp))
                                    // Passo 1: aceita qualquer e-mail, sem validar existência
                                    DoesItButton(
                                        text = "Confirmar e-mail",
                                        onClick = {
                                            scope.launch {
                                                isLoading = true
                                                try {
                                                    api.forgotPassword(mapOf("email" to email, "method" to "EMAIL"))
                                                } catch (_: Exception) { /* ignora — prossegue sempre */ }
                                                step = ForgotStep.VERIFY_CODE
                                                isLoading = false
                                            }
                                        },
                                        enabled = email.isNotBlank(),
                                        isLoading = isLoading,
                                        showArrow = false
                                    )
                                }

                                ForgotStep.VERIFY_CODE -> {
                                    ForgotCodeInput(
                                        code = code,
                                        isError = isError,
                                        isLoading = isLoading,
                                        canResend = canResend,
                                        timeLeft = timeLeft,
                                        onCodeChange = { idx, v ->
                                            val list = code.toMutableList(); list[idx] = v; code = list
                                            isError = false
                                        },
                                        onConfirm = {
                                            val input = code.joinToString("")
                                            val expired = System.currentTimeMillis() - codeSentTime > 15 * 60 * 1000
                                            if (expired) { errorMessage = "Código expirado. Solicite um novo."; isError = true; return@ForgotCodeInput }
                                            scope.launch {
                                                isLoading = true
                                                try {
                                                    val r = api.verifyCode(mapOf("email" to email, "code" to input))
                                                    if (r.isSuccessful) step = ForgotStep.NEW_PASSWORD
                                                    else { errorMessage = "Código incorreto. Tente novamente."; isError = true }
                                                } catch (_: Exception) { errorMessage = "Erro de conexão."; isError = true }
                                                isLoading = false
                                            }
                                        },
                                        onResend = {
                                            scope.launch {
                                                try { api.forgotPassword(mapOf("email" to email)) } catch (_: Exception) {}
                                                timeLeft = 60; canResend = false; codeSentTime = System.currentTimeMillis()
                                                while (timeLeft > 0) { delay(1000); timeLeft-- }
                                                canResend = true
                                            }
                                        }
                                    )
                                }

                                ForgotStep.NEW_PASSWORD -> {
                                    DoesItTextField(
                                        value = newPassword,
                                        onValueChange = { newPassword = it; isError = false },
                                        label = "Nova senha",
                                        placeholder = "••••••••",
                                        isPassword = true,
                                        isError = isError
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    DoesItTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it; isError = false },
                                        label = "Confirmar senha",
                                        placeholder = "••••••••",
                                        isPassword = true,
                                        isError = isError
                                    )
                                    Spacer(Modifier.height(32.dp))
                                    DoesItButton(
                                        text = "Confirmar nova senha",
                                        onClick = {
                                            when {
                                                newPassword != confirmPassword -> {
                                                    errorMessage = "As senhas não coincidem."
                                                    isError = true
                                                }
                                                !isPasswordStrong(newPassword) -> {
                                                    errorMessage = "A senha deve ter no mínimo 8 caracteres, maiúscula, minúscula, número e símbolo."
                                                    isError = true
                                                }
                                                else -> {
                                                    scope.launch {
                                                        isLoading = true
                                                        try {
                                                            val r = api.resetPassword(mapOf(
                                                                "email"       to email,
                                                                "code"        to code.joinToString(""),
                                                                "newPassword" to newPassword
                                                            ))
                                                            if (r.isSuccessful) onSuccess("Senha alterada com sucesso!")
                                                            else { errorMessage = "Erro ao redefinir senha."; isError = true }
                                                        } catch (_: Exception) { errorMessage = "Erro de conexão."; isError = true }
                                                        isLoading = false
                                                    }
                                                }
                                            }
                                        },
                                        enabled = newPassword.isNotBlank() && confirmPassword.isNotBlank(),
                                        isLoading = isLoading,
                                        showArrow = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Campos numéricos do código ────────────────────────────────────────────────

@Composable
private fun ForgotCodeInput(
    code: List<String>,
    isError: Boolean,
    isLoading: Boolean,
    canResend: Boolean,
    timeLeft: Int,
    onCodeChange: (Int, String) -> Unit,
    onConfirm: () -> Unit,
    onResend: () -> Unit
) {
    val focusRequesters  = remember { List(6) { FocusRequester() } }
    val focusManager     = LocalFocusManager.current
    val keyboardCtrl     = LocalSoftwareKeyboardController.current
    val buttonEnabled    = code.all { it.isNotBlank() } && !isLoading

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
            code.forEachIndexed { index, value ->
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        if (it.length <= 1 && it.all { c -> c.isDigit() }) {
                            onCodeChange(index, it)
                            if (it.isNotBlank()) {
                                if (index < 5) focusRequesters[index + 1].requestFocus()
                                else { focusManager.clearFocus(); keyboardCtrl?.hide() }
                            }
                        }
                    },
                    modifier = Modifier.width(44.dp).height(56.dp)
                        .focusRequester(focusRequesters[index])
                        .onKeyEvent { ev ->
                            if (ev.type == KeyEventType.KeyDown && ev.key == Key.Backspace && value.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus(); true
                            } else false
                        },
                    shape = RoundedCornerShape(8.dp),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = AppColors.InputBackground,
                        unfocusedContainerColor = AppColors.InputBackground,
                        focusedBorderColor      = if (isError) AppColors.ErrorBanner else AppColors.Primary,
                        unfocusedBorderColor    = if (isError) AppColors.ErrorBanner else AppColors.Border,
                        cursorColor             = Color.Transparent
                    ),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        DoesItButton(
            text = "Confirmar código",
            onClick = onConfirm,
            enabled = buttonEnabled,
            isLoading = isLoading,
            showArrow = false
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onResend,
            enabled = canResend,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canResend) AppColors.PrimaryLight else AppColors.ButtonDisabled,
                contentColor   = if (canResend) AppColors.Primary else AppColors.TextButtonDisabled
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = null
        ) {
            val fmt = String.format(Locale.getDefault(), "%02d:%02d", timeLeft / 60, timeLeft % 60)
            Text(
                if (canResend) "Reenviar código" else "Reenviar código ($fmt)",
                fontSize = 15.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun isPasswordStrong(password: String): Boolean =
    password.length >= 8 &&
    password.any { it.isUpperCase() } &&
    password.any { it.isLowerCase() } &&
    password.any { it.isDigit() } &&
    password.any { !it.isLetterOrDigit() }
