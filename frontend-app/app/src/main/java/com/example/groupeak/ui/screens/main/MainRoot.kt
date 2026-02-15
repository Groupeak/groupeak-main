package com.example.groupeak.ui.screens.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.groupeak.ui.components.AppBottomBar
import com.example.groupeak.ui.navigation.MainNavGraph
import com.example.groupeak.ui.navigation.Screen

@Composable
fun MainRoot(
    onLogout: () -> Unit
) {
    // Отдельный NavController для "внутренней" части приложения (вкладки)
    val mainNavController = rememberNavController()

    // Текущий route нужен, чтобы подсвечивать правильную вкладку
    val backStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Детальные экраны должны подсвечивать вкладку "Аккаунт"
    val selectedRoute = when (currentRoute) {
        Screen.ChangeEmail.route,
        Screen.ChangePassword.route -> Screen.Account.route
        // Добавь другие детальные экраны, если появятся
        else -> currentRoute
    }

    // Общий Scaffold для всех main-экранов: здесь находится единственный BottomBar
    Scaffold(
        contentWindowInsets = WindowInsets(0), // отключаем системные отступы, чтобы UI не съезжал вниз
        bottomBar = {
            AppBottomBar(
                navController = mainNavController,
                selectedRoute = selectedRoute
            )
        }
    ) { innerPadding ->
        // innerPadding учитывает место под bottomBar, чтобы контент не перекрывался
        MainNavGraph(
            navController = mainNavController,
            modifier = Modifier.padding(innerPadding),
            onLogout = onLogout
        )
    }
}
