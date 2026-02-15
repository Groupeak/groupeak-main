package com.example.groupeak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.groupeak.R
import com.example.groupeak.ui.navigation.Screen
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.NavButtonActive
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
    val items = listOf(
        BottomItem(Screen.Account.route, "Главная", R.drawable.ic_home_button),
        BottomItem("tasks", "Задачи", R.drawable.ic_tasks_button),
        BottomItem(Screen.Feedback.route, "Добавить", R.drawable.ic_add_button),
        BottomItem("calendar", "Календарь", R.drawable.ic_calendar_button),
        BottomItem(Screen.Profile.route, "Профиль", R.drawable.ic_user_button)
    )

    // Контейнер нижней панели
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 34.dp, end = 34.dp, top = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(45.dp),
            verticalAlignment = Alignment.Top
        ) {
            items.forEach { item ->
                val isSelected = selectedRoute == item.route

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            if (!isSelected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                ) {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        tint = if (isSelected) NavButtonActive else BluePrimary300,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}