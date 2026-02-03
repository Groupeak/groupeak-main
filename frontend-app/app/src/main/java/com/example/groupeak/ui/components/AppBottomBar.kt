package com.example.groupeak.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.groupeak.R
import com.example.groupeak.ui.navigation.Screen

private data class BottomItem(
    val route: String,
    val label: String,
    val iconRes: Int
)

@Composable
fun AppBottomBar(
    navController: NavHostController,
    selectedRoute: String?
) {
    // Вкладки нижнего меню
    val items = listOf(
        BottomItem(Screen.Account.route, "Аккаунт", R.drawable.ic_fio),
        BottomItem(Screen.Profile.route, "Профиль", R.drawable.ic_briefcase),
        BottomItem(Screen.Feedback.route, "Связь", R.drawable.ic_email)
    )

    // Контейнер нижней панели: задаёт фон, высоту и скругление
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        // NavigationBar рисуем прозрачным, чтобы фон был от Surface
        NavigationBar(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0) // убираем системные отступы, чтобы не менялась высота
        ) {
            items.forEach { item ->
                val selected = selectedRoute == item.route

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        // Переход по вкладкам без раздувания back stack
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = item.label
                        )
                    }
                )
            }
        }
    }
}
