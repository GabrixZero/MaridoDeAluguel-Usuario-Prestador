package com.example.doesitprovider.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.doesitprovider.data.network.SessionManager
import com.example.doesitprovider.data.repository.UserRepository
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
fun SettingScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: ((String?) -> Unit)? = null,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var showPasswordModal by remember { mutableStateOf(false) }
    var showDeleteModal   by remember { mutableStateOf(false) }

    var bannerError   by remember { mutableStateOf("") }
    var bannerSuccess by remember { mutableStateOf("") }

    var pushEnabled     by remember { mutableStateOf(true) }
    var emailEnabled    by remember { mutableStateOf(true) }
    var smsEnabled      by remember { mutableStateOf(false) }
    var whatsappEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(bannerError)   { if (bannerError.isNotEmpty())   { delay(5000); bannerError   = "" } }
    LaunchedEffect(bannerSuccess) { if (bannerSuccess.isNotEmpty()) { delay(5000); bannerSuccess = "" } }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            Column {
                Spacer(Modifier.height(40.dp))
                CenterAlignedTopAppBar(
                    title = { Text("Configurações", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = AppColors.TextPrimary) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
                )
            }
        },
        bottomBar = {
            DoesItBottomNavBar(currentRoute = "settings", onNavigate = { route ->
                when (route) {
                    "home"    -> onNavigateToHome()
                    "history" -> onNavigateToHistory()
                    "profile" -> onNavigateToProfile()
                }
            })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp)
            ) {
                item { SettingSectionTitle("DÚVIDAS") }
                item {
                    SettingGroupCard {
                        SettingItem("Central de Ajuda", Icons.AutoMirrored.Filled.KeyboardArrowRight) {}
                        HorizontalDivider(color = AppColors.Border)
                        SettingItem("Ligar na Central de Atendimento", Icons.Default.Phone) {}
                    }
                }
                item { SettingSectionTitle("TERMOS E POLÍTICA") }
                item {
                    SettingGroupCard {
                        SettingItem("Política de privacidade", Icons.AutoMirrored.Filled.KeyboardArrowRight) {}
                        HorizontalDivider(color = AppColors.Border)
                        SettingItem("Termos de uso", Icons.AutoMirrored.Filled.KeyboardArrowRight) {}
                    }
                }
                item { SettingSectionTitle("NOTIFICAÇÕES") }
                item {
                    SettingGroupCard {
                        SettingSwitchItem("Notificação Push", pushEnabled)     { pushEnabled     = it }
                        HorizontalDivider(color = AppColors.Border)
                        SettingSwitchItem("Email", emailEnabled)               { emailEnabled    = it }
                        HorizontalDivider(color = AppColors.Border)
                        SettingSwitchItem("SMS", smsEnabled)                   { smsEnabled      = it }
                        HorizontalDivider(color = AppColors.Border)
                        SettingSwitchItem("WhatsApp", whatsappEnabled)         { whatsappEnabled = it }
                    }
                }
                item { SettingSectionTitle("MEUS DADOS") }
                item {
                    SettingGroupCard {
                        SettingItem("Alterar senha de acesso", Icons.Default.Lock) { showPasswordModal = true }
                        HorizontalDivider(color = AppColors.Border)
                        SettingItem("Sair da conta", Icons.AutoMirrored.Filled.ExitToApp, textColor = AppColors.TextSecondary) {
                            SessionManager.clear()
                            onLogout?.invoke(null)
                        }
                        HorizontalDivider(color = AppColors.Border)
                        SettingItem("Excluir Conta DoesIt", Icons.Default.Delete, textColor = AppColors.Error) { showDeleteModal = true }
                    }
                }
                item { Spacer(Modifier.height(56.dp)) }
            }

            AnimatedVisibility(
                visible = bannerError.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.padding(top = 40.dp).zIndex(1f)
            ) { ErrorBanner(bannerError) }

            AnimatedVisibility(
                visible = bannerSuccess.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.padding(top = 40.dp).zIndex(1f)
            ) { SuccessBanner(bannerSuccess) }
        }
    }

    if (showPasswordModal) {
        ChangePasswordModal(
            onDismiss = { showPasswordModal = false },
            onSuccess = { showPasswordModal = false; bannerSuccess = "Senha alterada com sucesso!" },
            onError   = { bannerError = it }
        )
    }

    if (showDeleteModal) {
        DeleteAccountModal(
            onDismiss       = { showDeleteModal = false },
            onLogoutSuccess = {
                SessionManager.clear()
                onLogout?.invoke("Sua conta foi excluída com sucesso.")
            }
        )
    }
}

