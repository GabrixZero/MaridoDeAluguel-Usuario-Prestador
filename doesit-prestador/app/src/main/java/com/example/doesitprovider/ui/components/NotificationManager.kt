package com.example.doesitprovider.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class NotificationType { SUCCESS, ERROR }

// Sistema de notificações em tempo real — estrutura mantida para futura integração
// com push notifications da AWS. Para mostrar mensagens nas telas, usar ErrorBanner
// e SuccessBanner diretamente em cada Composable.
object NotificationManager {
    var message by mutableStateOf<String?>(null)
    var type    by mutableStateOf(NotificationType.ERROR)
    var isVisible by mutableStateOf(false)
}
