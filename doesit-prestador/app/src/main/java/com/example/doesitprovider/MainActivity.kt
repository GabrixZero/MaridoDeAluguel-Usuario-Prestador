package com.example.doesitprovider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.doesitprovider.data.network.SessionManager
import com.example.doesitprovider.ui.components.NotificationPopupHost
import com.example.doesitprovider.ui.navigation.SetupNavGraph
import com.example.doesitprovider.ui.theme.DoesitproviderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa EncryptedSharedPreferences — obrigatório antes de qualquer
        // acesso ao SessionManager (token, userId, etc.)
        try {
            SessionManager.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DoesitproviderTheme {
                Box(Modifier.fillMaxSize().systemBarsPadding()) {
                    SetupNavGraph()
                    NotificationPopupHost()
                }
            }
        }
    }
}
