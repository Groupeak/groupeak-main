package com.example.groupeak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    val route: String?,
    val label: String,
    val iconRes: Int,
)

@Composable
fun AppBottomBar(
    navController: NavHostController,
    selectedRoute: String?,
    onAddClick: () -> Unit,
) {
    val items = listOf(
        BottomItem(Screen.Home.route, "Главная", R.drawable.ic_home_button),
        BottomItem(Screen.Tasks.baseRoute, "Задачи", R.drawable.ic_tasks_button),
        BottomItem(null, "Добавить", R.drawable.ic_add_button),
        BottomItem(Screen.Events.route, "Мероприятия", R.drawable.ic_events),
        BottomItem(Screen.Account.route, "Аккаунт", R.drawable.ic_user_button),
    )

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
                .padding(top = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(45.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top
        ) {
            items.forEach { item ->
                val isSelected = item.route != null && selectedRoute == item.route

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            when {
                                item.route == null -> onAddClick()
                                !isSelected -> navController.navigate(item.route) {
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