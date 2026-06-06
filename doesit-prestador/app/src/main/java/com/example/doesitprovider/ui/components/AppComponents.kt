package com.example.doesitprovider.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.doesitprovider.ui.theme.AppColors
import kotlinx.coroutines.delay

// ── Banners de feedback ────────────────────────────────────────────────────────

@Composable
fun SuccessBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(AppColors.SuccessBanner, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(message, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(AppColors.ErrorBanner, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(message, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Botão principal ────────────────────────────────────────────────────────────

@Composable
fun DoesItButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    showArrow: Boolean = true,
    containerColor: Color = AppColors.Primary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) containerColor else AppColors.ButtonDisabled,
            disabledContainerColor = AppColors.ButtonDisabled
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) contentColor else AppColors.TextButtonDisabled
                )
                if (showArrow) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, null,
                        Modifier.size(18.dp),
                        tint = if (enabled) contentColor else AppColors.TextButtonDisabled
                    )
                }
            }
        }
    }
}

// ── Campo de texto ─────────────────────────────────────────────────────────────

@Composable
fun DoesItTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = AppColors.TextDisabled) },
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = isError,
            keyboardOptions = keyboardOptions,
            readOnly = readOnly,
            trailingIcon = if (isPassword) {
                {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, null, tint = AppColors.TextSecondary)
                    }
                }
            } else trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = AppColors.InputBackground,
                unfocusedContainerColor = AppColors.InputBackground,
                focusedBorderColor      = if (isError) AppColors.ErrorBanner else AppColors.Primary,
                unfocusedBorderColor    = if (isError) AppColors.ErrorBanner else Color.Transparent,
                errorBorderColor        = AppColors.ErrorBanner,
                focusedTextColor        = AppColors.TextPrimary,
                unfocusedTextColor      = AppColors.TextPrimary,
                cursorColor             = AppColors.Primary
            )
        )
    }
}

// ── Bottom Navigation Bar ─────────────────────────────────────────────────────

@Composable
fun DoesItBottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = AppColors.Surface, tonalElevation = 8.dp) {
        val items = listOf(
            Triple("home",    "Início",   Icons.Default.Home),
            Triple("history", "Serviços", Icons.Default.Assignment),
            Triple("profile", "Perfil",   Icons.Default.Person)
        )
        items.forEach { (route, label, icon) ->
            val selected = currentRoute == route
            NavigationBarItem(
                selected = selected,
                onClick  = { if (!selected) onNavigate(route) },
                icon = { Icon(icon, label, tint = if (selected) AppColors.Primary else AppColors.TextSecondary) },
                label = {
                    Text(
                        label,
                        color = if (selected) AppColors.Primary else AppColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}

// ── Card de acesso rápido ──────────────────────────────────────────────────────

@Composable
fun QuickAccessCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AppColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AppColors.Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
        }
    }
}

// ── NotificationPopupHost — overlay de notificações chamado pelo MainActivity ──
// Propriedades reais: NotificationManager.isVisible (Boolean) e .message (String?)

@Composable
fun NotificationPopupHost() {
    val isVisible = NotificationManager.isVisible
    val message   = NotificationManager.message.orEmpty()
    val msgType   = NotificationManager.type

    if (isVisible && message.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .zIndex(99f),
            contentAlignment = Alignment.TopCenter
        ) {
            if (msgType == NotificationType.SUCCESS) SuccessBanner(message)
            else ErrorBanner(message)
        }
    }
}