@Composable
fun ChangePasswordModal(onDismiss: () -> Unit, onSuccess: () -> Unit, onError: (String) -> Unit) {
    var senhaAtual     by remember { mutableStateOf("") }
    var novaSenha      by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(false) }
    var localError     by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository() }
    val isButtonEnabled = senhaAtual.isNotEmpty() && novaSenha.isNotEmpty() && confirmarSenha.isNotEmpty() && !isLoading

    LaunchedEffect(localError) { if (localError.isNotEmpty()) { delay(5000); localError = "" } }

    fun isPasswordStrong(p: String) = p.length >= 8 && p.any { it.isUpperCase() } &&
        p.any { it.isLowerCase() } && p.any { it.isDigit() } && p.any { !it.isLetterOrDigit() }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { onDismiss() }, Alignment.Center) {
            Card(
                Modifier.fillMaxWidth(0.9f).clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Nova senha", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = AppColors.Primary) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Crie uma nova senha de acesso para sua conta DoesIt.", fontSize = 14.sp, color = AppColors.TextSecondary, lineHeight = 20.sp)
                    Spacer(Modifier.height(24.dp))

                    DoesItTextField(senhaAtual, { senhaAtual = it }, "Senha atual", "••••••••", isPassword = true)
                    Spacer(Modifier.height(16.dp))
                    DoesItTextField(novaSenha, { novaSenha = it }, "Nova senha", "••••••••", isPassword = true)
                    Spacer(Modifier.height(16.dp))
                    DoesItTextField(confirmarSenha, { confirmarSenha = it }, "Confirmar senha", "••••••••", isPassword = true)
                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            when {
                                !isPasswordStrong(novaSenha) -> localError = "Senha deve ter 8+ caracteres, maiúscula, minúscula, número e símbolo"
                                novaSenha != confirmarSenha  -> localError = "As senhas não coincidem"
                                novaSenha == senhaAtual      -> localError = "A nova senha não pode ser igual à atual"
                                else -> scope.launch {
                                    isLoading = true
                                    repository.changePassword(senhaAtual, novaSenha).fold(
                                        onSuccess = { onSuccess() },
                                        onFailure = { onError(it.message ?: "Erro ao alterar senha") }
                                    )
                                    isLoading = false
                                }
                            }
                        },
                        enabled = isButtonEnabled,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isButtonEnabled) AppColors.Primary else AppColors.ButtonDisabled,
                            contentColor   = if (isButtonEnabled) Color.White else AppColors.TextButtonDisabled
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Confirmar nova senha", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            AnimatedVisibility(
                visible = localError.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp).zIndex(100f)
            ) { ErrorBanner(localError) }
        }
    }
}

@Composable
fun DeleteAccountModal(onDismiss: () -> Unit, onLogoutSuccess: () -> Unit) {
    var confirmarText by remember { mutableStateOf("") }
    var senha         by remember { mutableStateOf("") }
    var isLoading     by remember { mutableStateOf(false) }
    var localError    by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val repository = remember { UserRepository() }
    val isButtonEnabled = confirmarText.isNotEmpty() && senha.isNotEmpty() && !isLoading

    LaunchedEffect(localError) { if (localError.isNotEmpty()) { delay(5000); localError = "" } }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { onDismiss() }, Alignment.Center) {
            Card(
                Modifier.fillMaxWidth(0.9f).clickable(enabled = false) {},
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Excluir conta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.ErrorBanner)
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = AppColors.Primary) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Esta ação é irreversível. Digite \"Confirmar\" e sua senha para continuar.", fontSize = 14.sp, color = AppColors.TextSecondary, lineHeight = 20.sp)
                    Spacer(Modifier.height(24.dp))
                    DoesItTextField(confirmarText, { confirmarText = it }, "Digite \"Confirmar\"", "Confirmar")
                    Spacer(Modifier.height(16.dp))
                    DoesItTextField(senha, { senha = it }, "Sua senha", "••••••••", isPassword = true)
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (!confirmarText.equals("Confirmar", ignoreCase = true)) {
                                localError = "Digite 'Confirmar' corretamente"; return@Button
                            }
                            scope.launch {
                                isLoading = true
                                repository.deleteAccount().fold(
                                    onSuccess = { onLogoutSuccess() },
                                    onFailure = { localError = it.message ?: "Erro ao excluir conta" }
                                )
                                isLoading = false
                            }
                        },
                        enabled = isButtonEnabled,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isButtonEnabled) AppColors.ErrorBanner else AppColors.ButtonDisabled,
                            contentColor   = if (isButtonEnabled) Color.White else AppColors.TextButtonDisabled
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Excluir conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            AnimatedVisibility(
                visible = localError.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp).zIndex(100f)
            ) { ErrorBanner(localError) }
        }
    }
}

@Composable fun SettingSectionTitle(title: String) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
}

@Composable fun SettingGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(0.dp)) { Column(content = content) }
}

@Composable fun SettingItem(text: String, trailingIcon: ImageVector? = null, textColor: Color = AppColors.TextPrimary, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(text, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        if (trailingIcon != null) Icon(trailingIcon, null, tint = AppColors.TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@Composable fun SettingSwitchItem(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AppColors.White, checkedTrackColor = AppColors.Primary,
                uncheckedThumbColor = AppColors.TextSecondary, uncheckedTrackColor = AppColors.SurfaceVariant))
    }
}
