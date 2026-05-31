package com.example.doesitprovider.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

enum class NotificationType { SUCCESS, ERROR }

object NotificationManager {
    var message by mutableStateOf<String?>(null)
    var type by mutableStateOf(NotificationType.ERROR)
    var isVisible by mutableStateOf(false)

    suspend fun show(msg: String, notificationType: NotificationType) {
        message = msg
        type = notificationType
        isVisible = true
        delay(5000)
        isVisible = false
    }
}
