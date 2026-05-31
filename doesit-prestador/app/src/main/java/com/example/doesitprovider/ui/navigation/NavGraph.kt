package com.example.doesitprovider.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doesitprovider.data.network.SessionManager
import com.example.doesitprovider.ui.screens.address.AddressScreen
import com.example.doesitprovider.ui.screens.address.EditAddressScreen
import com.example.doesitprovider.ui.screens.avaliacoes.AvaliacoesScreen
import com.example.doesitprovider.ui.screens.history.HistoryScreen
import com.example.doesitprovider.ui.screens.home.HomeScreen
import com.example.doesitprovider.ui.screens.login.LoginScreen
import com.example.doesitprovider.ui.screens.notifications.NotificationScreen
import com.example.doesitprovider.ui.screens.profile.ProfileScreen
import com.example.doesitprovider.ui.screens.receipts.ReceiptsScreen
import com.example.doesitprovider.ui.screens.register.RegisterScreen
import com.example.doesitprovider.ui.screens.requests.ServiceDetailScreen
import com.example.doesitprovider.ui.screens.settings.SettingScreen
import com.example.doesitprovider.ui.screens.specialties.SpecialtiesScreen

@Composable
fun SetupNavGraph() {
    val nav = rememberNavController()

    NavHost(nav, startDestination = "login") {

        composable("login") { backStackEntry ->
            val successMsg = backStackEntry.savedStateHandle.get<String>("success_msg")
            LoginScreen(
                onLoginSuccess        = { nav.navigate("home") { popUpTo("login") { inclusive = true } } },
                onGoToCadastro        = { nav.navigate("register") },
                initialSuccessMessage = successMsg
            )
            backStackEntry.savedStateHandle.remove<String>("success_msg")
        }

        composable("register") {
            RegisterScreen(
                onBackToLogin     = { nav.popBackStack() },
                onRegisterSuccess = { message ->
                    nav.previousBackStackEntry?.savedStateHandle?.set("success_msg", message)
                    nav.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToSettings      = { nav.navigate("settings") },
                onNavigateToNotifications = { nav.navigate("notifications") },
                onNavigateToHistory       = { nav.navigate("history") },
                onNavigateToReceipts      = { nav.navigate("receipts") },
                onNavigateToSpecialties   = { nav.navigate("specialties") },
                onNavigateToAddress       = { nav.navigate("address") },
                onNavigateToProfile       = { nav.navigate("profile") },
                onNavigateToServiceDetail = { id -> nav.navigate("detail/$id") }
            )
        }

        composable("detail/{id}") { back ->
            val id = back.arguments?.getString("id")?.toLongOrNull() ?: 0L
            ServiceDetailScreen(
                requestId = id,
                onBack    = { nav.popBackStack() },
                onDone    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } }
            )
        }

        composable("history") {
            HistoryScreen(
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToProfile = { nav.navigate("profile") },
                onBack              = { nav.popBackStack() },
                onNavigateToDetail  = { id -> nav.navigate("detail/$id") }
            )
        }

        composable("receipts") {
            ReceiptsScreen(
                onBack              = { nav.popBackStack() },
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToProfile = { nav.navigate("profile") }
            )
        }

        composable("avaliacoes") {
            AvaliacoesScreen(
                onBack              = { nav.popBackStack() },
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToProfile = { nav.navigate("profile") }
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToHistory = { nav.navigate("history") },
                onBack              = { nav.popBackStack() }
            )
        }

        composable("notifications") {
            NotificationScreen(
                onBack              = { nav.popBackStack() },
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToProfile = { nav.navigate("profile") }
            )
        }

        composable("settings") {
            SettingScreen(
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToHistory = { nav.navigate("history") },
                onNavigateToProfile = { nav.navigate("profile") },
                onLogout = { message ->
                    SessionManager.clear()
                    if (message != null) {
                        nav.navigate("login") { popUpTo(0) { inclusive = true } }
                        nav.currentBackStackEntry?.savedStateHandle?.set("success_msg", message)
                    } else {
                        nav.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable("specialties") {
            SpecialtiesScreen(
                onBack              = { nav.popBackStack() },
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToProfile = { nav.navigate("profile") }
            )
        }

        composable("address") {
            AddressScreen(
                onBack              = { nav.popBackStack() },
                onEdit              = { nav.navigate("edit_address") },
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToProfile = { nav.navigate("profile") }
            )
        }

        composable("edit_address") {
            EditAddressScreen(
                onBack              = { nav.popBackStack() },
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToProfile = { nav.navigate("profile") }
            )
        }
    }
}
